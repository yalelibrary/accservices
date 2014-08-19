package edu.yale.sml.logic;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.yale.sml.model.DataLists;
import edu.yale.sml.model.OrbisRecord;
import edu.yale.sml.model.Report;
import edu.yale.sml.model.SearchResult;
import edu.yale.sml.model.ShelvingError;
import edu.yale.sml.persistence.BarcodeSearchDAO;
import edu.yale.sml.view.NullFileException;

/**
 * Types of methods: decorate, filter, process, calculate diff
 * Note: Do not remove/rename fields. Object is serialized for history page.
 */
public class BasicShelfScanEngine implements java.io.Serializable {

    private final static Logger logger = LoggerFactory.getLogger(BasicShelfScanEngine.class);

    /** Do not remove/rename fields without running tests first. See SerializationCheckIT.*/
    private static final long serialVersionUID = -1871752891918863039L;

    /** Main data structure */
    private DataLists reportLists;

    /** List of error items */
    private List<Report> culpritList;

    /** Warnings */
    private List<Report> enumWarnings;

    /** Error count breakdown */
    private ShelvingError shelvingError;

    /** Switchable implementation for getting data from Voyager */
    private transient BarcodeSearchDAO barcodeSearchDAO;

    /** Shelving Error calculator */
    private transient ShelvingErrorPopulator shelvingErrorPopulator;

    public BasicShelfScanEngine() {
        super();
        culpritList = new ArrayList<Report>();
        enumWarnings = new ArrayList<Report>();
        reportLists = new DataLists();
        shelvingError = new ShelvingError();
    }

    /**
     * Main entry point.
     * "bad barcodes" (no Orbis results) are not 'null barcodes' (0000...)
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public DataLists process(final List<String> barcodes, final String loc, final Date scanDate, final String oversize)
            throws IllegalAccessException, InvocationTargetException, IOException, HibernateException, NullFileException {
        logger.debug("Engine Processing . . .");

        try {
            // call Voyager
            final List<SearchResult> searchResultList = getOrbisDataForBarcodes(barcodes);

            reportLists = initCatalogList(immutableList(searchResultList));

            // Filter Items -- e.g. for Z9A394, separate Z9 A394
            reportLists.setCatalogAsList(filterCallNumbers(reportLists.getCatalogAsList()));

            // Create a "valid barcode list" (valid means no 00000)
            List<OrbisRecord> validBarcodesList = new ArrayList<OrbisRecord>(getOrbisList(reportLists));
            removeZeroBarcodes(validBarcodesList);

            int suppressedErrors = addSuppressedAndGetCount();

            List<OrbisRecord> validBarcodesSorted = new ArrayList<OrbisRecord>(validBarcodesList);
            Collections.copy(validBarcodesSorted, validBarcodesList);

            // sorted for mis-shelf?
            FullComparator fullComparator = new FullComparator();
            Collections.sort(validBarcodesSorted, fullComparator);
            fullComparator.getCulprits(); // TODO

            // set priors, and mis-shelf -- another method also runs for this
            final List<Report> legacyMisshelfs = MisshelfErrorsProcessor.legacyCalculateMisshelf(validBarcodesList, validBarcodesSorted);

            reportLists.setReportCatalogAsList(new ArrayList(legacyMisshelfs));

            //so far, no errors have been calculated except legacy mis-shelf and suppressed

            // set oversize flag by comparing against the UI value
            setOversizeFlag(getReportList(reportLists), oversize);

            // Filter out objects that do NOT have ANY errors
            List<Report> errorsOnlyList = ReportListFilter.filterReportList(Collections.unmodifiableList(getReportList(reportLists)), loc, scanDate, oversize);
            reportLists.setReportCatalogAsList(errorsOnlyList);

            // For UI
            reportLists.setCatalogSortedRaw(new ArrayList(reportLists.getCatalogAsList()));
            sortCatalogSortedRaw();
            removeNulls();

            // marked list copy for UI
            reportLists.setMarkedCatalogAsList(new ArrayList(getOrbisList(reportLists)));

            // again, strip out all null barcodes:
            reportLists.setMarkedCatalogAsList(removeNulls(reportLists.getMarkedCatalogAsList()));

            // Add * for call numbers that are out of sort order
            final int outOfPlace = markOutOfPlaceItems(reportLists.getMarkedCatalogAsList());

            //Re-calculate mis-shelf:
            culpritList = MisshelfErrorsProcessor.processMisshelfs(reportLists, Collections.unmodifiableList(legacyMisshelfs));

            // Add enums:
            addRemainingToMisshelfCulpritList(culpritList, getReportList(reportLists), getOrbisList(reportLists), loc, scanDate, oversize);

            // Add other errors:
            for (Report item : fullComparator.getCulpritList()) {
                enumWarnings.add(item);
                culpritList.add(item); // add shelving warnings to culpritList
            }

            // Fix Sort Order (i.e. original file order):
            culpritList = fixSortOrder(getOrbisList(reportLists), culpritList);

            //clear legacy misshelf:
            for (Report r: culpritList) {
                if (r.getMark() == 1) {
                    r.setText(0);
                }
            }

            reportLists.setCulpritList(culpritList); // ?

            int nullBarcodesCount = Collections.frequency(barcodes, Rules.NULL_BARCODE_STRING);

            // Calculate shelving error count:
            shelvingError = getShelvingErrorPopulator().calculate(culpritList, loc, scanDate, oversize, nullBarcodesCount, suppressedErrors, outOfPlace);
            reportLists.setShelvingError(shelvingError);

            reportLists.setEnumWarnings(enumWarnings);
            shelvingError.setEnum_warnings(fullComparator.getCulpritList().size());
        } catch (HibernateException h) {
            logger.error("Error", h);
            throw new HibernateException(h); // delegated to ErrorBean
        } catch (Throwable t) {
            logger.error("Error", t);
        }
        logger.debug("Done");
        return reportLists;
    }

    public List<SearchResult> getOrbisDataForBarcodes(List<String> barcodes) {
        return getBarcodeSearchDAO().findAllById(barcodes);
    }

    /**
     * Initialize data structure that's used for all subsequent processing
     */
    public DataLists initCatalogList(final List<SearchResult> list) throws InvocationTargetException, IllegalAccessException {
        logger.debug("Processing init catalog list");

        reportLists = CatalogInit.processCatalogList(Collections.unmodifiableList(list));
        return reportLists;
    }

    /**
     * Revert sort order to original file order
     */
    public List<Report> fixSortOrder(List<OrbisRecord> catalogList, List<Report> culpritList) {
        logger.debug("Fixing sort order");

        List<Report> naturalOrderList = new ArrayList<Report>();

        for (OrbisRecord orbisItem : catalogList) {
            Report item = LogicHelper.findFirstItemIndex(culpritList, orbisItem);
            if (item != null) {
                naturalOrderList.add(item);
            }
        }
        return naturalOrderList;
    }

    /**
     * remove 0000000
     */
    public void removeZeroBarcodes(List<OrbisRecord> purgedList) {
        logger.debug("Removing null string barcodes");

        for (OrbisRecord o : reportLists.getCatalogAsList()) {
            if (o.getITEM_BARCODE().contains("0000000")) {
                purgedList.remove(o);
            }
        }
    }

    /**
     * Adds suppressed and return count of suppressed
     */
    public int addSuppressedAndGetCount() {
        logger.debug("Adding suppressed list");

        int suppressed = 0;
        for (final OrbisRecord o : reportLists.getCatalogAsList()) {
            if (o.getSUPPRESS_IN_OPAC().trim().equals("Y") || o.getDISPLAY_CALL_NO().contains("Suppressed")) {
                suppressed++;
                reportLists.getSuppressedList().add(o);
            }
        }
        return suppressed;
    }

    public void sortCatalogSortedRaw() {
        logger.debug("Sorting raw catalog list");

        Collections.copy(reportLists.getCatalogSortedRaw(), reportLists.getCatalogAsList());
        Collections.sort(reportLists.getCatalogSortedRaw(), new FullComparator());
    }

    public void removeNulls() {
        logger.debug("Cleaning up list. Removing null barcodes");

        for (OrbisRecord o : reportLists.getCatalogAsList()) {
            if (o.getItemBarcode() == null  || o.getItemBarcode().equals(Rules.NULL_BARCODE_STRING)) {
                reportLists.getCatalogSortedRaw().remove(o);
            }
        }
    }

    public List<OrbisRecord> removeNulls(List<OrbisRecord> markedList) {
        logger.debug("Cleaning up list. Removing null barcodes");

        List<OrbisRecord> refList = new ArrayList<OrbisRecord>(markedList);
        Collections.copy(refList, markedList);

        for (OrbisRecord o : refList) {
            if (o.getItemBarcode() == null  || o.getItemBarcode().equals(Rules.NULL_BARCODE_STRING)) {
                markedList.remove(o);
            }
        }
        return markedList;
    }

    /*
     * Adds * Mark list is used in the main results page tab as well. It
     * compares on Normalized Call Number. Comparing on Display Call Number
     * results in much more errors. // e.g. :
     */
    public int markOutOfPlaceItems(List<OrbisRecord> list) {
        logger.debug("Decorating list with " + Rules.ITEM_FLAG_STRING);

        int count = 0;

        for (int i = 1; i < list.size(); i++) {
            OrbisRecord item = list.get(i);
            OrbisRecord prevItem = list.get(i - 1);

            if (item.getNormalizedCallNo() == null || prevItem.getNormalizedCallNo() == null
                    || item.getDisplayCallNo() == null || prevItem.getDisplayCallNo() == null) {
                continue;
            }
            String currentNormalized = replaceLCString(item.getNormalizedCallNo()).trim();
            String prevNormalized = replaceLCString(prevItem.getNormalizedCallNo()).trim();

            if (currentNormalized.compareTo(prevNormalized) < 0) {
                item.setDISPLAY_CALL_NO(Rules.ITEM_FLAG_STRING + item.getDisplayCallNo());
                count++;
            }
        }
        logger.debug("Done.");
        return count;
    }

    /**
     * filter obj -- e.g. PQ6613 Z9A394, separate Z9 A394
     */
    public List<OrbisRecord> filterCallNumbers(List<OrbisRecord> reportCatalogAsList) {
        logger.debug("Filter out string such as Z9");

        for (OrbisRecord o : reportCatalogAsList) {
            if (anyNull(o.getDisplayCallNo(), o.getNormalizedCallNo())) {
                continue;
            }
            // TODO doesn't count Z9 instances
            if (o.getDISPLAY_CALL_NO().contains("Z9")) {
                String[] str = o.getDisplayCallNo().split("Z9");

                if (str[1].matches("^[^\\d].*")) {
                    o.setDISPLAY_CALL_NO(o.getDisplayCallNo().replace("Z9", "Z9 "));
                    o.setNORMALIZED_CALL_NO(o.getNormalizedCallNo().replace("Z9", "Z9 "));
                }
            }
        }
        return reportCatalogAsList;
    }

    /**
     * Sets oversize flag
     */
    public void setOversizeFlag(final List<Report> itemList, final String oversize) {
        logger.debug("Setting oversize flag");

        for (Report item : itemList) {
            try {
                 Rules.printIfFieldsNull(item);

                if (item.getNormalizedCallNo().equals("Bad Barcode")) {
                    continue;
                }

                boolean isOversize = isOversizeCallNumber(item);
                logger.trace("Is barcode={} oversize={}", item.getITEM_BARCODE(), isOversize);

                if (oversize.equalsIgnoreCase("N")) {
                    if (isOversize) {
                        item.setOVERSIZE("Y"); // used?
                    }
                } else if (oversize.equalsIgnoreCase("Y")) {
                    if (isOversize) {
                        item.setOVERSIZE("Y"); // NOT AN ERROR
                    } else {
                        item.setOVERSIZE("N");
                    }
                }
            } catch (Exception e) {
                logger.debug("Error setting oversize flag for item : " + item.getITEM_BARCODE(), e);
                continue; // ?
            }
        }
    }

    public static boolean isOversizeCallNumber(Report item) {
        logger.trace("Considering call number={}", item.getDISPLAY_CALL_NO());
        return (item.getDisplayCallNo().contains("+") || item.getDisplayCallNo().toLowerCase().contains("oversize")) ? true : false;
    }

    public void addRemainingToMisshelfCulpritList(final List<Report> culpritList,
                                                  final List<Report> reportCatalogAsList,
                                                  final List<OrbisRecord> orbisList,
                                                  final String finalLocationName,
                                                  final Date scanDate,
                                                  final String oversize) {
        logger.debug("Adding Non-mis-shelf items to culprit/mis-shelf list. . .");

        for (Report item : reportCatalogAsList) {
            if (culpritList.contains(item)) {
                logger.trace("Skipping adding non-acc to culprit list: " + item.getITEM_BARCODE()
                        + " (List already contains).");
            } else if (Rules.isVoyagerError(item, finalLocationName, scanDate, oversize)) { // necessary because of legacyMisshelf()
                OrbisRecord prior = LogicHelper.priorPhysical(orbisList, item.getITEM_BARCODE());
                if (prior != null) {
                    if (prior.getDisplayCallNo() != null)
                        item.setPriorPhysical(prior.getDisplayCallNo());
                    if (prior.getItemEnum() != null)
                        item.setPriorPhysicalEnum(prior.getItemEnum());
                    if (prior.getChron() != null)
                        item.setPriorEnum(prior.getCHRON());
                } else {
                    // prior null. e.g. for the 1st item on report
                    item.setPriorPhysical("N/A");
                    item.setPriorPhysicalEnum("N/A");
                    item.setPriorPhysicalChron("N/A");
                }
                culpritList.add(item);
            } else {
                logger.trace("Skipping adding non-acc to culprit list: " + item.getITEM_BARCODE());
            }
        }

        logger.debug("Done.");
    }

    /**
     * Removes LC occurrences
     */
    public String replaceLCString(String str) {
        str = str.replace(Rules.LC_STRING_1, " ");
        str = str.replace(Rules.LC_STRING_2, " ");
        return str;
    }

    /**
     * Helper
     *
     * @param reportLists report list
     * @return report list
     */
    public List<OrbisRecord> getOrbisList(DataLists reportLists) {
        return reportLists.getCatalogAsList();
    }

    /**
     * Helper
     *
     * @param reportLists report list
     * @return report list
     */
    public List<Report> getReportList(DataLists reportLists) {
        return reportLists.getReportCatalogAsList();
    }

    public <T> List<T> immutableList(List<? extends T> list) {
        return Collections.unmodifiableList(list);
    }

    public static boolean anyNull(String s, String t) {
        return (s == null || t == null) ? true : false;
    }

    //getters and setters --------------------------------------------------------------------------------------------

     public DataLists getReportLists() {
        return reportLists;
    }

    public ShelvingError getShelvingError() {
        return shelvingError;
    }

      public void setReportLists(DataLists reportLists) {
        this.reportLists = reportLists;
    }

    public void setShelvingError(ShelvingError shelvingError) {
        this.shelvingError = shelvingError;
    }

    public BarcodeSearchDAO getBarcodeSearchDAO() {
        return barcodeSearchDAO;
    }

    public void setBarcodeSearchDAO(BarcodeSearchDAO barcodeSearchDAO) {
        this.barcodeSearchDAO = barcodeSearchDAO;
    }

    public ShelvingErrorPopulator getShelvingErrorPopulator() {
        if (shelvingErrorPopulator == null) { //TODO
            return new ShelvingErrorPopulator();
        }
        return shelvingErrorPopulator;
    }

    public void setShelvingErrorPopulator(ShelvingErrorPopulator shelvingErrorPopulator) {
        this.shelvingErrorPopulator = shelvingErrorPopulator;
    }
}
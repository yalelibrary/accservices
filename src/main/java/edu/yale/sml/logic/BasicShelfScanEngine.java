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
 *
 * NOTE: Do not remove/rename fields. Object is serialized for history page.
 *
 * TODO (refactoring) remove null barcode reference
 * TODO (refactoring) extract logic out of fullComparator
 * TODO (refactoring) see if all reportLists.setXXX methods can be re-factored
 *
 */
public class BasicShelfScanEngine implements java.io.Serializable {
    // int nullBarcodes = 0;  //?

    // NOTE: Do not remove/rename fields without running tests first

    private static final long serialVersionUID = -1871752891918863039L;

    final static Logger logger = LoggerFactory
            .getLogger(BasicShelfScanEngine.class);
    public static final String ITEM_FLAG_STRING = "*";
    public static final String LC_STRING = "( LC )";
    public static final int MAX_QUERY_COUNT = 1500;
    public static final String NOT_CHARGED_STRING = "Not Charged";
    public static final String NULL_BARCODE_STRING = "00000000";

    private DataLists reportLists; // main data structure

    private List<Report> culpritList;      // list of errors
    private List<Report> enumWarnings;     // list of warnings
    private List<Report> reportListCopy;   // for ui
    private ShelvingError shelvingError;   // represents error count breakdown

    /**
     * Main Function
     *
     * 'bad barcodes' (no voyager/orbis results) != 'null barcodes' (0000...)
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public DataLists process(final List<String> barcodes, final String location,
                             final Date scanDate, final String oversize) throws IllegalAccessException,
                             InvocationTargetException, IOException, HibernateException,
                             NullFileException {

        logger.debug("Engine Processing . . .");

        try {
            // call Voyager
            List<SearchResult> list =  new BarcodeSearchDAO().findAllById(barcodes);

            reportLists = initCatalogList(immutableList(list));

            // Filter Items -- e.g. for Z9A394, separate Z9 A394
            reportLists.setCatalogAsList(filterCallnumber(reportLists
                    .getCatalogAsList()));

            // Create validBarcodesList (valid means no 00000) and validBarcodesSorted
            List<OrbisRecord> validBarcodesList = new ArrayList<OrbisRecord>(
                    getOrbisList(reportLists));

            // remove null items & add to suppress errors
            removeZeroBarcodes(validBarcodesList);
            addSuppressed(validBarcodesList);
            List<OrbisRecord> validBarcodesSorted = new ArrayList<OrbisRecord>(
                    validBarcodesList);
            Collections.copy(validBarcodesSorted, validBarcodesList);

            // sorted for mis-shelf?
            FullComparator fullComparator = new FullComparator();
            Collections.sort(validBarcodesSorted, fullComparator);
            fullComparator.getCulprits(); // TODO

            // set priors, and mis-shelf -- another method also runs for this
            List<Report> legacyMisshelfs = MisshelfErrorsProcessor.legacyCalculateMisshelf(validBarcodesList,
                    validBarcodesSorted);

            reportLists.setReportCatalogAsList(legacyMisshelfs);

            // set oversize flag by comparing against the UI value
            setOversizeFlag(getReportList(reportLists), oversize);

            // Filter out objects that do NOT have ANY errors
            List<Report> errorsOnlyList = ReportListFilter.filterReportList(
                    Collections.unmodifiableList(getReportList(reportLists)), location,
                    scanDate, oversize);
            reportLists.setReportCatalogAsList(errorsOnlyList);

            // For UI
            reportLists.setCatalogSortedRaw(new ArrayList(reportLists
                    .getCatalogAsList()));
            sortCatalogSortedRaw();
            removeNulls();

            // For UI
            reportListCopy = new ArrayList(getReportList(reportLists));

            //Following steps are mostly for Lauren's new logic for calculating misshelfs

            // marked list copy for UI
            reportLists.setMarkedCatalogAsList(new ArrayList(getOrbisList(reportLists)));
            // again, strip out all null barcodes:
            reportLists.setMarkedCatalogAsList(removeNulls(reportLists
                    .getMarkedCatalogAsList()));

            // Add * for call nums. that are out of sort order
            markOutOfPlaceItems(reportLists.getMarkedCatalogAsList());

            //N.B. reset misshelfs to 0:
            for (Report item: getReportList(reportLists)) {
                if (item.getText() != 0) {
                    logger.debug("Erasing prior misshelf value :" + item.getITEM_BARCODE() + " : " + item.getText());
                    item.setText(0); //N.B.
                }
            }

            //re-calculate misshelf
            culpritList = MisshelfErrorsProcessor.processMisshelfs(reportLists);

            // Add enums
            addRemainingToMisshelfCulpritList(culpritList,
                    getReportList(reportLists),
                    getOrbisList(reportLists), location,
                    scanDate, oversize);

            // Add other errors:
            for (Report item : fullComparator.getCulpritList()) {
                enumWarnings.add(item);
                culpritList.add(item); // add shelving warnings to culpritList
            }

            // Fix Sort Oder (i.e. original file order) :
            culpritList = fixSortOrder(getOrbisList(reportLists),
                    culpritList);
            reportLists.setCulpritList(culpritList); // ?

            //null barcodes in list supplied:
            int nullBarcodesCount = Collections.frequency(barcodes, NULL_BARCODE_STRING);

            // Calculate shelving error count
            shelvingError = new ShelvingErrorPopulator().populateShelvingError(
                    getReportList(reportLists), location,
                    scanDate, oversize, nullBarcodesCount);
            reportLists.setShelvingError(shelvingError); // ?

            reportLists.setEnumWarnings(enumWarnings); // ?
            shelvingError.setEnum_warnings(fullComparator.getCulpritList()
                    .size());
        } catch (HibernateException h) {
            printStackTrace("Hibernate exception", h);
            throw new HibernateException(h); // delegated to ErrorBean
        } catch (Throwable t) {
            printStackTrace("Generic error", t);
        }
        return reportLists;
    }

    /**
     * Initialize datastructure that's used for all subsequent processing
     *
     * @param list
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public DataLists initCatalogList(final List<SearchResult> list)
            throws InvocationTargetException, IllegalAccessException {
        logger.debug("Init catalog list");
        //nullBarcodes = computeNullBarcodes(Collections.unmodifiableList(list));
        if (reportLists.getCatalogAsList() != null) {
            logger.debug("Pre-CatalogInit processing, list size: " +
                    reportLists.getCatalogAsList().size());
        }
        reportLists = CatalogInit.processCatalogList(Collections
                .unmodifiableList(list));
        logger.debug("Post-CatalogInit processing, list size : "
                + reportLists.getCatalogAsList().size());
        return reportLists;
    }

    /**
     * Revert sort order to original file order
     *
     * @param catalogList
     * @param culpritList
     * @return
     */
    private List<Report> fixSortOrder(List<OrbisRecord> catalogList,
                                      List<Report> culpritList) {
        logger.debug("Fixing sort order");
        List<Report> naturalOrderList = new ArrayList<Report>();
        for (OrbisRecord orbisItem : catalogList) {
            Report item = LogicHelper
                    .findFirstItemIndex(culpritList, orbisItem);
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
     * add suppressed
     * TODO clean up
     */
    public void addSuppressed(List<OrbisRecord> purgedList) {
        logger.debug("Adding suppressed list");
        for (OrbisRecord o : reportLists.getCatalogAsList()) {
            if (o.getSUPPRESS_IN_OPAC().trim().equals("Y")) {
                shelvingError.setSuppress_errors(shelvingError
                        .getSuppress_errors() + 1);
                reportLists.getSuppressedList().add(o);
                continue;
            }
        }
    }

    /**
     * ?
     */
    public void sortCatalogSortedRaw() {
        logger.debug("Sorting raw catalog list");
        Collections.copy(reportLists.getCatalogSortedRaw(),
                reportLists.getCatalogAsList());
        Collections.sort(reportLists.getCatalogSortedRaw(),
                new FullComparator());
    }

    /**
     * ?
     */
    public void removeNulls() {
        logger.debug("Cleaning up list. Removing null barcodes");
        for (OrbisRecord o : reportLists.getCatalogAsList()) {
            if (o.getITEM_BARCODE() == null
                    || o.getITEM_BARCODE().equals(NULL_BARCODE_STRING)) {
                reportLists.getCatalogSortedRaw().remove(o);
            }
        }
    }

    /**
     * ?
     */
    public List<OrbisRecord> removeNulls(List<OrbisRecord> markedList) {
        logger.debug("Cleaning up list. Removing null barcodes");
        List<OrbisRecord> refList = new ArrayList<OrbisRecord>(markedList);
        Collections.copy(refList, markedList);
        for (OrbisRecord o : refList) {
            if (o.getITEM_BARCODE() == null
                    || o.getITEM_BARCODE().equals(NULL_BARCODE_STRING)) {
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

    private void markOutOfPlaceItems(List<OrbisRecord> list) {
        logger.debug("Decorating list with " + ITEM_FLAG_STRING);
        for (int i = 1; i < list.size(); i++) {
            if (list.get(i).getNORMALIZED_CALL_NO() == null
                    || list.get(i - 1).getNORMALIZED_CALL_NO() == null
                    || list.get(i).getDISPLAY_CALL_NO() == null
                    || list.get(i - 1).getDISPLAY_CALL_NO() == null) {
                continue;
            }
            String item1 = replaceLCString(list.get(i).getNORMALIZED_CALL_NO());
            String item2 = replaceLCString(list.get(i - 1).getNORMALIZED_CALL_NO());
            if (item1.trim().compareTo(item2.trim()) < 0) {
                list.get(i).setDISPLAY_CALL_NO(
                        ITEM_FLAG_STRING
                                + list.get(i).getDISPLAY_CALL_NO());
            }
        }
        logger.debug("Done.");
    }

    /**
     *
     * @param reportCatalogAsList
     * @return
     */
    public List<OrbisRecord> filterCallnumber(
            List<OrbisRecord> reportCatalogAsList) {
        logger.debug("Filter out string such as Z9");
        // filter obj -- e.g. PQ6613 Z9A394, separate Z9 A394

        for (OrbisRecord o : reportCatalogAsList) {
            if (anyNull(o.getDISPLAY_CALL_NO(), o.getNORMALIZED_CALL_NO())) {
                continue;
            }

            // TODO doesn't count Z9 instances
            if (o.getDISPLAY_CALL_NO().contains("Z9")) {
                String[] str = o.getDISPLAY_CALL_NO().split("Z9");
                if (str[1].matches("^[^\\d].*")) {
                    o.setDISPLAY_CALL_NO(o.getDISPLAY_CALL_NO().replace("Z9",
                            "Z9 "));
                    o.setNORMALIZED_CALL_NO(o.getNORMALIZED_CALL_NO().replace(
                            "Z9", "Z9 "));
                }
            }
        }
        return reportCatalogAsList;
    }

    /**
     * Sets oversize flag
     */
    private void setOversizeFlag(final List<Report> itemList,
                                 final String oversize) {
        logger.debug("Setting oversize flag");
        List<Report> filteredList = new ArrayList<Report>(itemList);
        for (Report item : itemList) {
            try {
                if (item.getNORMALIZED_CALL_NO() == null
                        || item.getDISPLAY_CALL_NO() == null
                        || item.getLOCATION_NAME() == null
                        || item.getITEM_STATUS_DESC() == null
                        || item.getSUPPRESS_IN_OPAC() == null) {
                    logger.debug("at least one field null for: "
                            + item.getITEM_BARCODE());
                }

                if (item.getNORMALIZED_CALL_NO().equals("Bad Barcode")) {
                    continue;
                }

                boolean oversizeCallNumber = (item.getDISPLAY_CALL_NO()
                        .contains("+") || item.getDISPLAY_CALL_NO().contains(
                        "Oversize")) ? true : false;
                if (oversize.equalsIgnoreCase("N")) {
                    if (oversizeCallNumber) {
                        item.setOVERSIZE("Y"); // used?
                    }
                } else if (oversize.equalsIgnoreCase("Y")) {
                    if (oversizeCallNumber) {
                        item.setOVERSIZE("Y"); // NOT AN ERROR
                    } else {
                        item.setOVERSIZE("N");
                    }
                }
            } catch (Exception e) {
                logger.debug("Exception setting oversize flag for item : "
                        + item.getITEM_BARCODE());
                e.printStackTrace();
                continue; // ?
            }
        }
        // logger.debug("Done filtering barcodes");
    }

    /**
     *
     *
     * @param culpritList
     * @param reportCatalogAsList
     */
    public void addRemainingToMisshelfCulpritList(List<Report> culpritList,
                                                  final List<Report> reportCatalogAsList,
                                                  List<OrbisRecord> orbisList, String finalLocationName,
                                                  Date scanDate, String oversize) {
        logger.debug("Adding Non-misshelf items to culprit/misshelf list");
        //logger.debug("Current culprit list : " + culpritList.toString());
        for (Report item : reportCatalogAsList) {
            if (culpritList.contains(item)) {
                logger.debug("Skipping adding non-acc to culprit list: " + item.getITEM_BARCODE()
                        + " (List already contains).");
            } else if (Rules.isVoyagerError(item, finalLocationName, scanDate,
                    oversize)) // necessary because of legacyMisshelf()
            {
                OrbisRecord prior = LogicHelper.priorPhysical(orbisList,
                        item.getITEM_BARCODE());
                if (prior != null) {
                    if (prior.getDISPLAY_CALL_NO() != null)
                        item.setPriorPhysical(prior.getDISPLAY_CALL_NO());
                    if (prior.getITEM_ENUM() != null)
                        item.setPriorPhysicalEnum(prior.getITEM_ENUM());
                    if (prior.getCHRON() != null)
                        item.setPriorEnum(prior.getCHRON());
                } else {
                    // prior null. e.g. for the 1st item on report
                    item.setPriorPhysical("N/A");
                    item.setPriorPhysicalEnum("N/A");
                    item.setPriorPhysicalChron("N/A");
                }
                culpritList.add(item);
            } else {
                logger.debug("Skipping adding non-acc to culprit list: " + item.getITEM_BARCODE());
            }

        }
        logger.debug("DONE");
    }

    /**
     * Removes LC occurence
     * @param str
     * @return
     */
    public String replaceLCString(String str) {
        str =  str.replace("( LC )", " ");
        str = str.replace("(LC)", " ");
        return str;
    }

    /**
     * helper
     * @param reportLists
     * @return
     */
    public List<OrbisRecord> getOrbisList(DataLists reportLists) {
        return reportLists.getCatalogAsList();
    }

    /**
     * helper
     * @param reportLists
     * @return
     */
    public List<Report> getReportList(DataLists reportLists) {
        return reportLists.getReportCatalogAsList();
    }

    /**
     * Prints error messages
     */
    public static void printStackTrace(String msg, Throwable e) {
        logger.debug(msg);
        if (e.getCause() != null) {
            logger.error(e.getCause().toString());
        }
        if (e.getMessage() != null) {
            logger.error(e.getMessage());
        }
        e.printStackTrace();
    }

    /*
    public int getNullBarcodes() {
        return nullBarcodes;
    }
    */

    /*public List<OrbisRecord> getBadBarcodes() {
        return badBarcodes;
    }*/

    public List<Report> getCulpritList() {
        return culpritList;
    }

    public List<Report> getReportListCopy() {
        return reportListCopy;
    }

    public DataLists getReportLists() {
        return reportLists;
    }

    public ShelvingError getShelvingError() {
        return shelvingError;
    }

    /*
    public void setBadBarcodes(List<OrbisRecord> badBarcodes) {
        this.badBarcodes = badBarcodes;
    } */

    public void setCulpritList(List<Report> culpritList) {
        this.culpritList = culpritList;
    }

    /*
    public void setNullBarcodes(int nullBarcodes) {
        this.nullBarcodes = nullBarcodes;
    } */

    public void setReportListCopy(List<Report> reportListCopy) {
        this.reportListCopy = reportListCopy;
    }

    public void setReportLists(DataLists reportLists) {
        this.reportLists = reportLists;
    }

    public void setShelvingError(ShelvingError shelvingError) {
        this.shelvingError = shelvingError;
    }

    public void setEnumWarnings(List<Report> enumWarnings) {
        this.enumWarnings = enumWarnings;
    }

    private void setEnumWarningsSize(ShelvingError shelvingError, int size) {
        shelvingError.setEnum_warnings(size);
    }

    public <T> List<T> immutableList(List<? extends T> list) {
        return Collections.unmodifiableList(list);
    }

    public static boolean anyNull(String str, String str2) {
        return (str == null || str2 == null) ? true : false;
    }

    public BasicShelfScanEngine() {
        super();
        //badBarcodes = new ArrayList<OrbisRecord>();
        culpritList = new ArrayList<Report>();
        enumWarnings = new ArrayList<Report>();
        reportListCopy = new ArrayList<Report>();
        reportLists = new DataLists();
        shelvingError = new ShelvingError();
    }
}
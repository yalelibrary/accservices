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
 * @author od26
 * 
 */
public class BasicShelfScanEngine implements java.io.Serializable, ShelfScanEngine
{
    private static final long serialVersionUID = -1871752891918863039L;

    // Do not remove/rename fields:

    final static Logger logger = LoggerFactory.getLogger(BasicShelfScanEngine.class);
    public static final String ITEM_FLAG_STRING = "*";
    public static final String ENUM_FLAG_STRING = "@";    public static final String LC_STRING = "( LC )";
    public static final int MAX_QUERY_COUNT = 1500;
    public static final String NOT_CHARGED_STRING = "Not Charged";
    public static final String NULL_BARCODE_STRING = "00000000";
    public static final String RESET_BARCODE = "";
    public static final int MIN_ERROR_DISPLAY = 2;

    private List<OrbisRecord> badBarcodes;
    private List<Report> culpritList;
    private List<Report> enumWarnings = new ArrayList<Report>();
    private int nullBarcodes = 0;
    private List<Report> reportListCopy = new ArrayList<Report>();
    private DataLists reportLists = new DataLists(); // main data structure
    private ShelvingError shelvingError;

    // not used:
    private static boolean CHECK_FOR_LATEST_ORBISRECORD = false;
    private static boolean IGNORE_NON_DEFAULT_ITEM_STATUS = false;

    public BasicShelfScanEngine()
    {
        super();
    }

    /**
     * Calculate number of null barcodes. Null barcodes are added in another operation though, to keep
     * the file sort order intact!
     * @param list
     * @param
     * @return
     */
    private int computeNullBarcodes( final List<SearchResult> list)
    {
       for (SearchResult searchResult : list)
        {
            // e.g. for a barcode of legit length, but no result in Orbis
             if (searchResult.getResult().size() == 0)
            {
                if (searchResult.getId().contains(NULL_BARCODE_STRING))
                {
                    nullBarcodes++;
                }
            }
        }
       return nullBarcodes;
    }

    /**
     *
     * @param list
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public DataLists initCatalogList(final List<SearchResult> list) throws InvocationTargetException,
            IllegalAccessException
    {
            nullBarcodes = computeNullBarcodes(Collections.unmodifiableList(list));
            //TODO clean up
            if (reportLists.getCatalogAsList() != null)
            {
                logger.debug("Pre CatalogInit processing, list catalog size : " + reportLists.getCatalogAsList().size());
            }
            reportLists = CatalogInit.processCatalogList(Collections.unmodifiableList(list));
            logger.debug("Pre CatalogInit processing, list catalog size : " + reportLists.getCatalogAsList().size());
            return reportLists;
    }
    /**
     * Main Function
     */
    @SuppressWarnings(
    { "unchecked", "rawtypes" })
    public DataLists process(List<String> toFind, String finalLocationName, Date scanDate,
            String oversize) throws IllegalAccessException, InvocationTargetException, IOException,
            HibernateException, NullFileException
    {

        // bad barcodes different from null barcodes. check; counted twice?
        nullBarcodes = Collections.frequency(toFind, NULL_BARCODE_STRING);
        try
        {
            // init ds
            reportLists = new DataLists();
            shelvingError = new ShelvingError();

            reportLists.setCatalogAsList(new ArrayList<OrbisRecord>()); // interesting
            BarcodeSearchDAO itemdao = new BarcodeSearchDAO();

            // N.B. Critical call to Voyager DB
            List<SearchResult> list = itemdao.findAllById(toFind);

            // N.B. Put orbis results into List<Reprt> reportCatalogList
            reportLists = initCatalogList(list);

            // Filter Items -- e.g. Z9A394, separte Z9 A394
            reportLists.setCatalogAsList(filterCallnumber(reportLists.getCatalogAsList()));

            /* Create purgedList and catalogSortedPurged */
            List<OrbisRecord> purgedList = new ArrayList<OrbisRecord>(
                    reportLists.getCatalogAsList());
            purgedList = processPurgedList(purgedList);
            List<OrbisRecord> catalogSortedPurged = new ArrayList<OrbisRecord>(purgedList);
            Collections.copy(catalogSortedPurged, purgedList);

            // new: get enum_culprit_list
            FullComparator fullComparator = new FullComparator();
            // why sorted -- perchaps for misshelf?
            Collections.sort(catalogSortedPurged, fullComparator); // ?

            fullComparator.getCulprits();

            // then compare count error
            reportLists.setReportCatalogAsList(new ArrayList<Report>());

            // set priors for each report item, and calculate mis-shelf/accuracy
            // errors old style (misshelfs will be re-written again via
            // processMisshehlfs)
            processBySortOrder(purgedList, catalogSortedPurged,
                    reportLists.getReportCatalogAsList());

            // N.B. Filter out objects that DO NOT HAVE ANY ERRORS
            reportLists.setReportCatalogAsList(filterReportList(
                    reportLists.getReportCatalogAsList(), finalLocationName, scanDate, oversize));

            // Get Error count as ShelvingError (done 1st time)
            ShelvingErrorPopulator shelvingErrorPopulator = new ShelvingErrorPopulator();
            shelvingError = shelvingErrorPopulator.populateShelvingError(reportLists.getReportCatalogAsList(), finalLocationName,
                    scanDate, oversize, nullBarcodes);

            // finished processing, now process for other tabs

            // UI: raw results:
            reportLists.setCatalogSortedRaw(new ArrayList(reportLists.getCatalogAsList()));

            sortCatalogSortedRaw();

            stripCatalogSortedRawOfNullBarcodes();

            // UI: Used for printing dialog:
            reportListCopy = new ArrayList(reportLists.getReportCatalogAsList());

            // marked list copy for UI -- new Logic
            reportLists.setMarkedCatalogAsList(new ArrayList(reportLists.getCatalogAsList()));

            // again, strip out all null barcodes:

            List<OrbisRecord> refList = new ArrayList<OrbisRecord>(
                    reportLists.getMarkedCatalogAsList());
            Collections.copy(refList, reportLists.getMarkedCatalogAsList());

            for (OrbisRecord o : refList)
            {
                if (o.getITEM_BARCODE() == null || o.getITEM_BARCODE().equals(NULL_BARCODE_STRING))
                {
                    reportLists.getMarkedCatalogAsList().remove(o);
                }
            }

            // Add * for out of place call num
            decorateMarkList(reportLists.getMarkedCatalogAsList());

            // (Bit problematic: populates culprit list : enum w/ shelving
            // warnings are left out)
            // assumes all errors have been added prior
            culpritList = generateMisshelfList(culpritList,reportLists);
            
            // New Add enums, since processMisshelf() doesn't add enum warnings
            addNonAccErrorsToCulpritList(culpritList, reportLists.getReportCatalogAsList());
            
            // 2nd time (primarily for field 'accuracy errors')
            shelvingError = shelvingErrorPopulator.populateShelvingError(reportLists.getReportCatalogAsList(), finalLocationName,
                    scanDate, oversize, nullBarcodes);
            
            // TODO Note enum warnings are added. means their location errors
            // might not be counted or reported!

            // add to culprit list as well.

            for (Report item : fullComparator.getCulpritList())
            {
                enumWarnings.add(item);
                culpritList.add(item); // add shelving warnings to culpritList
            }
            
            LogicHelper.printBarcodes(culpritList);

            // re-arrange by File Order:
            culpritList = fixSortOrder(reportLists.getCatalogAsList(), culpritList);
            reportLists.setCulpritList(culpritList); // ?

            // 3rd time
            shelvingError = shelvingErrorPopulator.populateShelvingError(reportLists.getReportCatalogAsList(), finalLocationName,
                    scanDate, oversize, nullBarcodes);
            reportLists.setShelvingError(shelvingError); // TODO clean up

            reportLists.setEnumWarnings(enumWarnings);
            setEnumWarningsSize(shelvingError, fullComparator.getCulpritList().size());
            LogicHelper.printBarcodes(culpritList);
        }
        //TODO clean up exception so only one is caught
        catch (HibernateException e1)
        {
            LogicHelper.printErrors("Hibernate exception", e1);
            throw new HibernateException(e1); // delegated to ErrorBean
        }
        catch (Throwable t)
        {
            LogicHelper.printErrors("Generic error", t);
        }
        return reportLists;
    }


    private List<Report> fixSortOrder(List<OrbisRecord> catalogList, List<Report> culpritList)
    {
        List<Report> naturalOrderList = new ArrayList<Report>();

        for (OrbisRecord orbisItem : catalogList)
        {
            Report item = findFirstItemIndex(culpritList, orbisItem);
            if (item != null)
            {
                naturalOrderList.add(item);
            }
        }
        return naturalOrderList;
    }

    private Report findFirstItemIndex(List<Report> reportItems, OrbisRecord orbisItem)
    {
        for (int i = 0; i < reportItems.size(); i++)
        {
            // logicHelper method to compre orbis item to report item is used here
            if (LogicHelper.evaluateFullMatch(reportItems.get(i), orbisItem))
            {
                return reportItems.get(i);
            }
        }
        return null;
    }

    /**
     * process Purged + Suppressed
     * //TODO change signature if not used by JSF
     */
    public List<OrbisRecord> processPurgedList(List<OrbisRecord> purgedList)
    {
        for (OrbisRecord o : reportLists.getCatalogAsList())
        {
            if (o.getITEM_BARCODE().contains("0000000"))
            {
                purgedList.remove(o);
            }

            if (o.getSUPPRESS_IN_OPAC().trim().equals("Y"))
            {
                shelvingError.setSuppress_errors(shelvingError.getSuppress_errors() + 1);
                reportLists.getSuppressedList().add(o);
                continue;
            }
        }
        return purgedList;
    }

    /**
     */
    public void sortCatalogSortedRaw()
    {
        Collections.copy(reportLists.getCatalogSortedRaw(), reportLists.getCatalogAsList());
        Collections.sort(reportLists.getCatalogSortedRaw(), new FullComparator());
    }

    public void stripCatalogSortedRawOfNullBarcodes()
    {
        for (OrbisRecord o : reportLists.getCatalogAsList())
        {
            if (o.getITEM_BARCODE() == null || o.getITEM_BARCODE().equals(NULL_BARCODE_STRING))
            {
                reportLists.getCatalogSortedRaw().remove(o);
            }
        }
    }

    /**
     * Used by ShelfScanEngine. Adds all catalogSorted objects. Items with
     * accuracy, location errors are filtered out later by ShelfScanEngine.
     * 
     * @param catalogAsList
     * @param catalogSorted
     * @param reportCatalogAsList
     * @return
     */
    public void processBySortOrder(List<OrbisRecord> catalogAsList,
            List<OrbisRecord> catalogSorted, List<Report> reportCatalogAsList)
    {
        int diff = 0;
        for (int i = 0; i < catalogSorted.size(); i++)
        {
            diff = 0;
            if (i == 0)
            {
                continue; // skip 1st
            }
            if (catalogSorted.get(i).getNORMALIZED_CALL_NO() == null
                    || catalogSorted.get(i - 1).getNORMALIZED_CALL_NO() == null)
            {
                continue; // bug
            }
            String sortedItem1 = catalogSorted.get(i).getNORMALIZED_CALL_NO();
            String sortedItem2 = catalogSorted.get(i - 1).getNORMALIZED_CALL_NO();
            sortedItem1 = sortedItem1.replace("( LC )", " "); // TODO replace w/
            sortedItem2 = sortedItem2.replace("( LC )", " ");
            if (sortedItem1.equals(sortedItem2))
            {
            }
            if (catalogAsList.indexOf(catalogSorted.get(i - 1)) < catalogAsList
                    .indexOf(catalogSorted.get(i)))
            {
                Report item = Report.populateReport(catalogSorted.get(i), 0, "N/A", catalogAsList
                        .get(catalogAsList.indexOf(catalogSorted.get(i - 1))).getDISPLAY_CALL_NO(),
                        catalogAsList.get(catalogAsList.indexOf(catalogSorted.get(i - 1))),
                        catalogSorted.get(i - 1)); // hold
                reportCatalogAsList.add(item);
            }
            else
            {
                diff = Math.abs(catalogAsList.indexOf(catalogSorted.get(i - 1))
                        - catalogAsList.indexOf(catalogSorted.get(i)));
                Report item = Report.populateReport(catalogSorted.get(i), diff, "N/A",
                        catalogAsList.get(catalogAsList.indexOf(catalogSorted.get(i - 1)))
                                .getDISPLAY_CALL_NO(), catalogAsList.get(catalogAsList
                                .indexOf(catalogSorted.get(i - 1))), catalogSorted.get(i - 1)); // hold
                reportCatalogAsList.add(item);
            }
        }
    }


    /*
     * Adds * Mark list is used in theh main results page tab as well. It
     * compares on Normalized Call Number. Comparing on Display Call Number
     * results in much more errors. // e.g. :
     */

    // TODO replace LC logic w/ filter
    private List<OrbisRecord> decorateMarkList(List<OrbisRecord> catalogList)
    {
        for (int i = 1; i < catalogList.size(); i++)
        {
            if (catalogList.get(i).getNORMALIZED_CALL_NO() == null
                    || catalogList.get(i - 1).getNORMALIZED_CALL_NO() == null
                    || catalogList.get(i).getDISPLAY_CALL_NO() == null
                    || catalogList.get(i - 1).getDISPLAY_CALL_NO() == null)
            {
                continue;
            }

            String item1 = catalogList.get(i).getNORMALIZED_CALL_NO();
            String item2 = catalogList.get(i - 1).getNORMALIZED_CALL_NO();
            item1 = item1.replace("( LC )", " ");
            item1 = item1.replace("(LC)", " ");
            item2 = item2.replace("( LC )", " ");
            item2 = item2.replace("(LC)", " ");

            if (item1.trim().compareTo(item2.trim()) < 0)
            {
                catalogList.get(i).setDISPLAY_CALL_NO(
                        ITEM_FLAG_STRING + catalogList.get(i).getDISPLAY_CALL_NO());
            }
            else
            {
                //skip adding flag 
            }
        }
        return null;
    }

    public List<OrbisRecord> filterCallnumber(List<OrbisRecord> reportCatalogAsList)
    {
        // filter obj -- e.g. PQ6613 Z9A394, separate Z9 A394

        for (OrbisRecord o : reportCatalogAsList)
        {
            if (o.getDISPLAY_CALL_NO() == null || o.getNORMALIZED_CALL_NO() == null)
            {
                continue;
            }

            // TODO buggy. doesn't count Z9 instances
            if (o.getDISPLAY_CALL_NO().contains("Z9"))
            {
                String[] str = o.getDISPLAY_CALL_NO().split("Z9");
                if (str[1].matches("^[^\\d].*"))
                {
                    o.setDISPLAY_CALL_NO(o.getDISPLAY_CALL_NO().replace("Z9", "Z9 "));
                    o.setNORMALIZED_CALL_NO(o.getNORMALIZED_CALL_NO().replace("Z9", "Z9 "));
                }
            }
        }
        return reportCatalogAsList;
    }

    /**
     * Filter list -- if no errors are found, the item is not displayed in the
     * final report
     * 
     * TODO scan date?
     * 
     * TODO replace with general pattern matcher
     * 
     * @param itemList
     *            ArrayList<Report> of report entries that are displayed on the
     *            final report
     * @param finalLocationName
     *            location entered by end user when running the report
     * @param scanDate
     *            scan date entered by end user
     * @param oversize
     *            user specification of the material (options: y, intermixed, n)
     * @return
     */

    // TODO check for and report any nulls

    private List<Report> filterReportList(List<Report> itemList, String finalLocationName,
            Date scanDate, String oversize)
    {
        logger.debug("Filtering out barcodes that do not have any errors");

        List<Report> filteredList = new ArrayList<Report>(itemList);
        boolean foundError = false;

        for (Report item : itemList)
        {
            foundError = false;
            try
            {
                if (item.getNORMALIZED_CALL_NO() == null || item.getDISPLAY_CALL_NO() == null
                        || item.getLOCATION_NAME() == null || item.getITEM_STATUS_DESC() == null
                        || item.getSUPPRESS_IN_OPAC() == null)
                {
                    logger.debug("at least one field null for: " + item.getITEM_BARCODE());
                }

                if (item.getNORMALIZED_CALL_NO().equals("Bad Barcode"))
                {
                    continue;
                }

                boolean oversizeCallNumber = (item.getDISPLAY_CALL_NO().contains("+") || item
                        .getDISPLAY_CALL_NO().contains("Oversize")) ? true : false;

                if (oversize.equalsIgnoreCase("N"))
                {
                    if (oversizeCallNumber)
                    {
                        item.setOVERSIZE("Y"); // TODO hack -- not sure if it's
                                               // being used.. see
                                               // populateShelvingError()
                        foundError = true;
                    }
                    else
                    {
                    }
                }
                else if (oversize.equalsIgnoreCase("Y"))
                {
                    if (oversizeCallNumber)
                    {
                        item.setOVERSIZE("Y"); // NOT AN ERROR
                    }
                    else
                    {
                        item.setOVERSIZE("N");
                        foundError = true;
                    }
                }

                if (item.getText() != 0)
                {
                    foundError = true;
                }

                if (!item.getLOCATION_NAME().equals(finalLocationName))
                {
                    foundError = true;
                }

                if (item.getITEM_STATUS_DESC().equals("Not Charged")
                        || item.getITEM_STATUS_DESC().equals("Discharged"))
                {
                    if (item.getITEM_STATUS_DATE() != null
                            && scanDate.before(item.getITEM_STATUS_DATE()))
                    {
                        foundError = true;
                    }
                }
                else
                {
                    // System.out.print("Suspicious:" + r.getITEM_BARCODE());
                    foundError = true;
                }

                if (item.getSUPPRESS_IN_OPAC().equalsIgnoreCase("Y"))
                {
                    foundError = true;
                }

                if (!foundError)
                {
                    filteredList.remove(item); // remove if no error was found!
                }
            }
            catch (Exception e)
            {
                logger.debug("Exception filtering barcodes");
                e.printStackTrace();
                continue; // ?
            }

        }
        // logger.debug("Done filtering barcodes");

        return filteredList;
    }

    /**
     * find Date
     * TODO -- used by JSF???
     */

    public Date findMaxItemStatusDate(List<OrbisRecord> itemList, String barcode)
    {
        // assuming there's only one;
        for (OrbisRecord item : itemList)
        {
            if (item.getITEM_BARCODE().equals(barcode))
            {
                if (item.getITEM_STATUS_DATE() != null
                        && item.getITEM_STATUS_DATE().toString().length() > 1)
                {
                    return item.getITEM_STATUS_DATE();
                }
            }
        }
        return null;
    }

    /**
     * Call AccuracyErrorsProcessor: mishelf
     * Uses Lauren's logic to compute item shelving errors
     */
    public List<Report> generateMisshelfList(final List<Report> culpritList, final DataLists dataList)
    {
        return AccuracyErrorsProcessor.processMisshelfs(Collections.unmodifiableList(culpritList),dataList);
    }

    /**
     *
     * @param culpritList
     * @param reportCatalogAsList
     */
    public void addNonAccErrorsToCulpritList(List<Report> culpritList,
            final List<Report> reportCatalogAsList)
    {
        for (Report item : reportCatalogAsList)
        {
            if (item.getText() == 0)
            {
                item.setDISPLAY_CALL_NO(item.getDISPLAY_CALL_NO());

                culpritList.add(item);
            }
        }
    }

    public List<OrbisRecord> getBadBarcodes()
    {
        return badBarcodes;
    }

    public List<Report> getCulpritList()
    {
        return culpritList;
    }

    public int getNullBarcodes()
    {
        return nullBarcodes;
    }

    public List<Report> getReportListCopy()
    {
        return reportListCopy;
    }

    public DataLists getReportLists()
    {
        return reportLists;
    }

    public ShelvingError getShelvingError()
    {
        return shelvingError;
    }

    public void setBadBarcodes(List<OrbisRecord> badBarcodes)
    {
        this.badBarcodes = badBarcodes;
    }

    public void setCulpritList(List<Report> culpritList)
    {
        this.culpritList = culpritList;
    }

    public void setNullBarcodes(int nullBarcodes)
    {
        this.nullBarcodes = nullBarcodes;
    }

    public void setReportListCopy(List<Report> reportListCopy)
    {
        this.reportListCopy = reportListCopy;
    }

    public void setReportLists(DataLists reportLists)
    {
        this.reportLists = reportLists;
    }

    public void setShelvingError(ShelvingError shelvingError)
    {
        this.shelvingError = shelvingError;
    }
    /**
     * Used by JSF?
     * @return
     */
    public List<Report> getEnumWarnings()
    {
        return enumWarnings;
    }
    /**
     * Used by JSF?
     * @param enumWarnings
     */
    public void setEnumWarnings(List<Report> enumWarnings)
    {
        this.enumWarnings = enumWarnings;
    }

    private void setEnumWarningsSize(ShelvingError shelvingError, int size)
    {
        shelvingError.setEnum_warnings(size);
    }
}
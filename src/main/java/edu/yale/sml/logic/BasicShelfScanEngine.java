package edu.yale.sml.logic;


import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.beanutils.converters.DateConverter;
import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.yale.sml.logic.FullComparator;
import edu.yale.sml.model.DataLists;
import edu.yale.sml.model.Messages;
import edu.yale.sml.model.OrbisRecord;
import edu.yale.sml.model.Report;
import edu.yale.sml.model.ReportHelper;
import edu.yale.sml.model.SearchResult;
import edu.yale.sml.model.ShelvingError;
import edu.yale.sml.persistence.BarcodeSearchDAO;
import edu.yale.sml.persistence.MessagesDAO;
import edu.yale.sml.persistence.MessagesHibernateDAO;
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
    final static Logger logger = LoggerFactory.getLogger(BasicShelfScanEngine.class);

    public static final String ITEM_FLAG_STRING = "*";
    public static final String ENUM_FLAG_STRING = "@";

    public static final String LC_STRING = "( LC )";
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
    private DataLists reportLists = new DataLists(); // data structre that has
                                                     // report contents
    private ShelvingError shelvingError;

    // not used:
    private static boolean CHECK_FOR_LATEST_ORBISRECORD = false;
    private static boolean IGNORE_NON_DEFAULT_ITEM_STATUS = false;

    public BasicShelfScanEngine()
    {
        super();
    }

    public void processCatalogList(List<SearchResult> list) throws InvocationTargetException,
            IllegalAccessException
    {
        List<String> barocodesAdded = new ArrayList<String>();
        badBarcodes = new ArrayList<OrbisRecord>();

        for (SearchResult searchResult : list)
        {
            // e.g. for a barcode of legit length, but no result in Orbis

            if (searchResult.getResult().size() == 0)
            {
                // FIXME
                if (searchResult.getId().contains(NULL_BARCODE_STRING))
                {
                    nullBarcodes++;
                }
                OrbisRecord catalogObj = new OrbisRecord();
                catalogObj.setITEM_BARCODE(searchResult.getId());
                catalogObj.setDISPLAY_CALL_NO("Bad Barcode");
                catalogObj.setNORMALIZED_CALL_NO("Bad Barcode");
                catalogObj.setSUPPRESS_IN_OPAC("N/A");
                badBarcodes.add(catalogObj);

                continue; // skip full object populating
            }

            for (Map<String, Object> m : searchResult.getResult())
            {
                OrbisRecord catalogObj = new OrbisRecord();
                java.sql.Date date = null;
                Converter dc = new DateConverter(date);
                ConvertUtils.register(dc, java.sql.Date.class); // sql bug?
                BeanUtils.populate(catalogObj, m);

                // logger.debug("Added:" + catalogObj.getITEM_BARCODE());

                // Not sure what to do if CN Type null
                if (catalogObj.getCALL_NO_TYPE() == null)
                {
                    logger.debug("CN TYPE null for :" + catalogObj.getITEM_BARCODE());
                }

                if (catalogObj.getITEM_STATUS_DESC() == null
                        && catalogObj.getITEM_STATUS_DATE() == null
                        && (catalogObj.getNORMALIZED_CALL_NO() == null)
                        || catalogObj.getDISPLAY_CALL_NO() == null)
                {
                    logger.debug("Ignoring completely null record for Lauen"
                            + catalogObj.getITEM_BARCODE());
                    continue;
                }

                if (catalogObj.getITEM_STATUS_DESC() == null)
                {
                    logger.debug("ITEM_STATUS_DESC null for:" + catalogObj.getITEM_BARCODE());
                    continue;
                }

                // check if valid item status. This may cause duplicate entries:
                if (Rules.isValidItemStatus(catalogObj.getITEM_STATUS_DESC()))
                {

                    // not sure how useful it is because valid items seem to
                    // fetch only one row from Orbis (unlike invalid)

                    if (barocodesAdded.contains(catalogObj.getITEM_BARCODE())
                            && !catalogObj.getITEM_BARCODE().contains(NULL_BARCODE_STRING))
                    {
                        logger.debug("Already contains valid status item. Perhaps occurs twice!: "
                                + catalogObj.getITEM_BARCODE());
                        // check if repeat takes care of prior
                        catalogObj.setDISPLAY_CALL_NO(catalogObj.getDISPLAY_CALL_NO() + " REPEAT ");
                        reportLists.getCatalogAsList().add(catalogObj);

                    }
                    else
                    {
                        reportLists.getCatalogAsList().add(catalogObj);
                        barocodesAdded.add(catalogObj.getITEM_BARCODE());
                    }
                }
                // e.g. for barcode with Status 'Hold Status'
                else
                // if not valid item status
                {
                    printStatuses(catalogObj);

                    logger.debug("Discarding? :" + catalogObj.getITEM_BARCODE());

                    if (barocodesAdded.contains(catalogObj.getITEM_BARCODE()) == false)
                    {
                        logger.debug("Adding barcode anyway despite invalid item Status : "
                                + catalogObj.getITEM_BARCODE());
                        reportLists.getCatalogAsList().add(catalogObj);
                        barocodesAdded.add(catalogObj.getITEM_BARCODE());
                    }

                    else if (barocodesAdded.contains(catalogObj.getITEM_BARCODE()))
                    {
                        logger.debug("Already contains this item");
                        Date existingItemStatusDate = null;
                        OrbisRecord outdatedObject = findOlderItemStatusDateObject(
                                reportLists.getCatalogAsList(), catalogObj.getITEM_BARCODE());
                        if (outdatedObject != null)
                        {
                            existingItemStatusDate = outdatedObject.getITEM_STATUS_DATE();
                        }
                        else
                        {
                            logger.debug("Outdated object null!");
                        }

                        if (catalogObj.getITEM_STATUS_DATE() != null
                                && outdatedObject != null
                                && catalogObj.getITEM_STATUS_DATE().compareTo(
                                        existingItemStatusDate) > 0)
                        {
                            logger.debug("Item has more recent date:"
                                    + catalogObj.getITEM_BARCODE()
                                    + ", so it's replacing the older enttity");
                            reportLists.getCatalogAsList().remove(outdatedObject);
                            reportLists.getCatalogAsList().add(catalogObj);
                        }

                        // e.g. Missing 5-5-55 vs 'Not Charged' with status date
                        // wont' get here if item_status_desc for existing item
                        // is not null:

                        if (catalogObj.getITEM_STATUS_DATE() != null && outdatedObject == null)
                        {
                            OrbisRecord priorWithNullDate = findOlderItemStatusDesc(
                                    reportLists.getCatalogAsList(), catalogObj.getITEM_BARCODE());

                            if (priorWithNullDate != null) // &&
                                                           // Rules.isValidItemStatus(priorWithNullDate.getITEM_STATUS_DESC()))
                            {
                                logger.debug("Adding more recent invalid, and discarding older valid or invalid w/ null status date!");
                                reportLists.getCatalogAsList().remove(priorWithNullDate);
                                reportLists.getCatalogAsList().add(catalogObj);
                            }
                            else
                            {
                                logger.debug("Not sure what to do with item : "
                                        + catalogObj.getITEM_BARCODE());
                            }
                        }
                    }
                }
            }
        }
        reportLists.setNullResultBarcodes(badBarcodes);
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

        // bad barcodes different from null barcodes. check
        nullBarcodes = Collections.frequency(toFind, NULL_BARCODE_STRING); // counted
                                                                           // twice
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
            processCatalogList(list);

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
            populateShelvingError(reportLists.getReportCatalogAsList(), finalLocationName,
                    scanDate, oversize);

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
            culpritList = processMisshelfs();
            
            //printBarcodes(culpritList);

            // New Add enums, since processMisshelf() doesn't add enum warnings
            addNonAccErrorsToCulpritList(culpritList, reportLists.getReportCatalogAsList());
            
            //printBarcodes(culpritList);

            // 2nd time (primarily for field 'accuracy errors')
            populateShelvingError(reportLists.getCulpritList(), finalLocationName, scanDate,
                    oversize);
            
            //printBarcodes(culpritList);

            // TODO Note enum warnings are added. means their location errors
            // might not be counted or reported!

            // add to culprit list as well.

            for (Report item : fullComparator.getCulpritList())
            {
                enumWarnings.add(item);
                culpritList.add(item); // add shelving warnings to culpritList
            }
            
            printBarcodes(culpritList);

            // new fixSortOrder - re-arranged by File Order:
            culpritList = fixSortOrder(reportLists.getCatalogAsList(), culpritList);
            reportLists.setCulpritList(culpritList); // ?

            // 3rd time
            //printBarcodes(culpritList);
            populateShelvingError(reportLists.getCulpritList(), finalLocationName, scanDate,
                    oversize);
            reportLists.setShelvingError(shelvingError); // TODO clean up

            reportLists.setEnumWarnings(enumWarnings);
            setEnumWarningsSize(shelvingError, fullComparator.getCulpritList().size());
            printBarcodes(culpritList);
        }
        catch (HibernateException e1)
        {
            printErrors("Hibernate exception", e1);
            throw new HibernateException(e1); // delegated to ErrorBean
        }
        catch (Throwable t)
        {
            printErrors("Generic error", t);
        }
        return reportLists;
    }

    private void setEnumWarningsSize(ShelvingError shelvingError, int size)
    {
        shelvingError.setEnum_warnings(size);
    }

    private List<Report> fixSortOrder(List<OrbisRecord> catalogList, List<Report> culpritList)
    {
        List<Report> naturalOrderList = new ArrayList<Report>();

        for (OrbisRecord o : catalogList)
        {
            Report item = findFirstItemIndex(culpritList, o);
            if (item != null)
            {
                naturalOrderList.add(item);
            }
        }
        return naturalOrderList;
    }

    private Report findFirstItemIndex(List<Report> reportCatalogList, OrbisRecord o)
    {
        for (int i = 0; i < reportCatalogList.size(); i++)
        {
            if (evaluateFullMatch(reportCatalogList.get(i), o))
                return reportCatalogList.get(i);
        }
        return null;
    }

    private boolean evaluateFullMatch(Report item, OrbisRecord o)
    {
        if (item.getITEM_BARCODE().trim().equals(o.getITEM_BARCODE().trim()))
        {
            if (item.getITEM_ENUM() != null && o.getITEM_ENUM() != null)
            {
                if (item.getITEM_ENUM().equals(o.getITEM_ENUM()))
                {
                }
                else
                {
                    return false;
                }
            }
            else
            {
                if ((item.getITEM_ENUM() == null && o.getITEM_ENUM() != null)
                        || item.getITEM_ENUM() != null && o.getITEM_ENUM() == null)
                {
                    return false;
                }
            }

            // 2nd match item status desc
            if (item.getITEM_STATUS_DESC() != null && o.getITEM_STATUS_DESC() != null)
            {
                if (item.getITEM_STATUS_DESC().equals(o.getITEM_STATUS_DESC()))
                {
                }
                else
                {
                    return false;
                }
            }
            else
            {
                if ((item.getITEM_STATUS_DESC() == null && o.getITEM_STATUS_DESC() != null)
                        || item.getITEM_STATUS_DESC() != null && o.getITEM_STATUS_DESC() == null)
                {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Prints status desc and date
     * 
     * @param item
     */
    public void printStatuses(OrbisRecord item)
    {
        StringBuffer sb = new StringBuffer();
        sb.append(item.getITEM_BARCODE());

        if (item.getITEM_STATUS_DESC() == null)
        {
        }
        else
        {
            sb.append(" status_desc: " + item.getITEM_STATUS_DESC());
        }

        if (item.getITEM_STATUS_DATE() == null)
        {
            sb.append(" , Null status date");
        }
        else
        {
            sb.append(" ,status_date : " + item.getITEM_STATUS_DATE());
        }

        logger.debug(sb.toString());
    }

    /**
     * process Purged + Suppressed
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
     * Get error count
     */
    private void populateShelvingError(List<Report> reportCatalogAsList, String finalLocationName,
            Date scanDate, String oversize)
    {

        int accuracy_errors = 0;
        int total_errors = 0;
        int null_result_barcodes = 0;
        int oversize_errors = 0;
        int enum_warnings = 0;
        int location_errors = 0;
        int status_errors = 0;
        int misshelf_errors = 0;
        int misshelf_threshold_errors = 0;

        for (Report item : reportCatalogAsList)
        {
            // if there's no reason to have in the list remove the object
            if (item.getITEM_BARCODE().equals(NULL_BARCODE_STRING))
            {
                total_errors++;
            }
            else if (item.getNORMALIZED_CALL_NO().equals("Bad Barcode"))
            {
                null_result_barcodes++;
                total_errors++;
            }

            if (item.getText() != null && item.getText() != 0)
            {
                if (item.getITEM_ENUM() == null)
                {
                    accuracy_errors++;
                    misshelf_errors++;

                    if (item.getText() > MIN_ERROR_DISPLAY) // for report
                                                            // misshelf gt > 2
                    {
                        misshelf_threshold_errors++;
                    }
                }
                else if (item.getITEM_ENUM() != null)
                {
                }
                total_errors++;
            }

            if (!item.getLOCATION_NAME().equals(finalLocationName))
            {
                total_errors++;
            }

            if (!item.getLOCATION_NAME().trim().equals(finalLocationName.trim()))
            {
                location_errors++;
                accuracy_errors++;
            }

            if (item.getITEM_STATUS_DESC() != null)
            {

                if (item.getITEM_STATUS_DESC().equals("Not Charged")
                        || item.getITEM_STATUS_DESC().equals("Discharged"))
                {

                    if (item.getITEM_STATUS_DATE() != null)
                    {
                        if (scanDate.before(item.getITEM_STATUS_DATE()))
                        {
                            status_errors++;
                        }
                    }
                    else
                    {
                        // logger.debug("Item Status Desc valid, but status date Null. Not sure what to do in this case: "
                        // + item.getITEM_BARCODE() + " , with desc:" +
                        // item.getITEM_STATUS_DESC());
                    }

                }
                else
                // invalid status
                {
                    status_errors++;
                }
            }
            else
            {
                logger.debug("Item status desc null. Not sure what to do in this case: "
                        + item.getITEM_BARCODE());
            }

            if ((item.getDISPLAY_CALL_NO().contains("+") || item.getDISPLAY_CALL_NO().contains(
                    "Oversize"))
                    && oversize.equals("N"))
            {
                oversize_errors++;
            }
            else if ((!(item.getDISPLAY_CALL_NO().contains("+") || item.getDISPLAY_CALL_NO()
                    .contains("Oversize"))) && oversize.equals("Y"))
            {
                oversize_errors++;
            }

            if (item.getNORMALIZED_CALL_NO() == null)
            {
                total_errors++;
            }

            if ((item.getDISPLAY_CALL_NO().contains("+") || item.getDISPLAY_CALL_NO().contains(
                    "Oversize")))

            {
                item.setOVERSIZE("Y");
            }

        }
        shelvingError.setAccuracy_errors(accuracy_errors);
        shelvingError.setStatus_errors(status_errors);
        shelvingError.setEnum_warnings(enum_warnings);
        shelvingError.setNull_barcodes(nullBarcodes);
        shelvingError.setNull_result_barcodes(null_result_barcodes);
        shelvingError.setOversize_errors(oversize_errors);
        shelvingError.setTotal_errors(total_errors);
        shelvingError.setLocation_errors(location_errors);
        shelvingError.setStatus_errors(status_errors);
        shelvingError.setMisshelf_errors(misshelf_errors);
        shelvingError.setMisshelf_threshold_errors(misshelf_threshold_errors);
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

    private OrbisRecord findOlderItemStatusDateObject(List<OrbisRecord> catalogAsList,
            String item_BARCODE)
    {
        // assuming there's only one;
        for (OrbisRecord o : catalogAsList)
        {
            if (o.getITEM_BARCODE().equals(item_BARCODE))
            {
                if (o.getITEM_STATUS_DATE() != null
                        && o.getITEM_STATUS_DATE().toString().length() > 1)
                {
                    return o;
                }
            }
        }

        return null;
    }

    private OrbisRecord findOlderItemStatusDesc(List<OrbisRecord> catalogAsList, String item_BARCODE)
    {
        // assuming there's only one;
        for (OrbisRecord o : catalogAsList)
        {
            if (o.getITEM_BARCODE().equals(item_BARCODE))
            {
                if (o.getITEM_STATUS_DESC() != null
                        && o.getITEM_STATUS_DESC().toString().length() > 1)
                {
                    return o;
                }
            }
        }

        return null;
    }

    /**
     * find Date
     */

    public Date findMaxItemStatusDate(List<OrbisRecord> catalogList, String barcode)
    {
        // assuming there's only one;
        for (OrbisRecord o : catalogList)
        {
            if (o.getITEM_BARCODE().equals(barcode))
            {
                if (o.getITEM_STATUS_DATE() != null
                        && o.getITEM_STATUS_DATE().toString().length() > 1)
                {
                    return o.getITEM_STATUS_DATE();
                }
            }
        }
        return null;
    }

    /**
     * Helper
     */
    public void printBarcodes(List<Report> report)
    {
        logger.debug("--------------------------------------------");
        for (Report item : report)
        {
            logger.debug("Element:" + item.getITEM_BARCODE());
        }
        logger.debug("---------------------------------------------");
        logger.debug("Report size:" + report.size());

    }

    /**
     * New logic. Not sure how this works.
     * 
     */
    public List<Report> processMisshelfs()
    {
        // logger.debug("Processing acc. errors");
        final String operation = "Shelfscan";

        List<OrbisRecord> flaggedList = new ArrayList<OrbisRecord>(
                reportLists.getMarkedCatalogAsList());
        Collections.copy(flaggedList, reportLists.getMarkedCatalogAsList());
        List<OrbisRecord> sortedList = new ArrayList<OrbisRecord>(reportLists.getCatalogSortedRaw());
        Collections.copy(sortedList, reportLists.getCatalogSortedRaw());
        culpritList = new ArrayList<Report>();

        try
        {
            for (OrbisRecord o : flaggedList)
            {
                // logger.debug("Eval:" + o.getITEM_BARCODE());

                // FOR CN type bug
                if (o.getDISPLAY_CALL_NO() == null)
                {
                    logger.debug("Warning: display call number null for" + o.getITEM_BARCODE()
                            + "cannot processMisshelfs for this case");
                    LogicHelper.logMessage(operation, "",
                            "Display Call Num. null for" + o.getITEM_BARCODE()
                                    + "cannot process misshelfs for this item.");
                    continue;
                }

                if (o.getDISPLAY_CALL_NO().contains(ITEM_FLAG_STRING)) // ITEM_FLAG_STRING
                                                                       // = *
                {
                    System.out.println("Flagged item : " + o.getDISPLAY_CALL_NO());
                    int currentPosition = flaggedList.indexOf(o);
                    OrbisRecord prior = flaggedList.get(currentPosition - 1);
                    if (prior == null)
                    {
                        continue;
                    }
                    // is the prior sorted in highlighted in sorted?
                    boolean priorInSortedHighlighted = false;

                    priorInSortedHighlighted = ReportHelper.reportContains(prior, reportLists
                            .getReportCatalogAsList(), reportLists.getCatalogSortedRaw().size());

                    try
                    {
                        if (priorInSortedHighlighted)
                        {
                            System.out.println("[X] Prior in sorted highlighted for : "
                                    + o.getDISPLAY_CALL_NO());
                            OrbisRecord priorinSorted = null;
                            if (sortedList.indexOf(prior) >= 0)
                            {
                                priorinSorted = sortedList.get(sortedList.indexOf(prior));
                            }
                            else
                            {
                                logger.debug("Warning: Prior in sorted cannot be determined.");
                            }
                                                          
                            OrbisRecord priorinSortedPrior = null;
                            logger.debug("Continuing . . .");

                            try
                            {
                                logger.debug("Current Position: " + currentPosition);
                                int indexDiff = currentPosition - 2;
                                logger.debug("IndexDiff:" + indexDiff);
                                if (indexDiff >= 0)
                                {
                                    priorinSortedPrior = flaggedList.get(currentPosition - 2); // TODO
                                }
                                else
                                {
                                    //dummy record, since with current algo. prior cannot be found
                                    LogicHelper.logMessage("Shelfscan", "",
                                            "Warning: Cannot determine prior for :" + o.getITEM_BARCODE());
                                    //priorinSortedPrior.setIT//
                                    //diff = 0;
                                }
                            }
                            catch (ArrayIndexOutOfBoundsException e)
                            {
                                logger.debug("Warning: cannot determine prior");
                                LogicHelper.logMessage("Shelfscan", "",
                                        "Warning: Cannot determine prior for :" + o.getITEM_BARCODE());
                                throw e;
                            }
                            int diff = sortedList.indexOf(prior) - sortedList.indexOf(o);
                            logger.debug("Diff:" + diff);
                            logger.debug("Added to culpritlist : " + priorinSorted.getITEM_BARCODE());
                            Report reportItem = null;
                            reportItem = Report.populateReport(priorinSorted, diff,
                                    priorinSortedPrior.getDISPLAY_CALL_NO(),
                                    priorinSortedPrior.getDISPLAY_CALL_NO(), priorinSortedPrior,
                                    priorinSortedPrior);
                            if (reportItem == null)
                            {
                                logger.debug("report item null for : " + o.getITEM_BARCODE());
                            }                                                                        
                            culpritList.add(reportItem);
                            Report reportItem_Original = null;
                            reportItem_Original = Report.populateReport(o, 0,
                                    "N/A",
                                    "N/A", null, null);
                            //culpritList.add(reportItem_Original);

                            logger.debug("Added additional item : " + o.getITEM_BARCODE());
                        }
                        else
                        {
                            System.out.println("[Y] Prior NOT in sorted highlighted for: "
                                    + o.getDISPLAY_CALL_NO());
                            OrbisRecord priorinFlagged = flaggedList.get(currentPosition - 1);
                            int diff = sortedList.indexOf(o) - currentPosition;
                            culpritList.add(Report.populateReport(o, diff,
                                    priorinFlagged.getDISPLAY_CALL_NO(),
                                    priorinFlagged.getDISPLAY_CALL_NO(), priorinFlagged,
                                    priorinFlagged));
                        }
                    }
                    catch (ArrayIndexOutOfBoundsException e)
                    {
                        logger.debug("Process Misshelf: (ArrayIndexOutOfBoundsException) Item: "
                                + o.getITEM_BARCODE());
                        continue;
                    }
                }
                else
                {
                    // no * in call_no
                }
            }
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }

        logger.debug("Sending back . ..");
        printBarcodes(culpritList);
        return culpritList;
    }

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

    public String getApplicationProperty(String property)
    {
        try
        {
            MessagesDAO dao = new MessagesHibernateDAO();
            List<Messages> messageList = dao.findAll(Messages.class);
            for (Messages m : messageList)
            {
                if (m.getNAME().equals(property))
                {
                    return m.getVALUE();
                }
            }
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
        return null;

    }

    public boolean getCheckLatestPreference(String preference)
    {
        if (preference != null
                && (preference.equalsIgnoreCase("yes") || preference.equalsIgnoreCase("true")))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean getRetainPreference(String preference_RetainInvalidStatusItems)
    {
        if (preference_RetainInvalidStatusItems != null
                && (preference_RetainInvalidStatusItems.equalsIgnoreCase("yes") || preference_RetainInvalidStatusItems
                        .equalsIgnoreCase("true")))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Helper method
     */
    public void printErrors(String msg, Throwable e)
    {
        logger.debug(msg);
        logger.error(e.getCause().toString());
        logger.error(e.getMessage());
        e.printStackTrace();
    }

    public List<Report> getEnumWarnings()
    {
        return enumWarnings;
    }

    public void setEnumWarnings(List<Report> enumWarnings)
    {
        this.enumWarnings = enumWarnings;
    }

}
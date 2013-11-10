package edu.yale.sml.logic;

import edu.yale.sml.model.DataLists;
import edu.yale.sml.model.OrbisRecord;
import edu.yale.sml.model.Report;
import edu.yale.sml.model.ReportHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: odin
 * Date: 11/9/13
 * Time: 9:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class AccuracyErrorsProcessor {
    final private static Logger logger = LoggerFactory.getLogger(BasicShelfScanEngine.class);
    final private static String operation = "Shelfscan";
    final private static String ITEM_FLAG_STRING = "*";

    /**
     * New logic. Not sure how this works.
     *
     */
    public static List<Report> processMisshelfs(final List<Report> culpritList,
                                         final DataLists reportLists)

    {
        // logger.debug("Processing acc. errors");

        List<OrbisRecord> flaggedList = new ArrayList<OrbisRecord>(
                reportLists.getMarkedCatalogAsList());
        //reportLists.
        Collections.copy(flaggedList, reportLists.getMarkedCatalogAsList());
        List<OrbisRecord> sortedList = new ArrayList<OrbisRecord>(reportLists.getCatalogSortedRaw());
        Collections.copy(sortedList, reportLists.getCatalogSortedRaw());
        List<Report> errorItems = new ArrayList<Report>();

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
                            errorItems.add(reportItem);
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
                            errorItems.add(Report.populateReport(o, diff,
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
        LogicHelper.printBarcodes(culpritList);
        return errorItems;
    }

}

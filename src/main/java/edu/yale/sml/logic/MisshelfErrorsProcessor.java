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
 * Calculates misshelf. Currently, there are 2 static methods representing 2 passes.
 * <p/>
 * In first pass . . . Other items are then added. 2nd pass . . .
 */

public class MisshelfErrorsProcessor {
    final private static Logger logger = LoggerFactory.getLogger(MisshelfErrorsProcessor.class);

    final private static String APP_NAME = "Shelfscan";
    final private static String ITEM_FLAG_STRING = "*";

    /**
     * Calculates misshelfs. Pass 2.
     *
     * @param dataLists
     * @return
     */
    public static List<Report> processMisshelfs(final DataLists dataLists){
        logger.debug("(Pass/Step 2) Calculate Misshelf");

        //uses markedCatalogAsList and catalogSortedRaw
        List<OrbisRecord> itemList = new ArrayList<OrbisRecord>(dataLists.getMarkedCatalogAsList());
        Collections.copy(itemList, dataLists.getMarkedCatalogAsList());
        List<OrbisRecord> sortedList = new ArrayList<OrbisRecord>(dataLists.getCatalogSortedRaw());
        Collections.copy(sortedList, dataLists.getCatalogSortedRaw());
        List<Report> errorItems = new ArrayList<Report>();
        try {
            for (OrbisRecord o : itemList) {
                String BARCODE = o.getITEM_BARCODE();
                if (o.getDISPLAY_CALL_NO() == null) {
                    logger.debug("Disp. Call. Null :" + BARCODE + "cant calc. misshelf");
                    LogicHelper.logMessage(APP_NAME, "", "Disp. Call. Null :" + BARCODE + "cannot calc. misshelf");
                    continue;
                }

                if (o.getDISPLAY_CALL_NO().contains(ITEM_FLAG_STRING)) {

                    logger.trace("* Item : " + o.getITEM_BARCODE() + " :" + o.getDISPLAY_CALL_NO());
                    int pos = itemList.indexOf(o);
                    OrbisRecord prior = itemList.get(pos - 1);

                    if (prior == null) {
                        continue;
                    }
                    boolean priorInSortedHighlighted = false;

                    priorInSortedHighlighted = ReportHelper.reportContains(prior, dataLists.getReportCatalogAsList(), dataLists.getCatalogSortedRaw().size());

                    try {
                        if (priorInSortedHighlighted) {
                            logger.trace("[X] Prior in sorted highlighted for : " + o.getDISPLAY_CALL_NO() + o.getITEM_BARCODE());

                            OrbisRecord priorinSortedList = null;
                            OrbisRecord priorOfPriorinSortedList = null;

                            if (sortedList.indexOf(prior) >= 0) {
                                priorinSortedList = sortedList.get(sortedList.indexOf(prior));
                            } else {
                                logger.trace("Warning: Prior in sorted cannot be determined.");
                            }

                            try {
                                logger.trace("Current Position: " + pos);
                                int indexDiff = pos - 2;
                                logger.trace("IndexDiff:" + indexDiff);
                                if (indexDiff >= 0) {
                                    priorOfPriorinSortedList = itemList.get(pos - 2); // TODO
                                } else {
                                    LogicHelper.logMessage(APP_NAME, "", "can't prior for:" + o.getITEM_BARCODE());
                                }
                            } catch (ArrayIndexOutOfBoundsException e) {
                                logger.trace("Warning: cannot determine prior");
                                LogicHelper.logMessage("Shelfscan", "", "Warning: Cannot determine prior for :" + o.getITEM_BARCODE());
                                throw e;
                            }
                            int diff = sortedList.indexOf(prior) - sortedList.indexOf(o);
                            logger.trace("Diff:" + diff + " . Added to culprit List : " + priorinSortedList.getITEM_BARCODE());
                            Report reportItem = null;

                            if (priorOfPriorinSortedList == null) {
                                logger.trace("Warning  : priorinSortedPrior null");
                                priorOfPriorinSortedList = new OrbisRecord();
                                priorOfPriorinSortedList.setDISPLAY_CALL_NO("N/A");
                            }

                            // N.B. Add priorinSorted
                            reportItem = Report.populateReport(priorinSortedList, diff,
                                    priorOfPriorinSortedList.getDISPLAY_CALL_NO(),
                                    priorOfPriorinSortedList.getDISPLAY_CALL_NO(), priorOfPriorinSortedList, priorOfPriorinSortedList);

                            if (reportItem == null) {
                                logger.trace("Warning: report item null for : " + o.getITEM_BARCODE());
                            }
                            errorItems.add(reportItem);
                            logger.trace("Added item : " + reportItem.getITEM_BARCODE());

                            OrbisRecord priorinFlagged = itemList.get(pos - 1);

                            logger.trace("Prior in flagged was :" + priorinFlagged.getITEM_BARCODE());
                        } else {
                            logger.trace("[Y] Prior NOT in sorted highlighted for: " + o.getDISPLAY_CALL_NO());
                            OrbisRecord priorinFlagged = itemList.get(pos - 1);
                            //int diff = sortedList.indexOf(o) - pos;
                            //12-2-13
                            int diff = sortedList.indexOf(prior) - sortedList.indexOf(o);
                            errorItems.add(Report.populateReport(o, diff, priorinFlagged.getDISPLAY_CALL_NO(),
                                    priorinFlagged.getDISPLAY_CALL_NO(), priorinFlagged, priorinFlagged));
                            logger.trace("Added item : " + o.getITEM_BARCODE());

                        }
                    } catch (ArrayIndexOutOfBoundsException e) {
                        logger.debug("Process Misshelf: (ArrayIndexOutOfBoundsException)");
                        logger.debug("Item: " + o.getITEM_BARCODE());
                        continue;
                    } catch (NullPointerException n) {
                        logger.debug("Process Misshelf: (NullPointerException)  ");
                        logger.debug("Item: " + o.getITEM_BARCODE());
                        continue;
                    }
                } else {
                    // no * in display call num

                }
            }
        } catch (Throwable e) {
            logger.debug("Error={}", e);
        }
        //LogicHelper.printBarcodes(errorItems);
        logger.debug("Done.");
        return errorItems;
    }


    /**
     * Calculate misshelfs. Pass 1.
     * <p/>
     * Items with accuracy, location errors are
     * filtered out later by ShelfScanEngine.
     *
     * @param catalogList
     * @param sortedList
     * @return
     */
    public static List<Report> legacyCalculateMisshelf(List<OrbisRecord> catalogList, List<OrbisRecord> sortedList) {
        logger.debug("(Pass/Step 1) Calculate Misshelf");

        List<Report> reportCatalogAsList = new ArrayList<Report>();

        int diff = 0;
        for (int i = 0; i < sortedList.size(); i++) {
            diff = 0;
            if (i == 0) {
                logger.debug("Skipping 1st item in sorted list.");
                continue; // skip 1st
            }

            if (anyNull(sortedList.get(i).getNORMALIZED_CALL_NO(), sortedList.get(i - 1).getNORMALIZED_CALL_NO())) {
                logger.debug("Null norm. call num. case for barcode : " + sortedList.get(i).getITEM_BARCODE());
                continue;
            }

            if (catalogList.indexOf(sortedList.get(i - 1)) < catalogList.indexOf(sortedList.get(i))) {
                Report item = Report.populateReport(sortedList.get(i), 0, "N/A",
                        catalogList.get(catalogList.indexOf(sortedList.get(i - 1))).getDISPLAY_CALL_NO(),
                        catalogList.get(catalogList.indexOf(sortedList.get(i - 1))), sortedList.get(i - 1)); // hold

                reportCatalogAsList.add(item);
                // logger.debug("(Legacy) Added item:" + item.getITEM_BARCODE() // + " with diff: " + diff);

            } else {
                diff = Math.abs(catalogList.indexOf(sortedList.get(i - 1)) - catalogList.indexOf(sortedList.get(i)));
                Report item = Report.populateReport(sortedList.get(i), diff, "N/A",
                        catalogList.get(catalogList.indexOf(sortedList.get(i - 1))).getDISPLAY_CALL_NO(),
                        catalogList.get(catalogList.indexOf(sortedList.get(i - 1))), sortedList.get(i - 1)); // hold

                reportCatalogAsList.add(item);
                logger.trace("Added:" + item.getITEM_BARCODE() + " with diff: " + diff);
            }
        }
        return reportCatalogAsList;
    }

    public static boolean anyNull(String str, String str2) {
        return (str == null || str2 == null) ? true : false;
    }
}

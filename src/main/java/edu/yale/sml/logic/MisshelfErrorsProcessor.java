package edu.yale.sml.logic;

import edu.yale.sml.model.DataLists;
import edu.yale.sml.model.OrbisRecord;
import edu.yale.sml.model.Report;
import edu.yale.sml.model.ReportHelper;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
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

    final private static String ITEM_FLAG_STRING = "*";

    /**
     * Calculates misshelfs. Pass 2.
     *
     * @param dataLists
     * @return
     */
    public static List<Report> processMisshelfs(final DataLists dataLists) {
        logger.debug("(Pass/Step 2) Calculate Misshelf");

        //uses markedCatalogAsList and catalogSortedRaw
        List<OrbisRecord> itemList = new ArrayList<OrbisRecord>(dataLists.getMarkedCatalogAsList());
        Collections.copy(itemList, dataLists.getMarkedCatalogAsList());
        List<OrbisRecord> sortedList = new ArrayList<OrbisRecord>(dataLists.getCatalogSortedRaw());
        Collections.copy(sortedList, dataLists.getCatalogSortedRaw());
        List<Report> errorItems = new ArrayList<Report>();

        try {
            for (final OrbisRecord o : itemList) {
                String barcode = o.getITEM_BARCODE();

                final String callNum = o.getDISPLAY_CALL_NO();

                if (callNum == null) {
                    logger.error("Disp. Call. Null={}", barcode);
                    continue;
                }

                if (callNum.contains(ITEM_FLAG_STRING)) {
                    logger.debug("Eval *'ed barcode={}, {} ", barcode, callNum);

                    int pos = itemList.indexOf(o);

                    OrbisRecord p = itemList.get(pos - 1);

                    if (p == null) {
                        continue;
                    }

                    int length = dataLists.getCatalogSortedRaw().size();
                    boolean priorInSortedHighlighted = ReportHelper.reportContains(p, dataLists.getReportCatalogAsList(), length);

                    logger.debug("Prior in sorted highlight value={} for prior={}", priorInSortedHighlighted, p.getDisplayCallNo());

                    try {
                        if (priorInSortedHighlighted) {
                            logger.debug("[X] Prior in sorted highlighted list for={},={}", callNum, barcode);

                            OrbisRecord priorinSortedList = null, priorOfPriorinSortedList = null;

                            if (sortedList.indexOf(p) >= 0) {
                                priorinSortedList = sortedList.get(sortedList.indexOf(p));
                            } else {
                                logger.debug("Warning: Prior in sorted cannot be determined.");
                            }

                            try {
                                int indexDiff = pos - 2;

                                if (indexDiff >= 0) {
                                    priorOfPriorinSortedList = itemList.get(pos - 2);
                                }
                            } catch (ArrayIndexOutOfBoundsException e) {
                                logger.debug("Warning: cannot determine prior");
                                throw e;
                            }
                            int diff = sortedList.indexOf(p) - sortedList.indexOf(o);

                            logger.debug("Added to culprit list={}", priorinSortedList.getItemBarcode());

                            Report reportItem;

                            if (priorOfPriorinSortedList == null) {
                                logger.debug("Warning. Prior sorted is null for={}", barcode);
                                priorOfPriorinSortedList = new OrbisRecord();
                                priorOfPriorinSortedList.setDISPLAY_CALL_NO("N/A");
                            }

                            reportItem = Report.populateReport(priorinSortedList, diff,
                                    priorOfPriorinSortedList.getDisplayCallNo(),
                                    priorOfPriorinSortedList.getDisplayCallNo(),
                                    priorOfPriorinSortedList,
                                    priorOfPriorinSortedList);

                            errorItems.add(reportItem);

                            logger.debug("Added item={}", reportItem.getItemBarcode());

                            OrbisRecord priorinFlagged = itemList.get(pos - 1);

                            logger.debug("Prior in flagged was={}", priorinFlagged.getITEM_BARCODE());
                        } else {
                            logger.debug("[Y] Prior NOT in sorted highlighted list for={}", callNum);

                            OrbisRecord priorInFlagged = itemList.get(pos - 1);

                            int diff = sortedList.indexOf(p) - sortedList.indexOf(o);
                            errorItems.add(Report.populateReport(o, diff, priorInFlagged.getDISPLAY_CALL_NO(),
                                    priorInFlagged.getDISPLAY_CALL_NO(), priorInFlagged, priorInFlagged));

                            logger.debug("Added item={}", barcode);
                        }
                    } catch (ArrayIndexOutOfBoundsException e) {
                        logger.debug("ArrayIndexOutOfBounds. Item={}", barcode);
                        continue;
                    } catch (NullPointerException n) {
                        logger.debug("NPE. Item={}", o.getITEM_BARCODE());
                        continue;
                    } catch (Exception e) {
                        logger.error("Error", e);
                    }
                } else {
                    // no * in display call num
                }
            }
        } catch (Throwable e) {
            logger.debug("Error={}", e);
        }

        for (Report r : errorItems) {
            logger.debug(prettyPrint(r));
        }

        logger.debug("Done.");
        return errorItems;
    }

    public static String prettyPrint(Report report) {
        return ReflectionToStringBuilder.toString(report);
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
        int diff;
        for (int i = 0; i < sortedList.size(); i++) {
            diff = 0;
            if (i == 0) {
                logger.debug("Skipping 1st item in sorted list.");
                continue; // skip 1st
            }

            if (anyNull(sortedList.get(i).getNORMALIZED_CALL_NO(), sortedList.get(i - 1).getNORMALIZED_CALL_NO())) {
                logger.error("Null norm. call num. case for barcode={}", sortedList.get(i).getITEM_BARCODE());
                continue;
            }

            if (catalogList.indexOf(sortedList.get(i - 1)) < catalogList.indexOf(sortedList.get(i))) {
                Report item = Report.populateReport(sortedList.get(i), 0, "N/A",
                        catalogList.get(catalogList.indexOf(sortedList.get(i - 1))).getDISPLAY_CALL_NO(),
                        catalogList.get(catalogList.indexOf(sortedList.get(i - 1))), sortedList.get(i - 1)); // hold

                reportCatalogAsList.add(item);
                logger.debug("(Legacy) Added item:" + item.getITEM_BARCODE() + " with diff: " + diff);

            } else {
                diff = Math.abs(catalogList.indexOf(sortedList.get(i - 1)) - catalogList.indexOf(sortedList.get(i)));
                Report item = Report.populateReport(sortedList.get(i), diff, "N/A",
                        catalogList.get(catalogList.indexOf(sortedList.get(i - 1))).getDISPLAY_CALL_NO(),
                        catalogList.get(catalogList.indexOf(sortedList.get(i - 1))), sortedList.get(i - 1)); // hold

                reportCatalogAsList.add(item);
                logger.debug("(Legacy) Added:" + item.getITEM_BARCODE() + " with diff: " + diff);
            }
        }
        return reportCatalogAsList;
    }

    public static boolean anyNull(String str, String str2) {
        return (str == null || str2 == null) ? true : false;
    }
}

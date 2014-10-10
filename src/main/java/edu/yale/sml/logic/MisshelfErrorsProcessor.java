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

    public static List<Report> processMisshelfs(final DataLists dataLists, List<Report> legacyMisshelfs) {
        logger.debug("Calculate Misshelf");

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
                    logger.trace("Null call number={}", barcode);
                    continue;
                }

                if (callNum.contains(ITEM_FLAG_STRING)) {
                    logger.trace("Eval *'ed barcode={}, {} ", barcode, callNum);

                    int pos = itemList.indexOf(o);
                    OrbisRecord p = itemList.get(pos - 1);

                    if (p == null) {
                        continue;
                    }

                    int length = dataLists.getCatalogSortedRaw().size();
                    boolean priorInSortedHighlighted = ReportHelper.reportContainsNonZeroText(p, legacyMisshelfs, length);

                    logger.trace("Prior in sorted highlight value={} for prior={}", priorInSortedHighlighted, p.getDisplayCallNo());

                    try {
                        if (priorInSortedHighlighted) {
                            logger.trace("[X] Prior in sorted highlighted list for={},={}", callNum, barcode);

                            OrbisRecord priorinSortedList = null, priorOfPriorinSortedList = null;

                            if (sortedList.indexOf(p) >= 0) {
                                priorinSortedList = sortedList.get(sortedList.indexOf(p));
                            } else {
                                logger.trace("Warning: Prior in sorted cannot be determined.");
                            }

                            try {
                                int indexDiff = pos - 2;

                                if (indexDiff >= 0) {
                                    priorOfPriorinSortedList = itemList.get(pos - 2);
                                }
                            } catch (ArrayIndexOutOfBoundsException e) {
                                logger.trace("Warning: cannot determine prior");
                                throw e;
                            }

                            int diff = sortedList.indexOf(p) - sortedList.indexOf(o);

                            Report reportItem;

                            if (priorOfPriorinSortedList == null) {
                                logger.trace("Warning. Prior sorted is null for={}", barcode);
                                priorOfPriorinSortedList = new OrbisRecord();
                                priorOfPriorinSortedList.setDISPLAY_CALL_NO("N/A");
                            }

                            reportItem = Report.newReport(priorinSortedList, diff,
                                    priorOfPriorinSortedList.getDisplayCallNo(),
                                    priorOfPriorinSortedList.getDisplayCallNo(),
                                    priorOfPriorinSortedList,
                                    priorOfPriorinSortedList);

                            errorItems.add(reportItem);

                            logger.trace("Added item={}", reportItem.getItemBarcode());

                            OrbisRecord priorinFlagged = itemList.get(pos - 1);

                            logger.trace("Prior in flagged was={}", priorinFlagged.getITEM_BARCODE());
                        } else {
                            logger.trace("[Y] Prior NOT in sorted highlighted list for={}", callNum);

                            OrbisRecord priorInFlagged = itemList.get(pos - 1);

                            int diff = sortedList.indexOf(p) - sortedList.indexOf(o);
                            errorItems.add(Report.newReport(o, diff, priorInFlagged.getDISPLAY_CALL_NO(),
                                    priorInFlagged.getDISPLAY_CALL_NO(), priorInFlagged, priorInFlagged));

                            logger.trace("Added item={}", barcode);
                        }
                    } catch (ArrayIndexOutOfBoundsException e) {
                        logger.debug("ArrayIndexOutOfBounds. Item={}", barcode);
                        continue;
                    } catch (NullPointerException n) {
                        logger.debug("NPE. Item={}", o.getItemBarcode());
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

        logger.trace("Culprit list is . . . ");
        for (Report r : errorItems) {
            logger.trace(r.getITEM_BARCODE());
        }

        logger.debug("Done.");
        return errorItems;
    }

    /**
     * Arrange by sort for eventual misshelf processing
     * <p/>
     * Items with accuracy, location errors are
     * filtered out later by ShelfScanEngine.
     */
    public static List<Report> sortForMisshelf(final List<OrbisRecord> orbisList, final List<OrbisRecord> sortedList) {
        logger.debug("Sort for Mis-shelf");

        List<Report> list = new ArrayList<Report>();
        int diff;
        for (int i = 0; i < sortedList.size(); i++) {
            diff = 0;
            if (i == 0) {
                logger.trace("Skipping 1st item in sorted list.");
                continue; // skip 1st
            }

            if (anyNull(sortedList.get(i).getNORMALIZED_CALL_NO(), sortedList.get(i - 1).getNORMALIZED_CALL_NO())) {
                logger.error("Null norm. call num. case for barcode={}", sortedList.get(i).getITEM_BARCODE());
                continue;
            }

            if (orbisList.indexOf(sortedList.get(i - 1)) < orbisList.indexOf(sortedList.get(i))) {
                Report item = Report.newReport(sortedList.get(i),
                        0,
                        "N/A",
                        orbisList.get(orbisList.indexOf(sortedList.get(i - 1))).getDisplayCallNo(),
                        orbisList.get(orbisList.indexOf(sortedList.get(i - 1))),
                        sortedList.get(i - 1)); // hold

                item.setMark(0);

                list.add(item);
            } else {
                diff = Math.abs(orbisList.indexOf(sortedList.get(i - 1)) - orbisList.indexOf(sortedList.get(i)));
                //diff = 0;
                Report item = Report.newReport(sortedList.get(i),
                        diff,
                        "N/A",
                        orbisList.get(orbisList.indexOf(sortedList.get(i - 1))).getDisplayCallNo(),
                        orbisList.get(orbisList.indexOf(sortedList.get(i - 1))),
                        sortedList.get(i - 1)); // hold
                item.setMark(1);

                list.add(item);
                logger.trace("Added:" + item.getITEM_BARCODE() + " with diff: " + diff);
            }
        }
        return list;
    }

    public static boolean anyNull(String str, String str2) {
        return (str == null || str2 == null) ? true : false;
    }
}

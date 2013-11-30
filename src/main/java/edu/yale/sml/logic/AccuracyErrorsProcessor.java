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

public class AccuracyErrorsProcessor {
    final private static Logger logger = LoggerFactory
	    .getLogger(BasicShelfScanEngine.class);
    final private static String op = "Shelfscan";
    final private static String ITEM_FLAG_STRING = "*";

    /**
     * works of plain list, populates a List<Report>
     * @param dataLists
     * @return
     */
    public static List<Report> processMisshelfs(final DataLists dataLists)

    {
	logger.debug("(New) Processing acc. errors");
	List<OrbisRecord> itemList = new ArrayList<OrbisRecord>(
		dataLists.getMarkedCatalogAsList());
	Collections.copy(itemList, dataLists.getMarkedCatalogAsList());
	List<OrbisRecord> sortedList = new ArrayList<OrbisRecord>(
		dataLists.getCatalogSortedRaw());
	Collections.copy(sortedList, dataLists.getCatalogSortedRaw());
	List<Report> errorItems = new ArrayList<Report>();
	try {
	    for (OrbisRecord o : itemList) {
		String BARCODE = o.getITEM_BARCODE();
		if (o.getDISPLAY_CALL_NO() == null) {
		    logger.debug("Disp. Call. Null :" + BARCODE
			    + "cant calc. misshelf");
		    LogicHelper.logMessage(op, "", "Disp. Call. Null :"
			    + BARCODE + "cannot calc. misshelf");
		    continue;
		}

		if (o.getDISPLAY_CALL_NO().contains(ITEM_FLAG_STRING)) {
		    logger.debug("* Item : " + o.getITEM_BARCODE() + " :"
			    + o.getDISPLAY_CALL_NO());
		    int pos = itemList.indexOf(o);
		    OrbisRecord prior = itemList.get(pos - 1);
		    if (prior == null) {
			continue;
		    }
		    boolean priorInSortedHighlighted = false;

		    priorInSortedHighlighted = ReportHelper.reportContains(
			    prior, dataLists.getReportCatalogAsList(),
			    dataLists.getCatalogSortedRaw().size());

		    try {
			if (priorInSortedHighlighted) {
			    logger.debug("[X] Prior in sorted highlighted for : "
				    + o.getDISPLAY_CALL_NO()
				    + o.getITEM_BARCODE());

			    OrbisRecord priorinSortedList = null;
			    OrbisRecord priorOfPriorinSortedList = null;

			    if (sortedList.indexOf(prior) >= 0) {
				priorinSortedList = sortedList.get(sortedList
					.indexOf(prior));
			    } else {
				logger.debug("Warning: Prior in sorted cannot be determined.");
			    }

			    try {
				logger.debug("Current Position: " + pos);
				int indexDiff = pos - 2;
				logger.debug("IndexDiff:" + indexDiff);
				if (indexDiff >= 0) {
				    priorOfPriorinSortedList = itemList
					    .get(pos - 2); // TODO
				} else {
				    LogicHelper.logMessage(
					    op,
					    "",
					    "can't prior for:"
						    + o.getITEM_BARCODE());
				}
			    } catch (ArrayIndexOutOfBoundsException e) {
				logger.debug("Warning: cannot determine prior");
				LogicHelper.logMessage("Shelfscan", "",
					"Warning: Cannot determine prior for :"
						+ o.getITEM_BARCODE());
				throw e;
			    }
			    int diff = sortedList.indexOf(prior)
				    - sortedList.indexOf(o);
			    logger.debug("Diff:" + diff
				    + " . Added to culprit List : "
				    + priorinSortedList.getITEM_BARCODE());
			    Report reportItem = null;

			    if (priorOfPriorinSortedList == null) {
				logger.debug("Warning  : priorinSortedPrior null");
				priorOfPriorinSortedList = new OrbisRecord();
				priorOfPriorinSortedList
					.setDISPLAY_CALL_NO("N/A");
			    }

			    // N.B. Add priorinSorted
			    reportItem = Report.populateReport(
				    priorinSortedList, diff,
				    priorOfPriorinSortedList
					    .getDISPLAY_CALL_NO(),
				    priorOfPriorinSortedList
					    .getDISPLAY_CALL_NO(),
				    priorOfPriorinSortedList,
				    priorOfPriorinSortedList);
			    if (reportItem == null) {
				logger.debug("Warning: report item null for : "
					+ o.getITEM_BARCODE());
			    }
			    errorItems.add(reportItem);
			    logger.debug("Added item : "
				    + reportItem.getITEM_BARCODE());

			    OrbisRecord priorinFlagged = itemList.get(pos - 1);

			    logger.debug("Prior in flagged was :"
				    + priorinFlagged.getITEM_BARCODE());
			} else {
			    logger.debug("[Y] Prior NOT in sorted highlighted for: "
				    + o.getDISPLAY_CALL_NO());
			    OrbisRecord priorinFlagged = itemList.get(pos - 1);
			    //int diff = sortedList.indexOf(o) - pos;
                int diff = sortedList.indexOf(prior)
                        - sortedList.indexOf(o);
			    errorItems.add(Report.populateReport(o, diff,
				    priorinFlagged.getDISPLAY_CALL_NO(),
				    priorinFlagged.getDISPLAY_CALL_NO(),
				    priorinFlagged, priorinFlagged));
			    logger.debug("Added item : " + o.getITEM_BARCODE());

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
	    e.printStackTrace();
	}
	LogicHelper.printBarcodes(errorItems);
	logger.debug("Done.");
	return errorItems;
    }

}

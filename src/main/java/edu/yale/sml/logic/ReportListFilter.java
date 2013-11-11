package edu.yale.sml.logic;

import edu.yale.sml.model.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * HOLD
 *
 * Created with IntelliJ IDEA.
 * User: odin
 * Date: 11/10/13
 * Time: 6:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class ReportListFilter {

    final static Logger logger = LoggerFactory.getLogger("edu.yale.sml.logic.ReportListFilter");

    /**
     * Filter list -- if no errors are found, the item is not displayed in the
     * final report
     *
     * TODO scan date?   TODO check/report nulls
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
    public static List<Report> filterReportList(final List<Report> itemList, final String finalLocationName,
                                          final Date scanDate, final String oversize)
    {
        logger.debug("Filtering out barcodes that do not have any errors");
        List<Report> filteredList = new ArrayList<Report>(itemList);

        logger.debug("Copying filter list");
        Collections.copy(filteredList, itemList); //TODO new

        boolean foundError = false;

        for (Report item : itemList)
        {
            foundError = false;
            try
            {
                //TODO anyNull()
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
                        item.setOVERSIZE("Y"); // used?
                        foundError = true;
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

                if (foundError == false)
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
}

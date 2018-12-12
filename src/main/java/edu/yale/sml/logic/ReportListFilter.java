package edu.yale.sml.logic;

import edu.yale.sml.model.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReportListFilter {

    private static final Logger logger = LoggerFactory.getLogger("edu.yale.sml.logic.ReportListFilter");

    /**
     * Filter list
     *
     * @param itemList          ArrayList<Report> of report entries
     * @param finalLocationName location entered by end user when running the report
     * @param scanDate          scan date entered by end user
     * @param oversize          user specification of the material (options: y, intermixed, n)
     * @return                  filtered list
     */
    public static List<Report> filterReportList(final List<Report> itemList,
                                                final String finalLocationName,
                                                final Date scanDate,
                                                final String oversize) {
        logger.debug("Filtering out non-error barcodes");

        final List<Report> filteredList = new ArrayList<Report>(itemList);
        Collections.copy(filteredList, itemList);

        boolean errorFound;

        for (final Report item : itemList) {
            errorFound = false;
            try {

                Rules.printIfFieldsNull(item);

                if (item.getNormalizedCallNo().equals("Bad Barcode")) {
                    continue;
                }

                boolean oversizeCallNumber = (item.getDISPLAY_CALL_NO().contains("+")
                        || item.getDISPLAY_CALL_NO().toLowerCase().contains("oversize")) ? true : false;


                if (oversize.equalsIgnoreCase("N")) {
                    if (oversizeCallNumber) {
                        errorFound = true;
                    }
                } else if (oversize.equalsIgnoreCase("Y")) {
                    if (!oversizeCallNumber) {
                        errorFound = true;
                    }
                }
                /* Yue Ji Commented out on 10/23/2018 12:02 PM
                if (!item.getLocationName().equals(finalLocationName)) {
                    errorFound = true;
                }
                */
                // Yue Ji Added on 10/23/2018 12:02 PM
                String pattern = "\\b"+item.getLocationName().toLowerCase()+"\\b";
                Pattern p = Pattern.compile(pattern);
                Matcher m = p.matcher(finalLocationName.toLowerCase());
                if (m.find())   {
                    errorFound = false;
                }
                else
                    errorFound = true;

                final String desc = item.getItemStatusDesc();
                final Date statusDate = item.getItemStatusDate();

                if (desc.equals("Not Charged") || desc.equals("Discharged")) {
                    if (statusDate != null && scanDate.before(statusDate)) {
                        errorFound = true;
                    }
                } else {
                    errorFound = true;
                }

                if (item.getSuppressInOpac().equalsIgnoreCase("Y")) {
                    errorFound = true;
                }

                if (!errorFound) {
                    filteredList.remove(item); // remove if no error was found!
                }
            } catch (Exception e) {
                logger.debug("Exception filtering barcodes", e);
            }
        }
        logger.debug("Done.");
        return filteredList;
    }
}

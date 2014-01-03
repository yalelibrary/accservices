package edu.yale.sml.logic;

import edu.yale.sml.model.Report;
import edu.yale.sml.model.ShelvingError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: odin
 * Date: 11/9/13
 * Time: 11:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class ShelvingErrorPopulator {


    public static final String NULL_BARCODE_STRING = "00000000";
    final static Logger logger = LoggerFactory.getLogger(ShelvingErrorPopulator.class);
    public static final int MIN_ERROR_DISPLAY = 2;

    /*
    * Get error count. Suppress errors still done elsewhere
    */
    public ShelvingError populateShelvingError(List<Report> reportCatalogAsList, String finalLocationName,
                                               Date scanDate, String oversize, int nullBarcodes) {
        ShelvingError shelvingError = new ShelvingError();
        int accuracy_errors = 0;
        int total_errors = 0;
        int null_result_barcodes = 0;
        int oversize_errors = 0;
        int enum_warnings = 0;
        int location_errors = 0;
        int status_errors = 0;
        int misshelf_errors = 0;
        int misshelf_threshold_errors = 0;

        for (Report item : reportCatalogAsList) {
            String displayCallNumber = item.getDISPLAY_CALL_NO();

            if (displayCallNumber == null) {
                logger.debug("Display call number null for : " + item.getITEM_BARCODE());
            }

            // if there's no reason to have in the list remove the object
            if (item.getITEM_BARCODE().equals(NULL_BARCODE_STRING)) {
                total_errors++;
            } else if (item.getNORMALIZED_CALL_NO().equals("Bad Barcode")) {
                null_result_barcodes++;
                total_errors++;
            }

            if (item.getText() != null && item.getText() != 0) {
                if (item.getITEM_ENUM() == null) {
                    accuracy_errors++;
                    misshelf_errors++;
                    //for report misshelf > 2
                    if (item.getText() > MIN_ERROR_DISPLAY) {
                        misshelf_threshold_errors++;
                    }
                } else if (item.getITEM_ENUM() != null) {
                    //?
                }
                total_errors++;
            }

            if (!item.getLOCATION_NAME().equals(finalLocationName)) {
                total_errors++;
            }

            if (!item.getLOCATION_NAME().trim().equals(finalLocationName.trim())) {
                location_errors++;
                accuracy_errors++;
            }

            if (item.getITEM_STATUS_DESC() != null) {

                if (item.getITEM_STATUS_DESC().equals("Not Charged")
                        || item.getITEM_STATUS_DESC().equals("Discharged")) {
                    if (item.getITEM_STATUS_DATE() != null) {
                        if (scanDate.before(item.getITEM_STATUS_DATE())) {
                            status_errors++;
                        }
                    } else {
                        // logger.debug("Item Status Desc valid, but status date Null. Not sure what to do in this case: "
                        // + item.getITEM_BARCODE() + " , with desc:" +
                        // item.getITEM_STATUS_DESC());
                    }
                } else // invalid status
                {
                    status_errors++;
                }
            } else {
                logger.debug("Item status desc null. Not sure what to do: "
                        + item.getITEM_BARCODE());
            }

            if (displayCallNumber.contains("+") || displayCallNumber.contains(
                    "Oversize")
                    && oversize.equals("N")) {
                oversize_errors++;
            } else if ((!displayCallNumber.contains("+") || displayCallNumber
                    .contains("Oversize")) && oversize.equals("Y")) {
                oversize_errors++;
            }

            if (item.getNORMALIZED_CALL_NO() == null) {
                total_errors++;
            }

            if ((displayCallNumber.contains("+") || displayCallNumber.contains("Oversize"))) {
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

        return shelvingError;
    }

}

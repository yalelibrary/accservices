package edu.yale.sml.logic;

import edu.yale.sml.model.Report;
import edu.yale.sml.model.ShelvingError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

public class ShelvingErrorPopulator {

    public static final String NULL_BARCODE_STRING = "00000000";
    final static Logger logger = LoggerFactory.getLogger(ShelvingErrorPopulator.class);
    public static final int MIN_ERROR_DISPLAY = 2;

    /**
     * Get error count.
     */
    public ShelvingError populateShelvingError(List<Report> list, String finalLocationName,
                                               Date scanDate, String oversize, int nullBarcodes, int suppressedErrors) {
        logger.debug("Calculating or setting report header summary count.");
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

        for (Report item : list) {
            String displayCallNumber = item.getDISPLAY_CALL_NO();
            logger.debug("considering:" + item.getITEM_BARCODE());

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
                if (item.getITEM_ENUM() == null || item.getITEM_ENUM().isEmpty()) {
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

            if (!item.getLOCATION_NAME().trim().equals(finalLocationName.trim()) && item.getITEM_BARCODE().length() == 14) {
                location_errors++;
                accuracy_errors++;
            }

            if (item.getITEM_STATUS_DESC() != null) {

                if (Rules.isValidItemStatus(item.getITEM_STATUS_DESC())) {
                    if (item.getITEM_STATUS_DATE() != null) {
                        if (scanDate.before(item.getITEM_STATUS_DATE())) {
                            //TODO not sure if item.getITEM_STATUS_DATE gets time or just date.
                            if (scanDate.getTime() - item.getITEM_STATUS_DATE().getTime() > 86400000) {
                                status_errors++;
                                logger.debug("Incremented count for: " + item.getITEM_BARCODE() + ":" + item.getITEM_STATUS_DESC());
                            }
                        }
                    } else {
                        logger.debug("Item Status Desc valid, but status date Null. Not sure what to do in this case: "
                                + item.getITEM_BARCODE() + " , with desc:" +
                                item.getITEM_STATUS_DESC());
                    }
                } else // invalid status
                {
                    logger.debug("Incremented count for: " + item.getITEM_BARCODE() + ":" + item.getITEM_STATUS_DESC());
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
        shelvingError.setEnum_warnings(enum_warnings);
        shelvingError.setNull_barcodes(nullBarcodes);
        shelvingError.setNull_result_barcodes(null_result_barcodes);
        shelvingError.setOversize_errors(oversize_errors);
        shelvingError.setTotal_errors(total_errors);
        shelvingError.setLocation_errors(location_errors);
        shelvingError.setStatus_errors(status_errors);
        shelvingError.setMisshelf_errors(misshelf_errors);
        shelvingError.setMisshelf_threshold_errors(misshelf_threshold_errors);
        shelvingError.setSuppress_errors(suppressedErrors);

        logger.debug("Location error count:" + location_errors);
        logger.debug("Accuracy error count:" + accuracy_errors);
        logger.debug("Status error count:" + status_errors);

        return shelvingError;
    }

}

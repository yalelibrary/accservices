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
        logger.trace("Calculating or setting report header summary count.");
        ShelvingError shelvingError = new ShelvingError();
        int accErrors = 0;
        int totalErrors = 0;
        int nullResultBars = 0;
        int oversizeErrors = 0;
        int enumWarn = 0;
        int locError = 0;
        int statusError = 0;
        int misshelfError = 0;
        int misshelfThresholdErrors = 0;

        for (Report item : list) {
            String displayCallNumber = item.getDISPLAY_CALL_NO();
            logger.trace("considering bar:" + item.getITEM_BARCODE());

            if (displayCallNumber == null) {
                logger.trace("Display call number null for : " + item.getITEM_BARCODE());
            }

            // if there's no reason to have in the list remove the object
            if (item.getITEM_BARCODE().equals(NULL_BARCODE_STRING)) {
                totalErrors++;
            } else if (item.getNORMALIZED_CALL_NO().equals("Bad Barcode")) {
                nullResultBars++;
                totalErrors++;
            }

            if (item.getText() != null && item.getText() != 0) {
                if (item.getITEM_ENUM() == null || item.getITEM_ENUM().isEmpty()) {
                    accErrors++;
                    misshelfError++;
                    //for report misshelf > 2
                    if (item.getText() > MIN_ERROR_DISPLAY) {
                        misshelfThresholdErrors++;
                    }
                } else if (item.getITEM_ENUM() != null) {
                    // ignore
                }
                totalErrors++;
            }

            if (!item.getLOCATION_NAME().equals(finalLocationName)) {
                totalErrors++;
            }

            if (!item.getLOCATION_NAME().trim().equals(finalLocationName.trim()) && item.getITEM_BARCODE().length() == 14) {
                locError++;
                accErrors++;
            }

            if (item.getITEM_STATUS_DESC() != null) {

                if (Rules.isValidItemStatus(item.getITEM_STATUS_DESC())) {
                    if (item.getITEM_STATUS_DATE() != null) {
                        if (scanDate.before(item.getITEM_STATUS_DATE())) {
                            //TODO not sure if item.getITEM_STATUS_DATE gets time or just date.
                            if (scanDate.getTime() - item.getITEM_STATUS_DATE().getTime() > 86400000) {
                                statusError++;
                                logger.trace("Incremented count for: " + item.getITEM_BARCODE() + ":" + item.getITEM_STATUS_DESC());
                            }
                        }
                    } else {
                        logger.trace("Item Status Desc valid, but status date Null. Not sure what to do in this case: "
                                + item.getITEM_BARCODE() + " , with desc:" +
                                item.getITEM_STATUS_DESC());
                    }
                } else // invalid status
                {
                    logger.trace("Incremented count for: " + item.getITEM_BARCODE() + ":" + item.getITEM_STATUS_DESC());
                    statusError++;
                }
            } else {
                logger.trace("Item status desc null. Not sure what to do: "
                        + item.getITEM_BARCODE());
            }

            if (!oversize.equalsIgnoreCase("Intermixed")) {

                if (displayCallNumber.contains("+") || displayCallNumber.contains(
                        "Oversize")
                        && oversize.equals("N")) {
                    oversizeErrors++;
                } else if ((!displayCallNumber.contains("+") || displayCallNumber
                        .contains("Oversize")) && oversize.equals("Y")) {
                    oversizeErrors++;
                }
            }


            if (item.getNORMALIZED_CALL_NO() == null) {
                totalErrors++;
            }

            if ((displayCallNumber.contains("+") || displayCallNumber.contains("Oversize"))) {
                item.setOVERSIZE("Y");
            }
        }
        shelvingError.setAccuracy_errors(accErrors);
        shelvingError.setEnum_warnings(enumWarn);
        shelvingError.setNull_barcodes(nullBarcodes);
        shelvingError.setNull_result_barcodes(nullResultBars);
        shelvingError.setOversize_errors(oversizeErrors);
        shelvingError.setTotal_errors(totalErrors);
        shelvingError.setLocation_errors(locError);
        shelvingError.setStatus_errors(statusError);
        shelvingError.setMisshelf_errors(misshelfError);
        shelvingError.setMisshelf_threshold_errors(misshelfThresholdErrors);
        shelvingError.setSuppress_errors(suppressedErrors);

        logger.trace("Location error count:" + locError);
        logger.trace("Accuracy error count:" + accErrors);
        logger.trace("Status error count:" + statusError);

        return shelvingError;
    }

}

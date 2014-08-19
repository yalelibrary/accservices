package edu.yale.sml.logic;

import edu.yale.sml.model.Report;
import edu.yale.sml.model.ShelvingError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

public class ShelvingErrorPopulator {

    final static Logger logger = LoggerFactory.getLogger(ShelvingErrorPopulator.class);

    public static final String NULL_BARCODE_STRING = "00000000";

    private final ShelvingError shelvingError = new ShelvingError();

    @Deprecated
    public static final int MIN_ERROR_DISPLAY = 2;

    /**
     * Get error count.
     */
    public ShelvingError calculate(List<Report> list,
                                   String finalLocationName,
                                   Date scanDate,
                                   String oversize,
                                   int nullBarcodes,
                                   int suppressedErrors,
                                   int flaggedInFileOrderTableSize) {
        logger.debug("Calculating report header summary count.");
        int accErrors = 0;
        int totalErrors = 0;
        int nullResultBars = 0;
        int oversizeErrors = 0;
        int enumWarn = 0;
        int locError = 0;
        int statusError = 0;
        int misshelfError = 0; //ignored?
        //int misshelfThresholdErrors = 0; //ignored?

        for (Report item : list) {
            String dispCallNo = item.getDISPLAY_CALL_NO();
            logger.trace("Considering bar:" + item.getITEM_BARCODE());

            if (dispCallNo == null) {
                logger.trace("Display call number null for {}", item.getITEM_BARCODE());
            }

            // if there's no reason to have in the list remove the object
            if (item.getITEM_BARCODE().equals(NULL_BARCODE_STRING)) {
                totalErrors++;
            } else if (item.getNORMALIZED_CALL_NO().equals("Bad Barcode")) {
                nullResultBars++;
                totalErrors++;
            }

            if (item.getText() != null && item.getText() != 0) {
                accErrors++;
                misshelfError++;
                totalErrors++;
            }

            if (Rules.isLocationError(item.getLocationName().trim(), finalLocationName.trim())) {
                totalErrors++;
            }

            if (Rules.isLocationError(item.getLocationName(), finalLocationName)) {
                locError++;
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
                } else { //invalid status
                    logger.trace("Incremented count for: " + item.getITEM_BARCODE() + ":" + item.getITEM_STATUS_DESC());
                    statusError++;
                }
            } else {
                logger.trace("Item status desc null. Not sure what to do: " + item.getITEM_BARCODE());
            }

            //Oversize Errors
            if (!oversize.equalsIgnoreCase("Intermixed")) {
                if ((dispCallNo.contains("+") || dispCallNo.toLowerCase().contains("oversize")) && oversize.equals("N")) {
                    logger.trace("Oversize barcode={}", item.getITEM_BARCODE());
                    oversizeErrors++;
                } else if ((!dispCallNo.contains("+") && !dispCallNo.toLowerCase().contains("oversize")) && oversize.equals("Y")) {
                    oversizeErrors++;
                } else {
                    logger.trace("Not oversized={}", item.getITEM_BARCODE());
                }
            }

            if (item.getNORMALIZED_CALL_NO() == null) {
                totalErrors++;
            }

            if ((dispCallNo.contains("+") || dispCallNo.toLowerCase().contains("oversize"))) {
                item.setOVERSIZE("Y");
            }
        }
        //shelvingError.setAccuracy_errors(accErrors); //misshelf
        shelvingError.setAccuracy_errors(flaggedInFileOrderTableSize);
        shelvingError.setEnum_warnings(enumWarn);
        shelvingError.setNull_barcodes(nullBarcodes);
        shelvingError.setNull_result_barcodes(nullResultBars);
        shelvingError.setOversize_errors(oversizeErrors);
        shelvingError.setTotal_errors(totalErrors); //used?
        shelvingError.setLocation_errors(locError);
        shelvingError.setStatus_errors(statusError);
        //shelvingError.setMisshelf_errors(misshelfError); //ignored
        //shelvingError.setMisshelf_threshold_errors(misshelfThresholdErrors);
        shelvingError.setSuppress_errors(suppressedErrors);

        logger.debug("Location error count:" + locError);
        logger.debug("Accuracy error count:" + accErrors);
        logger.debug("Status error count:" + statusError);
        logger.debug("Oversize count={}", oversizeErrors);

        return shelvingError;
    }


}

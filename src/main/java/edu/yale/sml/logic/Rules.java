package edu.yale.sml.logic;

import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.yale.sml.model.OrbisRecord;
import edu.yale.sml.model.Report;

/**
 * Helper logic static class
 */
@ManagedBean
@RequestScoped
public class Rules {

    final static Logger logger = LoggerFactory.getLogger(Rules.class);

    public static final String ITEM_FLAG_STRING = "*";
    public static final String LC_STRING_1 = "( LC )";
    public static final String LC_STRING_2 = "(LC)";
    public static final String NOT_CHARGED_STRING = "Not Charged";
    public static final String NULL_BARCODE_STRING = "00000000";

    /**
     * @param s item status
     * @return whether status is valid or invalid (missing, lost, charged, or not charged)
     */
    public static boolean isValidItemStatus(final String s) {
        if (s.equals("Missing") || s.equals("Lost") || s.equals("Charged") || s.startsWith("Renewed")
                || s.startsWith("In Transit") || s.startsWith("Missing") || s.startsWith("On Hold")
                || s.startsWith("Withdrawn") || s.startsWith("Claims Returned") || s.startsWith("Lost")
                || s.startsWith("Recall Request") || s.startsWith("Overdue") || s.startsWith("Withdrawn")
                || s.startsWith("Overdue")) {
            return false;
        } else if (s.equals("Not Charged") || s.equals("Discharged") || s.startsWith("Not Charged")
                || s.startsWith("Discharged") || s.startsWith("Circulation Review")
                || s.startsWith("Cataloging Review") || s.startsWith("Damaged")) {
            return true;
        }
        return false;
    }

    public static String getLastValidDisplayCallNum(final List<OrbisRecord> list) {
        int lastValid = 0;

        for (int i = 0; i < list.size(); i++) {
            if (!list.get(i).getITEM_BARCODE().contains(NULL_BARCODE_STRING)) {
                lastValid = i;
            }
        }

        if (list.get(list.size() - 1).getITEM_BARCODE().contains(NULL_BARCODE_STRING)) {
            return list.get(lastValid).getDISPLAY_CALL_NO();
        } else {
            return list.get(list.size() - 1).getDISPLAY_CALL_NO();
        }
    }

    public static String getFirstValidDisplayCallNum(final List<OrbisRecord> list) {
        int firstValid = 0;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getITEM_BARCODE().contains(NULL_BARCODE_STRING)) {
                //ignore
            } else {
                firstValid = i;
                break;
            }
        }
        return list.get(firstValid).getDISPLAY_CALL_NO();
    }

    /*
     * same as above but returns only voyager errors .. i.e. not concerned with whether an item is a misshelf
     */

    public static boolean isVoyagerError(final Report item, final String finalLocationName, final Date scanDate,
                                         final String oversize) {
        boolean error = false;

        try {
            anyRelevantFieldNull(item); //print debug

            if (item.getNORMALIZED_CALL_NO().equals("Bad Barcode")) {
                // ignore //?
            }

            boolean oversizeCallNumber = (item.getDISPLAY_CALL_NO().contains("+")
                    || item.getDISPLAY_CALL_NO().contains("Oversize")) ? true : false;

            if (oversize.equalsIgnoreCase("N")) {
                if (oversizeCallNumber) {
                    item.setOVERSIZE("Y"); // used?
                    error = true;
                }
            } else if (oversize.equalsIgnoreCase("Y")) {
                if (oversizeCallNumber) {
                    item.setOVERSIZE("Y"); // NOT AN ERROR
                } else {
                    item.setOVERSIZE("N");
                    error = true;
                }
            }

            error = isLocationError(item.getLOCATION_NAME(), finalLocationName);

            if (Rules.isValidItemStatus(item.getITEM_STATUS_DESC())) {
                if (item.getITEM_STATUS_DATE() != null && scanDate.before(item.getITEM_STATUS_DATE())
                        && (scanDate.getTime() - item.getITEM_STATUS_DATE().getTime()) > 86400000) {
                    error = true;
                }
            } else {
                error = true;
            }

            if (item.getSUPPRESS_IN_OPAC().equalsIgnoreCase("Y")) {
                error = true;
            }
        } catch (Exception e) {
            logger.error("Exception figuring out any error with barcode :r={}", e);
        }
        logger.debug(item.getITEM_BARCODE() + " isVoyagerError? " + error);
        return error;
    }

    public static void anyRelevantFieldNull(final Report item) {
        try {
            if (item.getNORMALIZED_CALL_NO() == null || item.getDISPLAY_CALL_NO() == null
                    || item.getLOCATION_NAME() == null || item.getITEM_STATUS_DESC() == null
                    || item.getSUPPRESS_IN_OPAC() == null) {
                logger.debug("At least one field null for: " + item.getITEM_BARCODE());
            }
        } catch (Exception e) {
            //ignore
        }

    }

    public static boolean isLocationError(String locationName, String finalLocationName) {
        //logger.debug("Checking for locationName={} and finalLocationName={}", locationName, finalLocationName);

        //hardcoded check:
        if (finalLocationName.equalsIgnoreCase("art") && (locationName.equalsIgnoreCase("artref") || locationName.equalsIgnoreCase("artrefl") || locationName.equalsIgnoreCase("dra"))) {
            return false;
        }

        if (finalLocationName.equalsIgnoreCase("med") && (locationName.equalsIgnoreCase("medwk1"))) {
            return false;
        }

        if (finalLocationName.equalsIgnoreCase("medref") && (locationName.equalsIgnoreCase("medwk1") || (locationName.equalsIgnoreCase("med")))) {
            return false;
        }

        if (!locationName.equalsIgnoreCase(finalLocationName)) {
            return true;
        }

        return false;
    }

}
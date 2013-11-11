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
 * 
 * @author od26
 * 
 */
@ManagedBean
@RequestScoped
public class Rules
{

    final static Logger logger = LoggerFactory.getLogger(Rules.class);

    private static final String NULL_BARCODE_STRING="00000000";
    /**
     * @param desc
     *            item status
     * @return whether status is valid or invalid (missing, lost, charged, or not charged)
     */
    public static boolean isValidItemStatus(String desc)
    {
        if (desc.equals("Missing") || desc.equals("Lost") || desc.equals("Charged") || desc.startsWith("Renewed") || desc.startsWith("In Transit") || desc.startsWith("Missing") || desc.startsWith("On Hold") || desc.startsWith("Withdrawn") || desc.startsWith("Claims Returned")
                || desc.startsWith("Lost") || desc.startsWith("Recall Request") || desc.startsWith("Overdue") || desc.startsWith("Withdrawn") || desc.startsWith("Overdue"))
        {
            return false;
        }

        else if (desc.equals("Not Charged") || desc.equals("Discharged") || desc.startsWith("Not Charged") || desc.startsWith("Discharged"))
        {
            // System.out.println("Invalid Status:  " + desc);
            return true;
        }
        return false;
    }

    /**
     * TODO move to LogicHelper
     * Omits last call number if the barcode is 00000
     * @param list
     * @return
     */
    public static String getLastValidDisplayCallNum(List<OrbisRecord> list)
    {
        int lastValid = 0;

        for (int i = 0; i < list.size(); i++)
        {
            if (!list.get(i).getITEM_BARCODE().contains(NULL_BARCODE_STRING))
            {
                lastValid = i;
            }
        }

        if (list.get(list.size() - 1).getITEM_BARCODE().contains(NULL_BARCODE_STRING))
        {
            return list.get(lastValid).getDISPLAY_CALL_NO();
        }
        else
        {
            return list.get(list.size() - 1).getDISPLAY_CALL_NO();
        }
    }

    /**
     * TODO move to logic helper
     * @param list
     * @return
     */
    public static String getFirstValidDisplayCallNum(List<OrbisRecord> list)
    {
        int firstValid = 0;
        for (int i = 0; i < list.size(); i++)
        {
            if (list.get(i).getITEM_BARCODE().contains(NULL_BARCODE_STRING))
            {
                continue;
            }
            else
            {
                firstValid = i;
                break;
            }
        }
        return list.get(firstValid).getDISPLAY_CALL_NO();
    }
    
    // TODO fix return value -- should return on one first error found
    // TODO separate business logic class?
    public static boolean isItemError(final Report item, final String finalLocationName, final Date scanDate,
            final String oversize)
    {
        logger.debug("Filtering out barcodes that do not have any errors");
        boolean foundError = false;

        try
        {
            if (item.getNORMALIZED_CALL_NO() == null || item.getDISPLAY_CALL_NO() == null
                    || item.getLOCATION_NAME() == null || item.getITEM_STATUS_DESC() == null
                    || item.getSUPPRESS_IN_OPAC() == null)
            {
                logger.debug("at least one field null for: " + item.getITEM_BARCODE());
            }

            if (item.getNORMALIZED_CALL_NO().equals("Bad Barcode"))
            {
                // ?
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
        }
        catch (Exception e)
        {
            logger.debug("Exception figuring out any error with barcode : "
                    + item.getITEM_BARCODE());
            e.printStackTrace();
        }
        return foundError;
    }

}
package edu.yale.sml.logic;

import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

import edu.yale.sml.model.OrbisRecord;

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
     * TODO clean up if necessary
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

}
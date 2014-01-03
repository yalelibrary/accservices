package edu.yale.sml.logic;

import java.util.Comparator;

import edu.yale.sml.model.Report;

/**
 * Sorts / Compares just by Display Call Number
 */
public class CallNumberComparator implements Comparator<Report> {
    public int compare(Report o1, Report o2) {
        if (o1.getNORMALIZED_CALL_NO() == null || o2.getNORMALIZED_CALL_NO() == null) {
            return 0;
        }
        String item1 = o1.getDISPLAY_CALL_NO();
        String item2 = o2.getDISPLAY_CALL_NO();
        // TODO replace with filter
        item1 = item1.replace(BasicShelfScanEngine.LC_STRING_1, " ");
        item2 = item2.replace(BasicShelfScanEngine.LC_STRING_1, " ");
        return item1.compareTo(item2);
    }
}

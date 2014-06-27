package edu.yale.sml.model;

import java.util.List;

public class ReportHelper {

    public static boolean reportContains(OrbisRecord o, List<Report> itemList, int limit) {
        for (Report item : itemList) {
            for (int i = 0; i < limit; i++) {
                if (item.getITEM_BARCODE().equals(o.getITEM_BARCODE())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean reportContains(OrbisRecord o, List<Report> itemList) {
        for (Report item : itemList) {
            if (item.getITEM_BARCODE().equals(o.getITEM_BARCODE())) {
                return true;
            }
        }
        return false;
    }
}

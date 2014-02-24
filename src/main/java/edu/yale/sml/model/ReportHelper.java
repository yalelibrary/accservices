package edu.yale.sml.model;

import java.util.List;

public class ReportHelper {
    public static boolean containsValid(List<Report> reportCatalogList, OrbisRecord o) {
        boolean found = false;
        for (int i = 0; i < reportCatalogList.size(); i++) {
            if (reportCatalogList.get(i).getITEM_BARCODE().equals(o.getITEM_BARCODE()) && !o.getNORMALIZED_CALL_NO().equals("Bad Barcode")) {
                found = true;
            }
        }
        return found;
    }

    /**
     * Check whether List<Report> contains OrbisRecord, up to limit
     *
     * @param OrbisRecord
     * @return returns boolean true/false
     */
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

    /**
     * Check whether List<Report> contains OrbisRecord, up to limit
     *
     * @param OrbisRecord
     * @return returns boolean true/false
     */
    public static boolean reportContains(OrbisRecord o, List<Report> itemList) {
        for (Report item : itemList) {
            if (item.getITEM_BARCODE().equals(o.getITEM_BARCODE())) {
                return true;
            }
        }
        return false;
    }
}

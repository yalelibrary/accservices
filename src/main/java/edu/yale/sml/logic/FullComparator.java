/**
 *
 */
package edu.yale.sml.logic;

import java.util.Comparator;
import java.util.List;

import edu.yale.sml.model.OrbisRecord;
import edu.yale.sml.model.Report;
import edu.yale.sml.model.ReportHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Compares/sorts on enum and chron.
 */
public class FullComparator implements Comparator<OrbisRecord> {

    private final Logger logger = LoggerFactory.getLogger(FullComparator.class);

    /* Used by results.xhtml for controlling rendering */
    private static final int FLAG = 5555;

    List<OrbisRecord> culprits = new java.util.ArrayList<OrbisRecord>();
    List<Report> culpritList = new java.util.ArrayList<Report>();

    public int compare(final OrbisRecord o1, final OrbisRecord o2) {
        boolean bothEnumNull = false;
        boolean eitherEnumNull = false;
        boolean enum_found = false;

        if (o1.getNORMALIZED_CALL_NO() == null || o2.getNORMALIZED_CALL_NO() == null) {
            logger.debug("Null for comparison : " + o1.toString() + "  : " + o2.toString());
            return 0;
        }

        String item1 = o1.getNORMALIZED_CALL_NO();
        String item2 = o2.getNORMALIZED_CALL_NO();
        item1 = item1.replace("( LC )", " ").trim(); // TODO replace w/ filter
        item2 = item2.replace("( LC )", " ").trim();
        int diff, enumDiff = 0, yearDiff = 0;
        diff = item1.compareTo(item2);

        // for same call numbers, but different enums and years
        if (diff == 0) {
            if (o1.getITEM_ENUM() == null && o2.getITEM_ENUM() == null) {
                enumDiff = 0;
                bothEnumNull = true;
            }

            if (o1.getITEM_ENUM() == null || o2.getITEM_ENUM() == null) {
                eitherEnumNull = true;
            } else if (o1.getITEM_ENUM() != null && o2.getITEM_ENUM() != null) {

                if (o1.getITEM_ENUM().length() > 0 && o2.getITEM_ENUM().length() > 0) {
                    enum_found = true;
                }

                String enum1 = o1.getITEM_ENUM().replace("v.", "");
                String enum2 = o2.getITEM_ENUM().replace("v.", "");

                // TODO report back number format exception

                enum1 = enum1.replace("no.", "");
                enum2 = enum2.replace("no.", "");
                enum1 = enum1.replace("Tb.", "");
                enum2 = enum2.replace("Tb.", "");
                enum1 = enum1.replace("pt.", "");
                enum2 = enum2.replace("pt.", "");

                if (enum1.contains("-")) {
                    enum1 = enum1.replace("-", ":");
                }

                if (enum2.contains("-")) {
                    enum2 = enum2.replace("-", ":");
                }

                if (!o1.getITEM_ENUM().contains(":")) {
                    enum1 = enum1 + ":0";
                }
                if (!o2.getITEM_ENUM().contains(":")) {
                    enum2 = enum2 + ":0";
                }

                String[] s1contents = enum1.split(":");
                String[] s2contents = enum2.split(":");

                try {
                    if (Integer.parseInt(s1contents[0]) > Integer.parseInt(s2contents[0])) {
                        enumDiff = 1;
                    } else if (Integer.parseInt(s1contents[0]) < Integer.parseInt(s2contents[0])) {
                        enumDiff = -1;
                    } else if (Integer.parseInt(s1contents[0]) == Integer.parseInt(s2contents[0])) {

                        if (Integer.parseInt(s1contents[1]) > Integer.parseInt(s1contents[1])) {
                            enumDiff = 1;
                        } else if (Integer.parseInt(s1contents[1]) < Integer.parseInt(s1contents[1])) {
                            enumDiff = -1;
                        }
                    }
                } catch (NumberFormatException n) {
                    logger.debug("Number Format Exception" + n.getCause() + n.getMessage());
                    enumDiff = 0;
                }
            }

            // compare for chron / year:
            if (o1.getCHRON() == null && o2.getCHRON() == null) {
                yearDiff = 0;
            } else if (o1.getCHRON() != null && notDVD(o1.getCHRON()) && o2.getCHRON() != null && notDVD(o2.getCHRON())
                    && (bothEnumNull || eitherEnumNull || o1.getITEM_ENUM().equals(o2.getITEM_ENUM()))) {
                yearDiff = o1.getCHRON().compareTo(o2.getCHRON()); // tmp disabled
            }
        }
        int aggregate = diff + enumDiff + yearDiff;
        if (enum_found && aggregate > 0) {           
             culprits.add(o1); // redundant

            //if added already, skip all - -this is to prevent an enum from appearing multiple times
           
            if (!alreadyAdded(o1, culpritList)) {              
                culpritList.add(Report.newReport(o1, FLAG, "N/A", "N/A", o2, null));
            }
        }
        return diff + enumDiff + yearDiff;
    }


    public boolean alreadyAdded(OrbisRecord o, List<Report> culpritList) {
        return ReportHelper.reportContains(o, culpritList);
    }

    public List<Report> getCulpritList() {
        return culpritList;
    }

    public void setCulpritList(List<Report> culpritList) {
        this.culpritList = culpritList;
    }

    public List<OrbisRecord> getCulprits() {
        return culprits;
    }

    public void setCulprits(List<OrbisRecord> culprits) {
        this.culprits = culprits;
    }

    public boolean notDVD(String chron)
    {
        return (chron.contains("CD") || chron.contains("DVD")) ? false : true;
    }
}
/**
 *
 */
package edu.yale.sml.logic;

import java.util.Comparator;
import java.util.List;

import edu.yale.sml.model.OrbisRecord;
import edu.yale.sml.model.Report;
import edu.yale.sml.model.ReportHelper;

/**
 * Compares/sorts on enum and chron.
 *
 * @see edu.yale.sml.logic.comparator.CallNumberComparator
 */
public class FullComparator implements Comparator<OrbisRecord> {

    List<OrbisRecord> culprits = new java.util.ArrayList<OrbisRecord>();
    List<Report> culpritList = new java.util.ArrayList<Report>();
    private static final int FLAG = 5555;
    org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(FullComparator.class);


    public int compare(OrbisRecord o1, OrbisRecord o2) {

        boolean bothEnumNull = false;
        boolean eitherEnumNull = false;
        boolean enum_found = false; // TODO CLEAN UP REMOVE

        if (o1 == null || o2 == null) {
            // logger.debug("Null record");
        }

        if (o1.getNORMALIZED_CALL_NO() == null || o2.getNORMALIZED_CALL_NO() == null) {
            logger.debug("Null for comparison : " + o1.toString() + "  : " + o2.toString());
            return 0;
        }

        /*
         * logger.debug("Comparing :" + o1.getNORMALIZED_CALL_NO() + ":" + o2.getNORMALIZED_CALL_NO());
         */

        String item1 = o1.getNORMALIZED_CALL_NO();
        String item2 = o2.getNORMALIZED_CALL_NO();

        item1 = item1.replace("( LC )", " ").trim(); // TODO replace w/ filter
        item2 = item2.replace("( LC )", " ").trim();

        int diff = 0, enum_diff = 0, year_diff = 0;


        diff = item1.compareTo(item2);

        // for same call numbers, but different enums and years
        if (diff == 0) {
            // logger.debug("[ComparatorImpl] Call Num. same, but sorting on enum/year for : " + o1.getITEM_BARCODE() + " w/ " + o2.getITEM_BARCODE());

            if (item1.length() != item2.length()) {
                // logger.debug("Length not equal for :" + item1 + "and" + item2);
            }

            if (o1.getENUM_VALUE() == null && o2.getENUM_VALUE() == null) {
                // check for chron?
                enum_diff = 0;
                bothEnumNull = true;
            }

            if (o1.getENUM_VALUE() == null || o2.getENUM_VALUE() == null) {
                // check for chron?
                eitherEnumNull = true;
            } else if (o1.getITEM_ENUM() != null && o2.getITEM_ENUM() != null) {
                if (o1.getENUM_VALUE().length() > 0 && o2.getENUM_VALUE().length() > 0) {
                    enum_found = true;
                }

                String s1 = o1.getITEM_ENUM().replace("v.", "");
                String s2 = o2.getITEM_ENUM().replace("v.", "");

                // TODO report back number format exception

                s1 = s1.replace("no.", "");
                s2 = s2.replace("no.", "");

                s1 = s1.replace("Tb.", "");
                s2 = s2.replace("Tb.", "");

                s1 = s1.replace("pt.", "");
                s2 = s2.replace("pt.", "");

                if (s1.contains("-")) {
                    s1 = s1.replace("-", ":");
                }

                if (s2.contains("-")) {
                    s2 = s2.replace("-", ":");
                }

                if (!o1.getENUM_VALUE().contains(":")) {
                    s1 = s1 + ":0";
                }
                if (!o2.getENUM_VALUE().contains(":")) {
                    s2 = s2 + ":0";
                }

                // enum_diff = s1.compareTo(s2);
                String[] s1contents = s1.split(":");
                String[] s2contents = s2.split(":");

                try {
                    if (Integer.parseInt(s1contents[0]) > Integer.parseInt(s2contents[0])) {
                        enum_diff = 1;
                    } else if (Integer.parseInt(s1contents[0]) < Integer.parseInt(s2contents[0])) {
                        enum_diff = -1;
                    } else if (Integer.parseInt(s1contents[0]) == Integer.parseInt(s2contents[0])) {

                        if (Integer.parseInt(s1contents[1]) > Integer.parseInt(s1contents[1])) {
                            enum_diff = 1;
                        } else if (Integer.parseInt(s1contents[1]) < Integer.parseInt(s1contents[1])) {
                            enum_diff = -1;
                        } else {
                            // logger.debug("Unk sort. Preceeding colon OK, but error.");
                        }
                    } else {
                        // logger.debug("Sort Unknown condition.");
                    }

                } catch (NumberFormatException n) {
                    logger.debug("Number Format Exception" + n.getCause() + n.getMessage());
                    enum_diff = 0;
                }
            } else if (o1.getENUM_VALUE() != null && o2.getENUM_VALUE() == null) {
            } else if (o1.getENUM_VALUE() == null && o2.getENUM_VALUE() != null) {
            }

            // compare for chron / year:

            if (o1.getCHRON_VALUE() == null && o2.getCHRON_VALUE() == null) {
                year_diff = 0;
            } else if (o1.getCHRON() != null && notDVD(o1.getCHRON()) && o2.getCHRON() != null && notDVD(o2.getCHRON()) && (bothEnumNull || eitherEnumNull || o1.getENUM_VALUE().equals(o2.getENUM_VALUE()))) {
                year_diff = o1.getCHRON().compareTo(o2.getCHRON()); // tmp disabled
            } else if (o1.getCHRON_VALUE() != null && o2.getCHRON_VALUE() == null) {
            } else if (o1.getCHRON_VALUE() == null && o2.getCHRON_VALUE() != null) {
            } else {
            }
        }
        int aggregate = diff + enum_diff + year_diff;
        if (enum_found && aggregate > 0) {
            culprits.add(o1); // redundant

            //if added already, skip all - -this is to prevent an enum from appearing multiple times

            if (true && !alreadyAdded(o1, culpritList)) {
                culpritList.add(Report.populateReport(o1, FLAG, "N/A", "N/A", o2, null));
            } else {
            }

        }
        return diff + enum_diff + year_diff;
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

    public boolean notDVD(String chron) // TODO replace with non number regex
    {
        return (chron.contains("CD") || chron.contains("DVD")) ? false : true;
    }
}
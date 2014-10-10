package edu.yale.sml.model;

import java.io.Serializable;
import java.sql.Date;


public class Report implements Serializable {

    private static final long serialVersionUID = 5079435243211265712L;

    private String CHRON = "";

    private String DISPLAY_CALL_NO = "";

    private String ENCODING_LEVEL;

    private String ITEM_BARCODE = "";

    private String ITEM_ENUM = "";

    private String ITEM_ID = "";

    private Date ITEM_STATUS_DATE = null;

    private String ITEM_STATUS_DESC = "";

    private String LOCATION_NAME = "";

    private String MFHD_ID = "";

    private String NORMALIZED_CALL_NO = "";

    private String OVERSIZE = "";

    private String SUPPRESS_IN_OPAC;

    private String YEAR = "";

    private String CALL_NO_TYPE = "";

    private OrbisRecord orbisRecord;

    private String priorEnum = "";

    private String priorChron = "";

    private String priorPhysicalEnum = "";

    private String priorPhysicalChron = "";

    private OrbisRecord physicalPrior;

    private String prior = "";

    private String priorPhysical = "";

    private String marker = "";

    private Integer text; //acc error

    private Integer mark = 0;

    public Report() {
    }

    public static Report newReport(OrbisRecord o, Integer flag, String prior, String physicalPrior, OrbisRecord physicalPriorO, OrbisRecord priorO) {

        String oversize = "N";

        if (o.getNORMALIZED_CALL_NO().contains("+") || o.getNORMALIZED_CALL_NO().toLowerCase().contains("oversize")) {
            oversize = "Y";
        }

        try {
            //new Report(flag, o.getITEM_BARCODE(), o.getDISPLAY_CALL_NO(), o.getITEM_ENUM(), oversize, o.getCHRON(), o.getCHRON(), o.getLOCATION_NAME(), o.getITEM_STATUS_DESC(), o.getITEM_STATUS_DATE(), o.getSUPPRESS_IN_OPAC(), o.getCALL_NO_TYPE());
        } catch (Exception e) {
            //ok to just report the exception, not raise it
        }

        Report rc = new Report(flag, o.getITEM_BARCODE(), o.getDISPLAY_CALL_NO(), o.getITEM_ENUM(), oversize, o.getCHRON(), o.getCHRON(), o.getLOCATION_NAME(), o.getITEM_STATUS_DESC(), o.getITEM_STATUS_DATE(), o.getSUPPRESS_IN_OPAC(), o.getCALL_NO_TYPE());

        rc.setPrior(prior);
        rc.setPriorPhysical(physicalPrior);

        if (priorO != null) {
            rc.setPriorEnum(priorO.getITEM_ENUM());
            rc.setPriorChron(priorO.getCHRON());
        }

        if (physicalPriorO != null) {
            rc.setPriorPhysicalEnum(physicalPriorO.getITEM_ENUM());
            rc.setPriorPhysicalChron(physicalPriorO.getCHRON());
            rc.setPhysicalPrior(physicalPriorO);
        }
        return rc;
    }

    public OrbisRecord getPhysicalPrior() {
        return physicalPrior;
    }

    public void setPhysicalPrior(OrbisRecord physicalPrior) {
        this.physicalPrior = physicalPrior;
    }

    public String getPriorEnum() {
        return priorEnum;
    }

    public void setPriorEnum(String priorEnum) {
        this.priorEnum = priorEnum;
    }

    public String getPriorChron() {
        return priorChron;
    }

    public void setPriorChron(String priorChron) {
        this.priorChron = priorChron;
    }

    public String getPriorPhysicalEnum() {
        return priorPhysicalEnum;
    }

    public void setPriorPhysicalEnum(String priorPhysicalEnum) {
        this.priorPhysicalEnum = priorPhysicalEnum;
    }

    public String getPriorPhysicalChron() {
        return priorPhysicalChron;
    }

    public void setPriorPhysicalChron(String priorPhysicalChron) {
        this.priorPhysicalChron = priorPhysicalChron;
    }

    // this is used by report only
    public Report(Integer text, String iTEM_BARCODE, String dISPLAY_CALL_NO, String iTEM_ENUM, String o, String year,
                  String chron, String location_name, String item_status_desc, Date item_status_date,
                  String item_suppress, String call_no_type) {
        super();
        if (text >= 0)
            this.text = text;
        if (text < 0)
            this.text = text * -1;

        ITEM_BARCODE = iTEM_BARCODE;
        DISPLAY_CALL_NO = dISPLAY_CALL_NO;
        ITEM_ENUM = iTEM_ENUM;
        OVERSIZE = o;
        YEAR = year;
        CHRON = chron;
        LOCATION_NAME = location_name;
        ITEM_STATUS_DESC = item_status_desc;
        ITEM_STATUS_DATE = item_status_date;
        SUPPRESS_IN_OPAC = item_suppress;
        CALL_NO_TYPE = call_no_type;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Report other = (Report) obj;
        if (ITEM_BARCODE == null) {
            if (other.ITEM_BARCODE != null) {
                return false;
            }
        } else if (!ITEM_BARCODE.equals(other.ITEM_BARCODE)) {
            return false;
        }
        return true;
    }

    public String getCHRON() {
        return CHRON;
    }

    public String getChron() {
        return CHRON;
    }

    public String getDISPLAY_CALL_NO() {
        return DISPLAY_CALL_NO;
    }

    public String getDisplayCallNo() {
        return DISPLAY_CALL_NO;
    }

    public String getENCODING_LEVEL() {
        return ENCODING_LEVEL;
    }

    public String getEncodingLevel() {
        return ENCODING_LEVEL;
    }

    public String getITEM_BARCODE() {
        return ITEM_BARCODE;
    }

    public String getItemBarcode() {
        return ITEM_BARCODE;
    }

    public String getITEM_ENUM() {
        return ITEM_ENUM;
    }

    public String getItemEnum() {
        return ITEM_ENUM;
    }

    public String getITEM_ID() {
        return ITEM_ID;
    }

    public String getItemId() {
        return ITEM_ID;
    }

    public Date getITEM_STATUS_DATE() {
        return ITEM_STATUS_DATE;
    }

    public Date getItemStatusDate() {
        return ITEM_STATUS_DATE;
    }

    public String getITEM_STATUS_DESC() {
        return ITEM_STATUS_DESC;
    }

    public String getItemStatusDesc() {
        return ITEM_STATUS_DESC;
    }

    public String getLOCATION_NAME() {
        return LOCATION_NAME;
    }

    public String getLocationName() {
        return LOCATION_NAME;
    }

    public String getMarker() {
        return marker;
    }

    public String getMFHD_ID() {
        return MFHD_ID;
    }

    public String getNORMALIZED_CALL_NO() {
        return NORMALIZED_CALL_NO;
    }

    public String getNormalizedCallNo() {
        return NORMALIZED_CALL_NO;
    }

    public String getOVERSIZE() {
        return OVERSIZE;
    }

    public String getOversize() {
        return OVERSIZE;
    }

    public String getPrior() {
        return prior;
    }

    public String getPriorPhysical() {
        return priorPhysical;
    }

    public String getSUPPRESS_IN_OPAC() {
        return SUPPRESS_IN_OPAC;
    }

    public String getSuppressInOpac() {
        return SUPPRESS_IN_OPAC;
    }

    public Integer getText() {
        return text;
    }

    public String getYEAR() {
        return YEAR;
    }

    public String getYear() {
        return YEAR;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((ITEM_BARCODE == null) ? 0 : ITEM_BARCODE.hashCode());
        return result;
    }

    public void setCHRON(String cHRON) {
        CHRON = cHRON;
    }

    public void setDISPLAY_CALL_NO(String dISPLAY_CALL_NO) {
        DISPLAY_CALL_NO = dISPLAY_CALL_NO;
    }

    public void setENCODING_LEVEL(String eNCODING_LEVEL) {
        ENCODING_LEVEL = eNCODING_LEVEL;
    }

    public void setITEM_BARCODE(String iTEM_BARCODE) {
        ITEM_BARCODE = iTEM_BARCODE;
    }

    public void setITEM_ENUM(String iTEM_ENUM) {
        ITEM_ENUM = iTEM_ENUM;
    }

    public void setITEM_ID(String iTEM_ID) {
        ITEM_ID = iTEM_ID;
    }

    public void setITEM_STATUS_DATE(Date iTEM_STATUS_DATE) {
        ITEM_STATUS_DATE = iTEM_STATUS_DATE;
    }

    public void setITEM_STATUS_DESC(String iTEM_STATUS_DESC) {
        ITEM_STATUS_DESC = iTEM_STATUS_DESC;
    }

    public void setLOCATION_NAME(String lOCATION_NAME) {
        LOCATION_NAME = lOCATION_NAME;
    }

    public void setMarker(String marker) {
        this.marker = marker;
    }

    public void setMFHD_ID(String mFHD_ID) {
        MFHD_ID = mFHD_ID;
    }

    public void setNORMALIZED_CALL_NO(String nORMALIZED_CALL_NO) {
        NORMALIZED_CALL_NO = nORMALIZED_CALL_NO;
    }

    public void setOVERSIZE(String oVERSIZE) {
        OVERSIZE = oVERSIZE;
    }

    public void setPrior(String prior) {
        this.prior = prior;
    }

    public void setPriorPhysical(String priorPhysical) {
        this.priorPhysical = priorPhysical;
    }

    public OrbisRecord getOrbisRecord() {
        return orbisRecord;
    }

    public void setOrbisRecord(OrbisRecord orbisRecord) {
        this.orbisRecord = orbisRecord;
    }

    public void setSUPPRESS_IN_OPAC(String sUPPRESS_IN_OPAC) {
        SUPPRESS_IN_OPAC = sUPPRESS_IN_OPAC;
    }

    public void setText(Integer text) {
        if (text < 0) {
            text = text * -1;
        }
        this.text = text;
    }

    public void setYEAR(String yEAR) {
        YEAR = yEAR;
    }

    public String getCALL_NO_TYPE() {
        return CALL_NO_TYPE;
    }

    public void setCALL_NO_TYPE(String cALL_NO_TYPE) {
        CALL_NO_TYPE = cALL_NO_TYPE;
    }

    @Deprecated
    public String printBarcodesString() {
        return "Report [" + ITEM_BARCODE + "]";
    }

    @Override
    public String toString() {
        return "Report{" +
                "CHRON='" + CHRON + '\'' +
                ", DISPLAY_CALL_NO='" + DISPLAY_CALL_NO + '\'' +
                ", ENCODING_LEVEL='" + ENCODING_LEVEL + '\'' +
                ", ITEM_BARCODE='" + ITEM_BARCODE + '\'' +
                ", ITEM_ENUM='" + ITEM_ENUM + '\'' +
                ", ITEM_ID='" + ITEM_ID + '\'' +
                ", ITEM_STATUS_DATE=" + ITEM_STATUS_DATE +
                ", ITEM_STATUS_DESC='" + ITEM_STATUS_DESC + '\'' +
                ", LOCATION_NAME='" + LOCATION_NAME + '\'' +
                ", MFHD_ID='" + MFHD_ID + '\'' +
                ", NORMALIZED_CALL_NO='" + NORMALIZED_CALL_NO + '\'' +
                ", OVERSIZE='" + OVERSIZE + '\'' +
                ", SUPPRESS_IN_OPAC='" + SUPPRESS_IN_OPAC + '\'' +
                ", YEAR='" + YEAR + '\'' +
                ", CALL_NO_TYPE='" + CALL_NO_TYPE + '\'' +
                ", orbisRecord=" + orbisRecord +
                ", priorEnum='" + priorEnum + '\'' +
                ", priorChron='" + priorChron + '\'' +
                ", priorPhysicalEnum='" + priorPhysicalEnum + '\'' +
                ", priorPhysicalChron='" + priorPhysicalChron + '\'' +
                ", physicalPrior=" + physicalPrior +
                ", prior='" + prior + '\'' +
                ", priorPhysical='" + priorPhysical + '\'' +
                ", marker='" + marker + '\'' +
                ", text=" + text +
                '}';
    }

    public Integer getMark() {
        return mark;
    }

    public void setMark(final Integer mark) {
        this.mark = mark;
    }
}
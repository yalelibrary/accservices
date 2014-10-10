package edu.yale.sml.model;

import lombok.ToString;

import java.io.Serializable;
import java.sql.Date;

@ToString(callSuper=true, includeFieldNames=true)
public class OrbisRecord implements Serializable {

    private static final long serialVersionUID = -6792300395330658557L;

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

    private String PERM_LOCATION = "";

    private String prior = "N/A";

    private String SUPPRESS_IN_OPAC = "";

    private String YEAR = "";

    private String CALL_NO_TYPE = "N/A";

    public OrbisRecord() {
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        OrbisRecord other = (OrbisRecord) obj;
        if (ITEM_BARCODE == null) {
            if (other.ITEM_BARCODE != null) {
                return false;
            }
        } else if (!ITEM_BARCODE.equals(other.ITEM_BARCODE)) {
            return false;
        }
        return true;
    }

    public String getCALL_NO_TYPE() {
        return CALL_NO_TYPE;
    }

    public String getCallNoType() {
        return CALL_NO_TYPE;
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

    public String getItemID() {
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

    public String getMFHD_ID() {
        return MFHD_ID;
    }

    public String getMfhd() {
        return MFHD_ID;
    }

    public String getNORMALIZED_CALL_NO() {
        return NORMALIZED_CALL_NO;
    }

    public String getNormalizedCallNo() {
        return NORMALIZED_CALL_NO;
    }

    public String getPERM_LOCATION() {
        return PERM_LOCATION;
    }

    public String getPermLocation() {
        return PERM_LOCATION;
    }

    public String getPrior() {
        return prior;
    }

    public String getSUPPRESS_IN_OPAC() {
        return SUPPRESS_IN_OPAC;
    }

    public String getSuppressInOpac() {
        return SUPPRESS_IN_OPAC;
    }

    public String getYEAR() {
        return YEAR;
    }

    public String getYear() {
        return YEAR;
    }

    public void setCHRON(String chron) {
        CHRON = chron;
    }

    public void setDISPLAY_CALL_NO(String displayCallNum) {
        DISPLAY_CALL_NO = displayCallNum;
    }

    public void setENCODING_LEVEL(String level) {
        ENCODING_LEVEL = level;
    }

    public void setITEM_BARCODE(String barcode) {
        ITEM_BARCODE = barcode;
    }

    public void setITEM_ENUM(String itemEnum) {
        ITEM_ENUM = itemEnum;
    }

    public void setITEM_ID(String itemId) {
        ITEM_ID = itemId;
    }

    public void setITEM_STATUS_DATE(Date itemStatusDate) {
        ITEM_STATUS_DATE = itemStatusDate;
    }

    public void setITEM_STATUS_DESC(String itemStatusDesc) {
        ITEM_STATUS_DESC = itemStatusDesc;
    }

    public void setLOCATION_NAME(String locationName) {
        LOCATION_NAME = locationName;
    }

    public void setMFHD_ID(String mfhdId) {
        MFHD_ID = mfhdId;
    }

    public void setNORMALIZED_CALL_NO(String normCallNum) {
        NORMALIZED_CALL_NO = normCallNum;
    }

    public void setSUPPRESS_IN_OPAC(String supp) {
        SUPPRESS_IN_OPAC = supp;
    }

    public String getInfo() {
        return "OrbisRecord [NORMALIZED_CALL_NO=" + NORMALIZED_CALL_NO + ", ITEM_ENUM=" + ITEM_ENUM + ", ITEM_BARCODE=" + ITEM_BARCODE + ", CHRON=" + CHRON + " ]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((ITEM_BARCODE == null) ? 0 : ITEM_BARCODE.hashCode());
        return result;
    }

    public void setCALL_NO_TYPE(String cnType) {
        if (cnType == null)
            CALL_NO_TYPE = "N/A";
        else {
            CALL_NO_TYPE = cnType;
        }
    }
}
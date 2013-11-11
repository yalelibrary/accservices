package edu.yale.sml.model;

import java.io.Serializable;
import java.sql.Date;

public class OrbisRecord implements Serializable
{

    private static final long serialVersionUID = -6792300395330658557L;

    String CHRON = "";
    String DISPLAY_CALL_NO = "";
    String ENCODING_LEVEL;
    String ITEM_BARCODE = "";
    String ITEM_ENUM = "";
    String ITEM_ID = "";
    Date ITEM_STATUS_DATE = null;
    String ITEM_STATUS_DESC = "";
    String LOCATION_NAME = "";
    String MFHD_ID = "";
    String NORMALIZED_CALL_NO = "";
    String PERM_LOCATION = "";
    String prior = "N/A";
    String SUPPRESS_IN_OPAC = "";
    String YEAR = "";
    String CALL_NO_TYPE = "N/A";

    public OrbisRecord()
    {
    }

    // TODO remove
    public OrbisRecord(String string)
    {
        this.ITEM_BARCODE = string;
    }

    public String getCHRON_VALUE()
    {
        return CHRON;
    }

    //TODO remove, along with getCHRON_VALUE (no longer modified)
    public String getENUM_VALUE()
    {
        return ITEM_ENUM;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        OrbisRecord other = (OrbisRecord) obj;
        if (ITEM_BARCODE == null)
        {
            if (other.ITEM_BARCODE != null)
            {
                return false;
            }
        }
        else if (!ITEM_BARCODE.equals(other.ITEM_BARCODE))
        {
            return false;
        }
        return true;
    }

    public String getCALL_NO_TYPE()
    {
        return CALL_NO_TYPE;
    }

    public String getCHRON()
    {
        return CHRON;
    }

    public String getDISPLAY_CALL_NO()
    {
        return DISPLAY_CALL_NO;
    }

    public String getENCODING_LEVEL()
    {
        return ENCODING_LEVEL;
    }

    public String getInfo()
    {
        return "OrbisRecord [NORMALIZED_CALL_NO=" + NORMALIZED_CALL_NO + ", ITEM_ENUM=" + ITEM_ENUM + ", ITEM_BARCODE=" + ITEM_BARCODE + ", CHRON=" + CHRON + " ]";
    }

    public String getITEM_BARCODE()
    {
        return ITEM_BARCODE;
    }

    public String getITEM_ENUM()
    {
        return ITEM_ENUM;
    }

    public String getITEM_ID()
    {
        return ITEM_ID;
    }

    public Date getITEM_STATUS_DATE()
    {
        return ITEM_STATUS_DATE;
    }

    public String getITEM_STATUS_DESC()
    {
        return ITEM_STATUS_DESC;
    }

    public String getLOCATION_NAME()
    {
        return LOCATION_NAME;
    }

    public String getMFHD_ID()
    {
        return MFHD_ID;
    }

    public String getNORMALIZED_CALL_NO()
    {
        return NORMALIZED_CALL_NO;
    }

    public String getPERM_LOCATION()
    {
        return PERM_LOCATION;
    }

    public String getPrior()
    {
        return prior;
    }

    public String getSUPPRESS_IN_OPAC()
    {
        return SUPPRESS_IN_OPAC;
    }

    public String getYEAR()
    {
        return YEAR;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((ITEM_BARCODE == null) ? 0 : ITEM_BARCODE.hashCode());
        return result;
    }

    public void setCALL_NO_TYPE(String cALL_NO_TYPE)
    {

        if (cALL_NO_TYPE == null)
            CALL_NO_TYPE = "N/A";
        else
        { 
            CALL_NO_TYPE = cALL_NO_TYPE;
        }
    }

    public void setCHRON(String cHRON)
    {
        CHRON = cHRON;
    }

    public void setDISPLAY_CALL_NO(String display_call_number)
    {
        DISPLAY_CALL_NO = display_call_number;
    }

    public void setENCODING_LEVEL(String level)
    {
        ENCODING_LEVEL = level;
    }

    public void setITEM_BARCODE(String barcode)
    {
        ITEM_BARCODE = barcode;
    }

    public void setITEM_ENUM(String item_enum)
    {
        ITEM_ENUM = item_enum;
    }

    public void setITEM_ID(String item_id)
    {
        ITEM_ID = item_id;
    }

    public void setITEM_STATUS_DATE(Date item_status_date)
    {
        ITEM_STATUS_DATE = item_status_date;
    }

    public void setITEM_STATUS_DESC(String item_status_desc)
    {
        ITEM_STATUS_DESC = item_status_desc;
    }

    public void setLOCATION_NAME(String location_name)
    {
        LOCATION_NAME = location_name;
    }

    public void setMFHD_ID(String mFHD_ID)
    {
        MFHD_ID = mFHD_ID;
    }

    public void setNORMALIZED_CALL_NO(String norm_call_number)
    {
        NORMALIZED_CALL_NO = norm_call_number;
    }

    public void setPERM_LOCATION(String perm_location)
    {
        PERM_LOCATION = perm_location;
    }

    public void setPrior(String prior)
    {
        this.prior = prior;
    }

    public void setSUPPRESS_IN_OPAC(String supp)
    {
        SUPPRESS_IN_OPAC = supp;
    }

    public void setYEAR(String year)
    {
        YEAR = year;
    }

    @Override
    public String toString()
    {
        return "OrbisRecord [CHRON=" + CHRON + ", DISPLAY_CALL_NO=" + DISPLAY_CALL_NO + ", ENCODING_LEVEL=" + ENCODING_LEVEL + ", ITEM_BARCODE=" + ITEM_BARCODE + ", ITEM_ENUM=" + ITEM_ENUM + ", ITEM_ID=" + ITEM_ID + ", ITEM_STATUS_DATE=" + ITEM_STATUS_DATE + ", ITEM_STATUS_DESC=" + ITEM_STATUS_DESC
                + ", LOCATION_NAME=" + LOCATION_NAME + ", MFHD_ID=" + MFHD_ID + ", NORMALIZED_CALL_NO=" + NORMALIZED_CALL_NO + ", PERM_LOCATION=" + PERM_LOCATION + ", prior=" + prior + ", SUPPRESS_IN_OPAC=" + SUPPRESS_IN_OPAC + ", YEAR=" + YEAR + ", CALL_NO_TYPE=" + CALL_NO_TYPE + "]";
    }
}
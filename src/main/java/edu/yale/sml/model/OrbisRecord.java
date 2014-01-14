package edu.yale.sml.model;

import java.io.Serializable;
import java.sql.Date;

public class OrbisRecord implements Serializable
{

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

    public OrbisRecord()
    {
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

    public String getInfo()
    {
        return "OrbisRecord [NORMALIZED_CALL_NO=" + NORMALIZED_CALL_NO + ", ITEM_ENUM=" + ITEM_ENUM + ", ITEM_BARCODE=" + ITEM_BARCODE + ", CHRON=" + CHRON + " ]";
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


    //Shorthand for getter. All in CAPS.

    public String CALL_NO_TYPE()
    {
        return getCALL_NO_TYPE();
    }

    public String CHRON()
    {
        return getCHRON();
    }

    public String DISPLAY_CALL_NO()
    {
        return getDISPLAY_CALL_NO();
    }

    public String ENCODING_LEVEL()
    {
        return getENCODING_LEVEL();
    }

    public String BARCODE()
    {
        return getITEM_BARCODE();
    }

    public String ENUM()
    {
        return getITEM_ENUM();
    }

    public String ITEM_ID()
    {
        return getITEM_ID();
    }

    public Date ITEM_STATUS_DATE()
    {
        return getITEM_STATUS_DATE();
    }

    public String ITEM_STATUS_DESC()
    {
        return getITEM_STATUS_DESC();
    }

    public String LOCATION_NAME()
    {
        return  getLOCATION_NAME();
    }

    public String MFHD_ID()
    {
        return getMFHD_ID();
    }

    public String NORMALIZED_CALL()
    {
        return getNORMALIZED_CALL_NO();
    }

    public String PERM_LOCATION()
    {
        return getPERM_LOCATION();
    }

    public String PRIOR()
    {
        return getPrior();
    }

    public String SUPPRESS()
    {
        return getSUPPRESS_IN_OPAC();
    }

    public String YEAR()
    {
        return getYEAR();
    }
}
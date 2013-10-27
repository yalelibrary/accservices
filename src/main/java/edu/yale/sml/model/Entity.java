package edu.yale.sml.model;

import java.sql.Date;

public class Entity
{

	protected String CHRON = "";
	protected String DISPLAY_CALL_NO = "";
	protected String ENCODING_LEVEL;
	protected String ITEM_BARCODE = "";
	protected String ITEM_ENUM = "";
	protected String ITEM_ID = "";
	protected Date ITEM_STATUS_DATE = null;
	protected String ITEM_STATUS_DESC = "";
	protected String LOCATION_NAME = "";
	protected String MFHD_ID = "";
	protected String NORMALIZED_CALL_NO = "";
	protected String OVERSIZE = "";
	protected String SUPPRESS_IN_OPAC;
	protected String YEAR = "";

	public Entity()
	{
		super();
	}

	public String getCHRON()
	{
		return CHRON;
	}

	public void setCHRON(String cHRON)
	{
		CHRON = cHRON;
	}

	public String getDISPLAY_CALL_NO()
	{
		return DISPLAY_CALL_NO;
	}

	public void setDISPLAY_CALL_NO(String dISPLAY_CALL_NO)
	{
		DISPLAY_CALL_NO = dISPLAY_CALL_NO;
	}

	public String getENCODING_LEVEL()
	{
		return ENCODING_LEVEL;
	}

	public void setENCODING_LEVEL(String eNCODING_LEVEL)
	{
		ENCODING_LEVEL = eNCODING_LEVEL;
	}

	public String getITEM_BARCODE()
	{
		return ITEM_BARCODE;
	}

	public void setITEM_BARCODE(String iTEM_BARCODE)
	{
		ITEM_BARCODE = iTEM_BARCODE;
	}

	public String getITEM_ENUM()
	{
		return ITEM_ENUM;
	}

	public void setITEM_ENUM(String iTEM_ENUM)
	{
		ITEM_ENUM = iTEM_ENUM;
	}

	public String getITEM_ID()
	{
		return ITEM_ID;
	}

	public void setITEM_ID(String iTEM_ID)
	{
		ITEM_ID = iTEM_ID;
	}

	public Date getITEM_STATUS_DATE()
	{
		return ITEM_STATUS_DATE;
	}

	public void setITEM_STATUS_DATE(Date iTEM_STATUS_DATE)
	{
		ITEM_STATUS_DATE = iTEM_STATUS_DATE;
	}

	public String getITEM_STATUS_DESC()
	{
		return ITEM_STATUS_DESC;
	}

	public void setITEM_STATUS_DESC(String iTEM_STATUS_DESC)
	{
		ITEM_STATUS_DESC = iTEM_STATUS_DESC;
	}

	public String getLOCATION_NAME()
	{
		return LOCATION_NAME;
	}

	public void setLOCATION_NAME(String lOCATION_NAME)
	{
		LOCATION_NAME = lOCATION_NAME;
	}

	public String getMFHD_ID()
	{
		return MFHD_ID;
	}

	public void setMFHD_ID(String mFHD_ID)
	{
		MFHD_ID = mFHD_ID;
	}

	public String getNORMALIZED_CALL_NO()
	{
		return NORMALIZED_CALL_NO;
	}

	public void setNORMALIZED_CALL_NO(String nORMALIZED_CALL_NO)
	{
		NORMALIZED_CALL_NO = nORMALIZED_CALL_NO;
	}

	public String getOVERSIZE()
	{
		return OVERSIZE;
	}

	public void setOVERSIZE(String oVERSIZE)
	{
		OVERSIZE = oVERSIZE;
	}

	public String getSUPPRESS_IN_OPAC()
	{
		return SUPPRESS_IN_OPAC;
	}

	public void setSUPPRESS_IN_OPAC(String sUPPRESS_IN_OPAC)
	{
		SUPPRESS_IN_OPAC = sUPPRESS_IN_OPAC;
	}

	public String getYEAR()
	{
		return YEAR;
	}

	public void setYEAR(String yEAR)
	{
		YEAR = yEAR;
	}

}
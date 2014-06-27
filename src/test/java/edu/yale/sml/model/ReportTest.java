package edu.yale.sml.model;

import org.junit.Test;

/**
 *
 */
public class ReportTest {
    @Test
    public void testPopulateReport() throws Exception {

    }

    @Test
    public void testGetPhysicalPrior() throws Exception {
        Report r = new Report();
        OrbisRecord o = new OrbisRecord();
        o.setDISPLAY_CALL_NO("PRIOR PHYSICAL");
        r.setPhysicalPrior(o);
        assert (r.getPhysicalPrior().equals(o));
    }

    @Test
    public void testSetPhysicalPrior() throws Exception {
        Report r = new Report();
        OrbisRecord o = new OrbisRecord();
        o.setDISPLAY_CALL_NO("PRIOR PHYSICAL");
        r.setPhysicalPrior(o);
        assert (r.getPhysicalPrior().equals(o));
    }

    @Test
    public void testGetPriorEnum() throws Exception {
        Report r = new Report();
        OrbisRecord o = new OrbisRecord();
        o.setITEM_ENUM("i");
        r.setPhysicalPrior(o);
        assert (r.getPriorEnum().equals(""));
    }

    @Test
    public void testSetPriorEnum() throws Exception {
        Report r = new Report();
        OrbisRecord o = new OrbisRecord();
        o.setITEM_ENUM("i");
        r.setPhysicalPrior(o);
        assert (r.getPriorEnum().equals(""));
    }

    @Test
    public void testGetPriorChron() throws Exception {

    }

    @Test
    public void testSetPriorChron() throws Exception {

    }

    @Test
    public void testGetPriorPhysicalEnum() throws Exception {

    }

    @Test
    public void testSetPriorPhysicalEnum() throws Exception {

    }

    @Test
    public void testGetPriorPhysicalChron() throws Exception {

    }

    @Test
    public void testSetPriorPhysicalChron() throws Exception {

    }

    @Test
    public void testEquals() throws Exception {

    }

    @Test
    public void testGetCHRON() throws Exception {

    }

    @Test
    public void testGetChron() throws Exception {

    }

    @Test
    public void testGetDISPLAY_CALL_NO() throws Exception {

    }

    @Test
    public void testGetDisplayCallNo() throws Exception {

    }

    @Test
    public void testGetENCODING_LEVEL() throws Exception {

    }

    @Test
    public void testGetEncodingLevel() throws Exception {

    }

    @Test
    public void testGetITEM_BARCODE() throws Exception {

    }

    @Test
    public void testGetItemBarcode() throws Exception {

    }

    @Test
    public void testGetITEM_ENUM() throws Exception {

    }

    @Test
    public void testGetItemEnum() throws Exception {

    }

    @Test
    public void testGetITEM_ID() throws Exception {

    }

    @Test
    public void testGetItemId() throws Exception {

    }

    @Test
    public void testGetITEM_STATUS_DATE() throws Exception {

    }

    @Test
    public void testGetItemStatusDate() throws Exception {

    }

    @Test
    public void testGetITEM_STATUS_DESC() throws Exception {

    }

    @Test
    public void testGetItemStatusDesc() throws Exception {

    }

    @Test
    public void testGetLOCATION_NAME() throws Exception {

    }

    @Test
    public void testGetLocationName() throws Exception {

    }

    @Test
    public void testGetMarker() throws Exception {

    }

    @Test
    public void testGetMFHD_ID() throws Exception {

    }

    @Test
    public void testGetNORMALIZED_CALL_NO() throws Exception {

    }

    @Test
    public void testGetNormalizedCallNo() throws Exception {

    }

    @Test
    public void testGetOVERSIZE() throws Exception {

    }

    @Test
    public void testGetOversize() throws Exception {

    }

    @Test
    public void testGetPrior() throws Exception {

    }

    @Test
    public void testGetPriorPhysical() throws Exception {

    }

    @Test
    public void testGetSUPPRESS_IN_OPAC() throws Exception {

    }

    @Test
    public void testGetSuppressInOpac() throws Exception {

    }

    @Test
    public void testGetText() throws Exception {

    }

    @Test
    public void testGetYEAR() throws Exception {

    }

    @Test
    public void testGetYear() throws Exception {

    }

    @Test
    public void testHashCode() throws Exception {

    }

    @Test
    public void testSetCHRON() throws Exception {

    }

    @Test
    public void testSetDISPLAY_CALL_NO() throws Exception {

    }

    @Test
    public void testSetENCODING_LEVEL() throws Exception {

    }

    @Test
    public void testSetITEM_BARCODE() throws Exception {

    }

    @Test
    public void testSetITEM_ENUM() throws Exception {

    }

    @Test
    public void testSetITEM_ID() throws Exception {

    }

    @Test
    public void testSetITEM_STATUS_DATE() throws Exception {

    }

    @Test
    public void testSetITEM_STATUS_DESC() throws Exception {

    }

    @Test
    public void testSetLOCATION_NAME() throws Exception {

    }

    @Test
    public void testSetMarker() throws Exception {

    }

    @Test
    public void testSetMFHD_ID() throws Exception {

    }

    @Test
    public void testSetNORMALIZED_CALL_NO() throws Exception {

    }

    @Test
    public void testSetOVERSIZE() throws Exception {

    }

    @Test
    public void testSetPrior() throws Exception {

    }

    @Test
    public void testSetPriorPhysical() throws Exception {

    }

    @Test
    public void testGetOrbisRecord() throws Exception {

    }

    @Test
    public void testSetOrbisRecord() throws Exception {

    }

    @Test
    public void testSetSUPPRESS_IN_OPAC() throws Exception {

    }

    @Test
    public void testSetText() throws Exception {

    }

    @Test
    public void testSetYEAR() throws Exception {

    }

    @Test
    public void testGetCALL_NO_TYPE() throws Exception {

    }

    @Test
    public void testSetCALL_NO_TYPE() throws Exception {

    }

    @Test
    public void testPrintBarcodesString() throws Exception {

    }

    @Test
    public void testToString() throws Exception {

    }
}

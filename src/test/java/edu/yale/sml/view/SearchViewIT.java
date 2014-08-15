package edu.yale.sml.view;

import edu.yale.sml.logic.BasicShelfScanEngine;
import edu.yale.sml.model.DataLists;
import edu.yale.sml.model.Report;
import edu.yale.sml.persistence.BarcodeSearchDAO;
import edu.yale.sml.persistence.config.HibernateOracleUtils;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;


public class SearchViewIT {

    private static final Logger logger = LoggerFactory.getLogger(SearchViewIT.class);

    //Expected results to match against:
    public static final String TEST_PRIOR_PHYSICAL = "PR6003.U13 Z593 2013";
    private static final String TEST_INVALID_STATUS_BARCODE = "39002016619729";
    private static final int TEST_OVERSIZE = 2;
    private static final String TEST_FIRST_CALL = "PR6005 O58 B5";
    private static final int TEST_ORBIS_LIST_SIZE = 225;
    public static final int TEST_CULPRIT_LIST_SIZE = 51;
    private static DataLists dataLists = null;

    /**
     * Get data from Voyager once, and if unable ignore the test
     */
    @Before
    public void init() {
        Assume.assumeNotNull(dataLists);

        try {
            if (dataLists != null) { //get once
                BasicShelfScanEngine engine = new BasicShelfScanEngine();
                engine.setBarcodeSearchDAO(new BarcodeSearchDAO());
                dataLists = engine.process(barcodeList(), "sml", new Date(), "N");
            }
        } catch (Exception e) {
            logger.error("Error init", e.getMessage());
        }
    }

    @After
    public void terminate() {
        HibernateOracleUtils.shutdown();
        logger.debug("Closed Oracle Session Factory");
    }

    @Test
    public void shouldEqualOversize() {
        assertEquals("Oversize number not equal.", dataLists.getShelvingError().getOversize_errors(), TEST_OVERSIZE);
    }

    /**
     * Tests against Voyager reports DB a known barcode TEST_INVALID_STATUS_BARCODE (39002016619729)
     * with a more recent 'valid' status (not charged). an 'invalid' status(e.g. withdrawn in this case)
     * is supposed to trump this status.
     */
    @Test
    public void shouldEqualItemStatus() {
        //TODO define valid/invalid in an enum
        assertEquals("Should have returned 2 specified statuses for this barcode",
                dataLists.getBarcodesAsMap().get(TEST_INVALID_STATUS_BARCODE), new ArrayList(Arrays.asList("Not Charged", "Withdrawn")));
        Report item = findBarcodeItem(TEST_INVALID_STATUS_BARCODE, dataLists.getCulpritList());
        assertEquals("Item status don't match. Expected an invalid status",item.getITEM_STATUS_DESC(), "Withdrawn");
    }

    /**
     * Tests size of orbis records and error items. The latter is more important.
     */
    @Test
    public void shouldGetListSize() {
        assertEquals("Orbis List size doesn't match.", dataLists.getCatalogAsList().size(), TEST_ORBIS_LIST_SIZE);
        assertEquals("Culprit list size doesn't match.", dataLists.getCulpritList().size(), TEST_CULPRIT_LIST_SIZE);
    }

    /**
     * Tests suppress for single item.
     * Note: Voyager reportsDB dependent: an items suppress status might be updated, perhaps.
     */
    @Test
    public void shouldEqualSuppressStatus() {
        Report item = dataLists.getCulpritList().get(2);
        assertEquals("Item is supposed to be suppressed.", item.getSUPPRESS_IN_OPAC(), "Y");
    }

    @Test
    public void shouldEqualCNType() {
        Report item = dataLists.getCulpritList().get(2);
        assertEquals("Item is supposed have CN type 8.", item.getCALL_NO_TYPE(), "8");
    }

    @Test
    public void shouldEqualCallNum() {
        logger.debug("First Call Num." + dataLists.getCatalogAsList().get(0).getDISPLAY_CALL_NO());
        assertEquals("First call num mismatch", dataLists.getCatalogAsList().get(0).getDISPLAY_CALL_NO(), TEST_FIRST_CALL);
    }

    @Test
    public void shouldEqualPrior() {
        Report item = dataLists.getCulpritList().get(2);
        assertEquals("Prior physical call num. doesn't match:", item.getPriorPhysical(), TEST_PRIOR_PHYSICAL);
        assert (item.getPriorPhysical().equals(item.getPrior()));
    }

    public Report findBarcodeItem(String barcode, List<Report> list) {
        for (Report t : list) {
            if (t.getITEM_BARCODE().equals(barcode)) {
                return t;
            }
        }
        return null;
    }

    /**
     * Gets input for test
     * @return List of barcodes or null if an exception occurs
     */
    public static List<String> barcodeList() {
        try {
            return readBarcodeFileFromSysProperty();
        } catch (Exception e) {
            logger.debug("Item not found", e);
            return null;
        }
    }

    /** reads from sys property inputFile */
    public static List<String> readBarcodeFileFromSysProperty() throws Exception {
        return FileUtils.readLines(new java.io.File("src/test/resources/AccuracyBarcodes11-28-12.txt"), "UTF-8");
    }

}
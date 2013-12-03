package edu.yale.sml;

import com.thoughtworks.xstream.XStream;
import edu.yale.sml.logic.BasicShelfScanEngine;
import edu.yale.sml.model.DataLists;
import edu.yale.sml.model.History;
import edu.yale.sml.model.Report;
import edu.yale.sml.persistence.config.HibernateOracleUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.After;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import edu.yale.sml.view.SearchView;


public class SearchViewIT {

    public static final String TEST_PRIOR_PHYSICAL = "PR6003.U13 Z593 2013";
    final private static String TEST_INVALID_STATUS_BARCODE = "39002016619729";
    final private static int TEST_OVERSIZE = 2;
    final private static String TEST_FIRST_CALL = "PR6005 O58 B5";
    final private static int TEST_ORBIS_LIST_SIZE = 225;
    public static final int TEST_CULPRIT_LIST_SIZE = 51;
    public static final int TEST_HISTORY_REPORT_ID = 22;
    public static final int TEST_REPORT_LIST_SIZE = 309;
    public static final String TEST_SERIALIZED_FILE_NAME = "AccuracyBarcodes11-28-12.txt";


    final private Logger logger = LoggerFactory.getLogger(SearchViewIT.class);
    static private DataLists dataLists;
    /**
     * Get data from Voyager once
     */
    static  {
        BasicShelfScanEngine engine = new BasicShelfScanEngine();
        //will obtain DAO connection in the method itself
        try {
            // NOTE not process...no way to get individual data once... this shows why .process() should not
            // be a data gatherer, but a true processor. now have to replicate this in each test (resulting in many
            // connections.
            dataLists = engine.process(populateInput(), "sml", new java.util.Date(), "N");

            //TODO xml (or use .properties to match expected i/o)
            XStream x = new XStream();
            String s = x.toXML(dataLists);
            x.toXML(dataLists, new java.io.FileWriter("/tmp/xml.txt"));

        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    /**
     * Tests oversize
     */
    @Test
    public void testOverSize() {
        assertEquals("Oversize number not equal.", dataLists.getShelvingError().getOversize_errors(), TEST_OVERSIZE);
    }

    /**
     * Tests against Voyager reports DB a known barcode TEST_INVALID_STATUS_BARCODE (39002016619729)
     * with a more recent 'valid' status (not charged). an 'invalid' status(e.g. withdrawn in this case)
     * is supposed to trump this status.
     */
    @Test
    public void testItemStatusLogic() {
        //TODO define valid/invalid in an enum
        assertEquals("Should have returned 2 specified statuses for this barcode",
                dataLists.getBarcodesAsMap().get(TEST_INVALID_STATUS_BARCODE),
                new java.util.ArrayList(java.util.Arrays.asList("Not Charged", "Withdrawn")));
        Report item = findBarcodeItem(TEST_INVALID_STATUS_BARCODE, dataLists.getCulpritList());

        //TODO also test the assumption that withdrawn status is less recent.

        assertEquals("Item status don't match. Should have been an invalid status",
                item.getITEM_STATUS_DESC(),"Withdrawn");
    }

    /**
     * Tests size of orbis records and error items. The latter is more important.
     */
    @Test
    public void testListSize() {
        assertEquals("Orbis List size doesn't match.", dataLists.getCatalogAsList().size(), TEST_ORBIS_LIST_SIZE);
        assertEquals("Culprit list size doesn't match.", dataLists.getCulpritList().size(), TEST_CULPRIT_LIST_SIZE);
    }

    /**
     * Tests suppress for single item.
     *
     * Note: Voyager reportsDB dependent
     */
    @Test
    public void testItemSuppress() {
        Report item = dataLists.getCulpritList().get(2);
        assertEquals("Item is supposed to be suppressed.", item.getSUPPRESS_IN_OPAC(), "Y");
    }

    @Test
    public void testItemCNType() {
        Report item = dataLists.getCulpritList().get(2);
        assertEquals("Item is supposed have CN type 8.", item.getCALL_NO_TYPE(), "8");
    }

    @Test
    public void testCallNumbers() {
        logger.debug("First Call Num." + dataLists.getCatalogAsList().get(0).getDISPLAY_CALL_NO());
        assertEquals("First call num mismatch", dataLists.getCatalogAsList().get(0).getDISPLAY_CALL_NO(), TEST_FIRST_CALL);
    }

    @Test
    public void testPriors() {
         Report item = dataLists.getCulpritList().get(2);
         assertEquals("Prior physical call num. doesn't match:",
                 item.getPriorPhysical(),
                 TEST_PRIOR_PHYSICAL);
        //TODO clean up Report structure
        assert(item.getPriorPhysical().equals(item.getPrior()));
    }

    /**
     * Checks backward compatibility since serialization is used
     */
    @Test
    public void checkSerialization() {
        History historyCatalog = new History();
        HistoryHibernateTestDAO historyDAO = new HistoryHibernateTestDAO();
        try
        {
            historyCatalog = historyDAO.findById(TEST_HISTORY_REPORT_ID).get(0);

            SearchView savedSearchViewObject = (SearchView) SerializationUtils.deserialize(historyCatalog
                    .getSEARCHVIEW());
            assertEquals("Serialized FileName not equal:", savedSearchViewObject.getFileName(), TEST_SERIALIZED_FILE_NAME);
            assertEquals("Serialized Num barcodes different:", savedSearchViewObject.getEngine().getReportLists().getCatalogAsList().size(), TEST_REPORT_LIST_SIZE);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            if (e.getCause().getClass().equals(java.io.InvalidClassException.class)) {
                logger.error("Fatal Error", e);
                fail("Class serialization mismatch!!");
            }
        }
    }
    @After
    public void terminate() {
        HibernateOracleUtils.shutdown();//TODO check
        logger.debug("Closed Oracle Session Factory");
    }

    /**
     *  Replace with Report.find or some sort of BarcodeUtils
     *  The problem is that Report and OrbisRecord are disparate
     */
    public Report findBarcodeItem(String barcode, List<Report> list)
    {
         for (Report t: list)
         {
             if (t.getITEM_BARCODE().equals(barcode)) {
                 return t;
             }
         }
         return null;
    }


    /**
     * Utility
     * @return
     */
    public static List<String> populateInput() {
        //TODO read from db?
        //TODO hold in static
        try {
            return readBarcodeFileFromSysProperty();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Utility
     * @return
     * @throws Exception
     */
    public static List<String> readBarcodeFileFromSysProperty() throws Exception
    {
           return FileUtils.readLines(new java.io.File(System.getProperty("inputFile")), "UTF-8");
    }

}
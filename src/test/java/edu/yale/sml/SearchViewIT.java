package edu.yale.sml;

import com.thoughtworks.xstream.XStream;
import edu.yale.sml.logic.BasicShelfScanEngine;
import edu.yale.sml.model.DataLists;
import edu.yale.sml.model.History;
import edu.yale.sml.persistence.config.HibernateOracleUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import edu.yale.sml.view.SearchView;


public class SearchViewIT {

    final private Logger logger = LoggerFactory.getLogger(SearchViewIT.class);

    @Before
    public void acquireConnection() {
    }

    @After
    public void terminate() {
        HibernateOracleUtils.shutdown();//TODO check
        logger.debug("Closed Oracle Session Factory");
    }

    @Test
    public void testOverSize() {
        logger.debug("Testing oversize");
    }

    @Test
    public void testLocation() {
    }

    @Test
    public void testItemStatus() {
    }

    @Test
    public void testMisshelf() {

    }

    /**
     *
     * Checks backward compatibility since serialization is used
     */
    @Test
    public void checkSerialization() {
        History historyCatalog = new History();
        HistoryHibernateTestDAO historyDAO = new HistoryHibernateTestDAO();
        final Integer HISTORY_ID = 22;  //TODO generate random
        try
        {
            logger.debug("Testing serialization"); //TODO use same reference. o/wise have to maintain 2 classes
            historyCatalog = historyDAO.findById(HISTORY_ID).get(0);

            SearchView savedSearchViewObject = (SearchView) SerializationUtils.deserialize(historyCatalog
                    .getSEARCHVIEW());
            assertEquals("Serialized FileName not equal:", savedSearchViewObject.getFileName(), "AccuracyBarcodes11-28-12.txt");
            assertEquals("Serialized Num barcodes different:", savedSearchViewObject.getEngine().getReportLists().getCatalogAsList().size(), 309 );
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



    @Test
    public void test() {

        BasicShelfScanEngine engine = new BasicShelfScanEngine();
        DataLists dataLists = null;
        //will obtain DAO connection in the method itself
        try {
            // NOTE not process...no way to get individual data once... this shows why .process() should not
            // be a data gatherer, but a true processor. now have to replicate this in each test (resulting in many
            // connections.

            dataLists = engine.process(populateInput(), "sml", new java.util.Date(), "N");
            //TODO xml
            XStream x = new XStream();
            String s = x.toXML(dataLists);
            x.toXML(dataLists, new java.io.FileWriter("/tmp/xml.txt"));
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        //TODO count sepeartion per file. Should it comoare against a text file?
        assertEquals("List size doesn't match.", dataLists.getCatalogAsList().size(), 237);
        logger.debug("First Call Num." + dataLists.getCatalogAsList().get(0).getDISPLAY_CALL_NO());
        assertEquals("First call num mismatch", dataLists.getCatalogAsList().get(0).getDISPLAY_CALL_NO(), "PN1995.9 P5 T6");
        //assertEquals("Accuracy errors mismatch", dataLists.getShelvingError().getAccuracy_errors(), 4);
        assertEquals("Oversize not equal.", dataLists.getShelvingError().getOversize_errors(), 0);
    }

    public List<String> populateInput() {
        //TODO read from db?
        //TODO hold in static
        try {
            return readBarcodeFileFromSysProperty();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public static List<String> readBarcodeFileFromSysProperty() throws Exception
    {
           return FileUtils.readLines(new java.io.File(System.getProperty("inputFile")), "UTF-8");
    }

}
package edu.yale.sml.view;

import edu.yale.sml.model.History;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;
import org.slf4j.Logger;

import java.io.FileInputStream;
import java.io.ObjectInputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.slf4j.LoggerFactory.getLogger;


public class SerializationCheckIT {

    private static final Logger logger = getLogger(SerializationCheckIT.class);

    /** Expected report list size */
    private static final int REPORT_SIZE = 309;

    /** Expected file name */
    private static final String FILE_NAME = "AccuracyBarcodes11-28-12.txt";

    /**
     * Checks backward compatibility for a particular report. (Serialization is used to persist reports.)
     */
    @Test
    public void shouldDeserializeOlderReport() {
        try {
            final History historyCatalog = readFromDisk();
            final SearchView deserialized = (SearchView) SerializationUtils.deserialize(historyCatalog.getSEARCHVIEW());

            assertEquals("Serialized file name does not match",deserialized.getFileName(), FILE_NAME);
            assertEquals("Mismatch", deserialized.getEngine().getReportLists().getCatalogAsList().size(), REPORT_SIZE); //309
        } catch (Exception e) {
            if (e.getCause().getClass().equals(java.io.InvalidClassException.class)) {
                logger.error("Fatal Error", e);
                fail("Class serialization mismatch!!");
            }
            fail("Error" + e.getMessage());
        }
    }

    /** Reads object from resources */
    private History readFromDisk() {
        try {
            FileInputStream fin = new FileInputStream("src/test/resources/history.ser");
            ObjectInputStream ois = new ObjectInputStream(fin);
            History history = (History) ois.readObject();
            return history;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
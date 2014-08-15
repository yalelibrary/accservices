package edu.yale.sml.logic;

import edu.yale.sml.model.DataLists;
import edu.yale.sml.model.Report;
import edu.yale.sml.persistence.BarcodeSearchDAO;
import edu.yale.sml.persistence.config.HibernateOracleUtils;
import edu.yale.sml.view.NullFileException;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class MisshelfErrrorsProcessorTest {

    private Logger logger = LoggerFactory.getLogger(this.getClass());



    @Test
    public void shouldProcessMisshelfs(){
        BasicShelfScanEngine basicShelfScanEngine = new BasicShelfScanEngine();
        try {
            List<String> barcodes = FileUtils.readLines(new File("src/main/resources/testMEDWK1Accuracy.txt"));
            basicShelfScanEngine.setBarcodeSearchDAO(new BarcodeSearchDAO()); //allows flexibility of impl.
            DataLists dataLists = basicShelfScanEngine.process(barcodes, "med", new Date(), "N");
            List<Report> reports  = MisshelfErrorsProcessor.processMisshelfs(dataLists);
            //assertEquals(reports.size(), 25);

            logger.debug("Reports size={}", reports.size());

            for (Report report: reports) {
                logger.debug("Barcode={} Misshelf={}", report.getDISPLAY_CALL_NO() ,report.getText().toString());
            }

            HibernateOracleUtils.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

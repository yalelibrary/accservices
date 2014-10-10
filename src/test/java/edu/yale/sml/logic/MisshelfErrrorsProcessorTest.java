package edu.yale.sml.logic;

import edu.yale.sml.model.DataLists;
import edu.yale.sml.model.Report;
import edu.yale.sml.persistence.BarcodeSearchDAO;
import edu.yale.sml.persistence.config.HibernateOracleUtils;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Date;
import java.util.List;

public class MisshelfErrrorsProcessorTest {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    //String loc = "sml";
    String loc = "med";
    //String oversize = "N";
    String oversize = "Y";
    String fileName = "src/main/resources/testMEDWK1Accuracy.txt";
    //String fileName = "src/test/resources/TestFile.txt";

    @Test
    public void shouldProcessMisshelfs(){
        BasicShelfScanEngine basicShelfScanEngine = new BasicShelfScanEngine();
        try {
            List<String> barcodes = FileUtils.readLines(new File(fileName));
            basicShelfScanEngine.setBarcodeSearchDAO(new BarcodeSearchDAO()); //allows flexibility of impl.
            DataLists dataLists = basicShelfScanEngine.process(barcodes, loc, new Date(), oversize);

            List<Report> reports = dataLists.getCulpritList();

            logger.debug("Reports size={}", reports.size());

            for (Report report: reports) {
                if (report.getText() != 5555 && report.getText() != 0)
                 logger.debug("Barcode=" + report.getITEM_BARCODE() + " --" + report.getDisplayCallNo() + "--" + report.getText());
            }

            HibernateOracleUtils.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

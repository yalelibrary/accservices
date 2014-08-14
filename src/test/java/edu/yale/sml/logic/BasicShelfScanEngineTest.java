package edu.yale.sml.logic;

import edu.yale.sml.model.DataLists;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 */
public class BasicShelfScanEngineTest {

    private Logger logger = LoggerFactory.getLogger(BasicShelfScanEngine.class);

    @Test
    public void shouldProcess() throws Exception {
        String loc = "";
        List<String> barcodes = new ArrayList<String >();
        Date scanDate = new Date();
        String oversize = "N";
        BasicShelfScanEngine basicShelfScanEngine = new BasicShelfScanEngine();
        DataLists dataLists = basicShelfScanEngine.process(barcodes, loc, scanDate, oversize); //TODO
        logger.debug("Report catalog size={}", dataLists.getReportCatalogAsList().size()) ;
    }
}

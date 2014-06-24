package edu.yale.sml.logic;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 */
public class BasicShelfScanEngineTest {

    @Test
    public void shouldProcess() throws Exception {
        String loc = "";
        List<String> barcodes = new ArrayList<String >();
        Date scanDate = new Date();
        String oversize = "N";
        BasicShelfScanEngine basicShelfScanEngine = new BasicShelfScanEngine();
        basicShelfScanEngine.process(barcodes, loc, scanDate, oversize);
    }
}

package edu.yale.sml.logic;

import edu.emory.mathcs.backport.java.util.Collections;
import edu.yale.sml.model.Report;
import org.junit.Test;

import java.util.Date;

public class ShelvingErrorPopulatorTest {

    @Test
    public void shouldPopulateShelvingError() {

        Date scanDate = new Date();
        Report item = new Report();
        item.setSUPPRESS_IN_OPAC("Y");
        String finalLocationName = "sml";
        String oversize = "n";
        int nullBarcodes = 0;
        int suppressedErrors = 0;

        ShelvingErrorPopulator shelvingErrorPopulator = new ShelvingErrorPopulator();
        shelvingErrorPopulator.calculate(Collections.singletonList(item),
                finalLocationName, scanDate, oversize, nullBarcodes, suppressedErrors);

    }
}

package edu.yale.sml.logic;

import edu.emory.mathcs.backport.java.util.Collections;
import edu.yale.sml.model.Report;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class ReportListFilterTest {

    @Test
    public void shouldFilterReportList() {
        Date scanDate = new Date();
        Report item = new Report();
        item.setText(0);
        item.setSUPPRESS_IN_OPAC("Y");
        String finalLocationName = "sml";
        String oversize = "n";

        List<Report> items = ReportListFilter.filterReportList(Collections.singletonList(item), finalLocationName, scanDate, oversize);
        assertTrue(items.size() == 1);
    }
}




package edu.yale.sml.logic;


import edu.yale.sml.logic.CallNumberComparator;
import edu.yale.sml.logic.ShelvingComparator;
import edu.yale.sml.model.Report;
import edu.yale.sml.model.Shelving;
import org.junit.Test;
import org.primefaces.model.SortOrder;

import java.util.Date;

import static org.junit.Assert.assertEquals;

public class CallNumberComparatorTest {

    @Test
    public void shouldCompare() {
        Report r1 = new Report();
        Report r2 = new Report();

        r1.setDISPLAY_CALL_NO("AB 1");
        r2.setDISPLAY_CALL_NO("AB 2");

        CallNumberComparator c = new CallNumberComparator();
        assertEquals(c.compare(r2, r1), 1);
    }

}

package edu.yale.sml.logic;


import edu.yale.sml.model.Report;
import org.junit.Test;

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

package edu.yale.sml.logic;

import edu.yale.sml.logic.FullComparator;
import edu.yale.sml.model.OrbisRecord;
import org.junit.Test;
import org.slf4j.*;

public class FullComparatorTest {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void shouldCompare() {
        try {
            FullComparator fullComparator = new FullComparator();

            OrbisRecord o1 = new OrbisRecord();
            o1.setDISPLAY_CALL_NO("AB 1");
            o1.setNORMALIZED_CALL_NO("AB 1");
            OrbisRecord o2 = new OrbisRecord();
            o2.setDISPLAY_CALL_NO("AB 2");
            o2.setNORMALIZED_CALL_NO("AB 2");

            assert (fullComparator.compare(o1, o2) == -1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

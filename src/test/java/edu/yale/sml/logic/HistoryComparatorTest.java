package edu.yale.sml.logic;

import edu.yale.sml.model.History;
import org.junit.Test;
import org.primefaces.model.SortOrder;

public class HistoryComparatorTest {

    @Test
    public void shouldCompare() {
        try {
            HistoryComparator historyComparator = new HistoryComparator("", SortOrder.ASCENDING);

            History h1 = new History();
            History h2 = new History();

            assert (historyComparator.compare(h1, h2) == 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

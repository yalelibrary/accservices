package edu.yale.sml.logic;


import edu.yale.sml.model.Shelving;
import org.junit.Test;
import org.primefaces.model.SortOrder;

import java.util.Date;

import static org.junit.Assert.assertEquals;

public class ShelvingComparatorTest {

    @Test
    public void shouldCompare() {
        Shelving s1 = new Shelving();
        Shelving s2 = new Shelving();

        s1.setDisplayStart("A");
        long time = System.currentTimeMillis() - 5;
        s1.setCreationDate(new Date(time));

        s2.setCreationDate(new Date());
        s2.setDisplayStart("B");

        ShelvingComparator shelvingComparator = new ShelvingComparator("test", SortOrder.ASCENDING);
        assertEquals(shelvingComparator.compare(s2, s1), 1);
    }

}

package edu.yale.sml.logic;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import edu.emory.mathcs.backport.java.util.Collections;
import edu.yale.sml.logic.CatalogInit;
import org.junit.Test;
import org.slf4j.*;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class CatalogInitTest {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void shouldProcess() {

        try {
            CatalogInit.processCatalogList(Collections.emptyList());
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @org.junit.Test
    public void addDuplicate() {
        Multimap<String, String> map = ArrayListMultimap.create();
        map.put("1", "o");
        map.put("1", "t");
        map.put("2", "p");
        assertEquals("ArrayListMultiMap does not contain duplicate",
                map.get("1"), new ArrayList(Arrays.asList("o", "t")));
        assertEquals("ArrayListMultiMap does not contain duplicate",
                map.get("2"), new ArrayList(Arrays.asList("p")));
    }
}

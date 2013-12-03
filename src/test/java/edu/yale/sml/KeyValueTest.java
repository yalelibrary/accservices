package edu.yale.sml;

/**
 * Created with IntelliJ IDEA.
 * User: osmandin
 * Date: 12/3/13
 * Time: 12:18 PM
 * To change this template use File | Settings | File Templates.
 */

import com.google.common.collect.Multimap;
import com.google.common.collect.ArrayListMultimap;

import java.util.Arrays;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class KeyValueTest {

    @org.junit.Test
    public void addDuplicate()
    {
        Multimap<String,String> map = ArrayListMultimap.create();
        map.put("1", "o");
        map.put("1", "t");
        map.put("2", "p");
        assertEquals("ArrayListMultiMap does not contain duplicate",
                map.get("1"), new ArrayList(Arrays.asList("o","t")));
        assertEquals("ArrayListMultiMap does not contain duplicate",
                map.get("2"), new ArrayList(Arrays.asList("p")));
    }
}

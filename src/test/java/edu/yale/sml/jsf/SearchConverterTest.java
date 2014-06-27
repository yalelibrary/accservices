package edu.yale.sml.jsf;

import org.junit.Test;

public class SearchConverterTest {

    @Test
    public void shouldConvertObject() {
        SearchConverter searchConverter = new SearchConverter();
        Integer i = (Integer) searchConverter.getAsObject(null, null, "true");
        assert (i == 1);
    }

    @Test
    public void shouldConvertString() {
        SearchConverter searchConverter = new SearchConverter();
        assert (searchConverter.getAsString(null, null, true).equals("1"));
    }

}

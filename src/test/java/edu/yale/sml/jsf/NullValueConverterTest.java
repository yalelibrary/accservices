package edu.yale.sml.jsf;

import org.junit.Test;

public class NullValueConverterTest {

    @Test
    public void shouldConvertObject() {
        NullValueConverter nullValueConverter = new NullValueConverter();
        Integer i = (Integer) nullValueConverter.getAsObject(null, null, "test");
        assert (i == 0);
    }

    @Test
    public void shouldConvertString() {
        NullValueConverter nullValueConverter = new NullValueConverter();
        assert(nullValueConverter.getAsString(null, null, " ").isEmpty());
    }

}

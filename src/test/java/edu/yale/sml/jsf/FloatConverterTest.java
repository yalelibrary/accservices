package edu.yale.sml.jsf;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class FloatConverterTest {

    @Test
    public void shouldConvert() {
        FloatConverter floatConverter = new FloatConverter();
        assertEquals("22.5", floatConverter.getAsString(null, null, (float) 22.5));
    }

    @Test
    public void shouldConvertObject() {
        FloatConverter floatConverter = new FloatConverter();
        assertEquals(null, floatConverter.getAsObject(null, null, "22.5"));
    }
}

package edu.yale.sml.jsf;

import org.junit.Test;


public class LongStringConverterTest {

    @Test
    public void shouldConvertString() {
        LongStringConverter longStringConverter = new LongStringConverter();
        String s = "this is a string that has more than 41 characters. This should be displayed with a ellipsis. Thank you.";
        assert (s.length() > 40);
        String t = longStringConverter.getAsString(null, null, s);
        assert (t.length() <= 46);
        assert (t.contains(". . ."));
    }

    @Test
    public void shouldConvertObject() {
        LongStringConverter longStringConverter = new LongStringConverter();
        assert (longStringConverter.getAsObject(null, null, "") == null);
    }

}

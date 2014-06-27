package edu.yale.sml.jsf;

import org.junit.Test;

public class ParagraphStringConverterTest {

    @Test
    public void shouldConvertObject() {
        ParagraphStringConverter paragraphStringConverter = new ParagraphStringConverter();
        assert (paragraphStringConverter.getAsObject(null, null, "") == null);
    }

    @Test
    public void shouldConvertString() {
        ParagraphStringConverter paragraphStringConverter = new ParagraphStringConverter();
        String s = "this is a string that has more than 41 characters. This should be displayed with a ellipsis. Thank you.";
        assert (s.length() > 86);
        String t = paragraphStringConverter.getAsString(null, null, s);
        assert (t.length() <= 86);
        assert (t.contains(". . ."));
    }
}

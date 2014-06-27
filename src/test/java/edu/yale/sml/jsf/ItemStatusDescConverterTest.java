package edu.yale.sml.jsf;

import edu.yale.sml.logic.Rules;
import org.junit.Test;


public class ItemStatusDescConverterTest {

    @Test
    public void shouldConvertObject() {
        ItemStatusDescConverter itemStatusDescConverter = new ItemStatusDescConverter();
        int o = (Integer) itemStatusDescConverter.getAsObject(null, null, "true");
        assert (o == 1);
    }

    @Test
    public void shouldConvertString() {
        ItemStatusDescConverter itemStatusDescConverter = new ItemStatusDescConverter();
        String s = itemStatusDescConverter.getAsString(null, null, Rules.NOT_CHARGED_STRING);
        assert (s.equals("1"));
    }
}

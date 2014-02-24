package edu.yale.sml.jsf;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

@FacesConverter("edu.yale.sml.jsf.LongStringConverter")
public class LongStringConverter implements Converter {

    public String getAsString(FacesContext context, UIComponent component, Object value) {
        String val = (String) value;
        int min_length = (val.length() > 40) ? 40 : val.length();
        return val.length() < 41 ? val : val.substring(0, min_length) + " . . .";
    }

    public Object getAsObject(FacesContext arg0, UIComponent arg1, String arg2) {
        return null;
    }
}
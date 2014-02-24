package edu.yale.sml.jsf;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

@FacesConverter("edu.yale.sml.jsf.ParagraphStringConverter")
public class ParagraphStringConverter implements Converter {

    public String getAsString(FacesContext context, UIComponent component, Object value) {
        String val = (String) value;
        int min_length = (val.length() > 80) ? 80 : val.length();
        return val.substring(0, min_length) + " . . .";
    }

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        return null;
    }
}
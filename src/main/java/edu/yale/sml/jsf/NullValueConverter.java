package edu.yale.sml.jsf;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

@FacesConverter("edu.yale.sml.jsf.NullValueConverter")
public class NullValueConverter implements Converter {

    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        return value.compareTo("true") == 0 ? 1 : 0;
    }

    @Deprecated
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        return "";
    }

}

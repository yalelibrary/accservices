package edu.yale.sml.jsf;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

@FacesConverter("edu.yale.sml.jsf.FloatConverter")
public class FloatConverter implements Converter {

    public String getAsString(FacesContext context, UIComponent component, Object value) {
        float val = ((Float) value);
        return (val % 1 == 0) ? new Integer(Math.round(val)).toString() : Float.toString(val);
    }

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        return null;
    }
}
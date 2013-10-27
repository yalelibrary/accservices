package edu.yale.sml.jsf;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

@FacesConverter("edu.yale.sml.jsf.NullValueConverter")
public class NullValueConverter implements Converter
{

    public Object getAsObject(FacesContext context, UIComponent component, String value)
    {
        if (value.compareTo("true") == 0)
        {
            return 1;
        }
        else
        {
            return 0;
        }
    }

    public String getAsString(FacesContext context, UIComponent component, Object value)
    {
        String val = (String) value;

        if (val.equals("0"))
        {
            return "";
        }
        else
        {
            return "";
        }
    }

}

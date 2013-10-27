package edu.yale.sml.jsf;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

@FacesConverter("edu.yale.sml.jsf.SearchConverter")
public class SearchConverter implements Converter
{

	public Object getAsObject(FacesContext context, UIComponent component,
			String value)
	{
		if(value.compareTo("true") == 0)
		{
			return 1;
		}
		else 
		{
			return 0;
		}
	}

	public String getAsString(FacesContext context, UIComponent component,
			Object value)
	{
		if((Boolean) value == true)
		{
			return "1";
		}
		else
		{
			return "";
		}
	}
}

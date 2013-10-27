package edu.yale.sml.jsf;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import edu.yale.sml.logic.BasicShelfScanEngine;

@FacesConverter("edu.yale.sml.jsf.ItemStatusDescConverter")
public class ItemStatusDescConverter implements Converter
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
		if(((String) value).equals(BasicShelfScanEngine.NOT_CHARGED_STRING))
		{
			return "1";
		}
		else
		{
			return "";
		}
	}
}

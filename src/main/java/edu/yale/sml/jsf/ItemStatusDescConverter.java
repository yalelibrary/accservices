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
		return value.compareTo("true") == 0 ? 1 : 0;	
	}

	public String getAsString(FacesContext context, UIComponent component,
			Object value)
	{
		return ((String) value).equals(BasicShelfScanEngine.NOT_CHARGED_STRING) ? "1" : "";
	}
}

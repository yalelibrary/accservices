package edu.yale.sml.view;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

import edu.yale.sml.model.Location;


//TODO is this used anywhere?

@FacesConverter(forClass=Location.class)
public class CategoryConverter implements Converter {

	public Object getAsObject(FacesContext context, UIComponent component,
			String value)
	{
		return null;
	}

	public String getAsString(FacesContext context, UIComponent component,
			Object value)
	{
		return null;
	}
}

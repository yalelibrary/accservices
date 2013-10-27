package edu.yale.sml.jsf;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

@FacesValidator("edu.yale.sml.jsf.ValidatorBean")
public class ValidatorBean implements Validator
{

	public Object getAsObject(FacesContext context, UIComponent component,
			String value)
	{
		if (value.compareTo("true") == 0)
		{
			return 1;
		} else
		{
			return 0;
		}
	}

	/**
	 * JSF Text converter
	 * 
	 * @param context
	 * @param component
	 * @param value
	 * @return Empty String if input value = 0
	 */
	public String getAsString(FacesContext context, UIComponent component,
			Object value)
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

	@Override
	public void validate(FacesContext context, UIComponent component,
			Object value) throws ValidatorException
	{
		String val = (String) value;
		if (val.equals("null"))
		{
			FacesMessage msg = new FacesMessage("validation failed.",
					"Invalid format.");
			msg.setSeverity(FacesMessage.SEVERITY_ERROR);
			throw new ValidatorException(msg);
		}
	}
}

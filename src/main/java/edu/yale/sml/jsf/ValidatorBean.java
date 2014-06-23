package edu.yale.sml.jsf;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

@FacesValidator("edu.yale.sml.jsf.ValidatorBean")
public class ValidatorBean implements Validator {

    public Object getAsObject(FacesContext context, UIComponent component,
                              String value) {
        return (value.compareTo("true") == 0) ? 1 : 0;
    }

    public String getAsString(FacesContext context, UIComponent component,
                              Object value) {
        return "";
    }

    @Deprecated
    @Override
    public void validate(FacesContext context, UIComponent component,
                         Object value) throws ValidatorException {
        String val = (String) value;
        if (val.equals("null")) {
            FacesMessage msg = new FacesMessage("validation failed.",
                    "Invalid format.");
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(msg);
        }
    }
}

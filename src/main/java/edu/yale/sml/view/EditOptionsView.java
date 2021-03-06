package edu.yale.sml.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import java.io.IOException;

@ManagedBean
@RequestScoped
public class EditOptionsView {

    private final Logger logger = LoggerFactory.getLogger(EditOptionsView.class);

    private String redirect_id = "";

    // To jump to a particular history report
    public void jump() throws IOException {
        logger.debug("EditOptionsView redirecting to=" + redirect_id);
        FacesContext.getCurrentInstance().getExternalContext()
                .redirect("/shelfscan/pages/edit.xhtml?id=" + redirect_id);
    }

    public String getRedirect_id() {
        return redirect_id;
    }

    public void setRedirect_id(String redirect_id) {
        this.redirect_id = redirect_id;
    }
}

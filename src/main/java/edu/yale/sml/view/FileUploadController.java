package edu.yale.sml.view;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;


@ManagedBean
@SessionScoped
public class FileUploadController implements java.io.Serializable {

    private static final long serialVersionUID = 9177080916752001407L;
    private UploadedFile file;

    public UploadedFile getFile() {
        return file;
    }

    public void handleFileUpload(FileUploadEvent event) {
        //logger.debug("FileUploadController : fileName : " + event.getFile().getFileName());
        FacesMessage msg = new FacesMessage("File uploaded.");
        setFile(event.getFile());
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public void setFile(UploadedFile file) {
        this.file = file;
    }
}

package edu.yale.sml.view;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import edu.yale.sml.persistence.FileDAO;
import edu.yale.sml.persistence.FileHibernateDAO;

@ManagedBean
@RequestScoped
public class FileDownloadController {

    private StreamedContent file;

    public FileDownloadController() {
        String fileName = "";
        file = null;

        try {
            if (FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("id") == null) {
                return;
            } else {
                fileName = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("id");
            }
            FileDAO dao = new FileHibernateDAO();
            String contents = dao.findContentsByFileName(fileName);
            InputStream stream = new ByteArrayInputStream(contents.getBytes("UTF-8"));
            file = new DefaultStreamedContent(stream, "text/plain", fileName); //TODO conflicts with p:commandButton (old values is retained)
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public StreamedContent getFile() {
        return file;
    }
}

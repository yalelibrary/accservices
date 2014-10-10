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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ManagedBean
@RequestScoped
public class FileDownloadController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private StreamedContent file;

    public FileDownloadController() {
        final String fileName;
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
            file = new DefaultStreamedContent(stream, "text/plain", fileName);
        } catch (Throwable e) {
            logger.error("Error download", e);
        }
    }

    public StreamedContent getFile() {
        return file;
    }
}

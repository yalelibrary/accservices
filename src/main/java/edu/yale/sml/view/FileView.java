package edu.yale.sml.view;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

import edu.yale.sml.model.InputFile;
import edu.yale.sml.persistence.FileDAO;
import edu.yale.sml.persistence.FileHibernateDAO;
import edu.yale.sml.persistence.GenericHibernateDAO;
import edu.yale.sml.persistence.GenericDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ManagedBean
@RequestScoped
public class FileView {

    private Logger logger = LoggerFactory.getLogger(FileView.class);

    private List<InputFile> inputFileAsList;

    private InputFile selectedFile;

    GenericDAO<InputFile> fileDAO = new GenericHibernateDAO<InputFile>();

    public FileView() {
        super();
    }

    @PostConstruct
    public void init() {
        try {
            inputFileAsList = new ArrayList<InputFile>();
            try {
                inputFileAsList = fileDAO.findAll(InputFile.class);
            } catch (Throwable e) {
                logger.debug("Error init bean", e);
            }
        } catch (Exception e) {
            logger.error("Error", e);
        }
    }

    public InputFile getSelectedFile() {
        return selectedFile;
    }

    public void setSelectedFile(InputFile selectedFile) {
        this.selectedFile = selectedFile;
    }

    public List<InputFile> getInputFileAsList() {
        return inputFileAsList;
    }

    public void setInputFileAsList(List<InputFile> inputFileAsList) {
        this.inputFileAsList = inputFileAsList;
    }

}
package edu.yale.sml.view;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.model.SelectItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.yale.sml.model.InputFile;
import edu.yale.sml.persistence.FileDAO;
import edu.yale.sml.persistence.FileHibernateDAO;
import edu.yale.sml.persistence.HistoryHibernateDAO;
import edu.yale.sml.persistence.HistoryDAO;

@ManagedBean
@SessionScoped
public class HistorySearchView implements java.io.Serializable {

    final static Logger logger = LoggerFactory.getLogger(HistorySearchView.class);

    private static final long serialVersionUID = 6842600101066936186L;

    private String barcodeSearchTerm = "39002097141650"; // tmp for Hibernate Search

    private List<InputFile> inputFileAsList;

    private SelectItem[] levelOptions;

    private Date searchEndDate = new Date();

    private Date searchRunEndDate = new Date();

    private Date searchRunStartDate = new Date();

    private Date searchStartDate = new Date();

    private InputFile selectedFile;

    public HistorySearchView() {
        super();
    }

    public String getBarcodeSearchTerm() {
        return barcodeSearchTerm;
    }

    public List<InputFile> getInputFileAsList() {
        return inputFileAsList;
    }

    public InputFile getSelectedFile() {
        return selectedFile;
    }

    @PostConstruct
    public void initialize() {
    }

    /**
     * Search
     *
     * @return JSF ok navigate
     */
    public String process() {
        inputFileAsList = new ArrayList<InputFile>();
        FileDAO dao = new FileHibernateDAO();

        try {
            logger.trace("HistorySearchView. Building index on InputFile");
            dao.doIndex();
        } catch (Throwable e) {
            // TODO Auto-generated catch block
            logger.error("\n HistorySearchView Error building index!", e);
        }

        List<InputFile> list = null;
        try {
            logger.trace("Searching for : " + barcodeSearchTerm);
            list = dao.search(barcodeSearchTerm);
        } catch (Throwable e) {
            logger.error("general exception", e);
        }

        HashSet<InputFile> hs = new HashSet<InputFile>();
        hs.addAll(list);
        inputFileAsList.addAll(hs);
        HistoryDAO historyDAO = new HistoryHibernateDAO();         // used because there's unidirectional mapping

        for (InputFile item : inputFileAsList) {
            List<List<Integer>> historyIDs = historyDAO.findByFileId(item.getId());
            for (List<Integer> historyIdentifiers : historyIDs) {
                for (Integer i : historyIdentifiers) {
                    item.getHistoryList().add(i.toString());
                }
            }
        }

        return "ok";
    }

    public void setBarcodeSearchTerm(String barcodeSearchTerm) {
        this.barcodeSearchTerm = barcodeSearchTerm;
    }

    public void setInputFileAsList(List<InputFile> inputFileAsList) {
        this.inputFileAsList = inputFileAsList;
    }

    public void setSelectedFile(InputFile selectedFile) {
        this.selectedFile = selectedFile;
    }
}
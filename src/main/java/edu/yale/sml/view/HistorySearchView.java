package edu.yale.sml.view;

// Generated Oct 3, 2012 8:38:46 PM by Hibernate Tools 3.4.0.CR1

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

    String barcodeSearchTerm = "39002097141650"; // tmp for Hibernate Search

    List<InputFile> inputFileAsList;
    SelectItem[] levelOptions;

    Date searchEndDate = new Date();
    Date searchRunEndDate = new Date();
    Date searchRunStartDate = new Date();
    Date searchStartDate = new Date();

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

    public List<InputFile> getInputFileList() {
        return inputFileAsList;
    }

    public Date getSearchEndDate() {
        return searchEndDate;
    }

    public Date getSearchRunEndDate() {
        return searchRunEndDate;
    }

    public Date getSearchRunStartDate() {
        return searchRunStartDate;
    }

    public Date getSearchStartDate() {
        return searchStartDate;
    }

    public String getSearchTerm() {
        return barcodeSearchTerm;
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

        //TODO convert datetime for SQL

        inputFileAsList = new ArrayList<InputFile>();
        FileDAO dao = new FileHibernateDAO(); // check

        try {
            System.out.println("HistorySearchView. Building index on InputFile");
            dao.doIndex();
        } catch (Throwable e) {
            // TODO Auto-generated catch block
            System.out.println("\n HistorySearchView Error building index!");
            System.out.println(e.getCause() + e.getMessage());
            e.printStackTrace();
        }

        List<InputFile> list = null;
        try {
            System.out.println("Searching for : " + barcodeSearchTerm);
            list = dao.search(barcodeSearchTerm);
        } catch (Throwable e) {
            // TODO Auto-generated catch block
            System.out.println("Exception in history search view");
            e.printStackTrace();
        }

        HashSet<InputFile> hs = new HashSet<InputFile>();
        hs.addAll(list);
        inputFileAsList.addAll(hs);

        // used because there's unidirectional mapping
        HistoryDAO historyDAO = new HistoryHibernateDAO();

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

    public void setInputFileList(List<InputFile> inputFileList) {
        this.inputFileAsList = inputFileList;
    }

    public void setSearchEndDate(Date searchEndDate) {
        this.searchEndDate = searchEndDate;
    }

    public void setSearchRunEndDate(Date searchRunEndDate) {
        this.searchRunEndDate = searchRunEndDate;
    }

    public void setSearchRunStartDate(Date searchRunStartDate) {
        this.searchRunStartDate = searchRunStartDate;
    }

    public void setSearchStartDate(Date searchStartDate) {
        this.searchStartDate = searchStartDate;
    }

    public void setSearchTerm(String searchTerm) {
        this.barcodeSearchTerm = searchTerm;
    }

    public void setSelectedFile(InputFile selectedFile) {
        this.selectedFile = selectedFile;
    }
}
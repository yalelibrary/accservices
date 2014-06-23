package edu.yale.sml.view;

import edu.yale.sml.logic.BasicShelfScanEngine;
import edu.yale.sml.logic.InvalidFormatException;
import edu.yale.sml.logic.LogicHelper;
import edu.yale.sml.model.*;
import edu.yale.sml.persistence.GenericDAO;
import edu.yale.sml.persistence.GenericHibernateDAO;
import edu.yale.sml.persistence.HistoryDAO;
import edu.yale.sml.persistence.HistoryHibernateDAO;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang3.SerializationUtils;
import org.hibernate.HibernateException;
import org.hibernate.exception.DataException;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/*
 * Main entry point for report creation.
 *
 * Important methods: initialize, populateSearchView (loads history), saveHistory, process
 */
@ManagedBean
@ViewScoped
public class SearchView implements Serializable {

    private static final long serialVersionUID = 8064034317364105517L;

    final static Logger logger = LoggerFactory.getLogger(SearchView.class);
    final static String PF_FILE_PREFIX = "PrimeFacesUploadedFile";
    final static String PF_FILE_NAME = "PrimeFacesUploadedFileName";
    private final static String APP = "ShelfScan";

    BasicShelfScanEngine engine; // note: using direct impl.
    String fileName = "";
    String finalLocationName = "";
    String firstCallNumber = "", lastCallNumber = "";
    List<Location> locationAsList = new LocationView().findAll();
    Location locationCatalog;
    String locationCode = "";
    String locationName = "sml";
    List<String> locationNames = new ArrayList<String>();
    int nullBarcodes = 0;
    String oversize = "N";
    List<String> oversizeAsList = new ArrayList<String>();
    DataLists reportLists = new DataLists();
    Date scanDate = new Date();
    String selectBoxFileName = "";
    int timeSpent = 30;
    UploadedFile uploadedFile;
    List<OrbisRecord> badBarcodes;
    String uploadedFileName = "";
    String user = "";
    String notes = "";
    String redirect_id = "";

    public SearchView() {
    }

    @PostConstruct
    public void initialize() {

        ExternalContext JsfExternalContext = FacesContext.getCurrentInstance().getExternalContext();
        Map sessionMap = JsfExternalContext.getSessionMap();
        Map<String, String> requestMap = JsfExternalContext.getRequestParameterMap();

        if (sessionMap.get("netid") == null) {
            //TODO
            //ignore
        } else {
            setUser(sessionMap.get("netid").toString());
        }

        oversizeAsList.add("Y");
        oversizeAsList.add("Intermixed");
        oversizeAsList.add("N");
        locationNames = new LocationView().findLocationNames();
        Integer historyID = -1;
        try {
            if (requestMap.get("id") != null) // check in param
            {
                historyID = Integer.parseInt(requestMap.get("id"));
                populateSearchView(historyID);
            } else if (sessionMap.get("HISTORYID") != null) {
                historyID = (Integer) sessionMap.get("HISTORYID");
                populateSearchView(historyID);
            } else {
            }
            FacesContext.getCurrentInstance().getExternalContext().getFlash().remove("initId");
        } catch (InvalidFormatException e) {
            try {
                JsfExternalContext.redirect(new PropertiesConfiguration("messages.properties")
                        .getString("generic_error_redirect"));
            } catch (Exception ce) {
                ce.printStackTrace();
            }
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    public void populateSearchView(Integer ID) throws InvalidFormatException, IOException {
        History historyCatalog;
        HistoryDAO historyDAO = new HistoryHibernateDAO();
        Map sessionMap = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();

        if (engine == null) //TODO change
        {
            engine = new BasicShelfScanEngine();
        }

        try {
            List<History> historyList = null;
            try {
                historyList = historyDAO.findById(ID);
                historyCatalog = historyList.get(0); // ?
            } catch (Exception e) {
                logger.debug("Failed to get report # " + ID + " Redirecting . . .");
                sessionMap.remove("HISTORYID"); // out of caution
                clearSessionMap();
                FacesContext.getCurrentInstance().getExternalContext()
                        .redirect("/shelfscan/pages/error/oops.xhtml");
                return;
            }

            SearchView savedSearchViewObject;

            try {
                savedSearchViewObject = (SearchView) SerializationUtils.deserialize(historyCatalog
                        .getSEARCHVIEW());
            } catch (RuntimeException re) {
                //business logic exception
                throw new InvalidFormatException("Serialization format exception.");
            }

            engine.getReportLists().setCatalogAsList(
                    savedSearchViewObject.reportLists.getCatalogAsList());
            engine.getReportLists().setReportCatalogAsList(
                    savedSearchViewObject.reportLists.getReportCatalogAsList());
            engine.getReportLists().setCatalogSortedRaw(
                    savedSearchViewObject.reportLists.getCatalogSortedRaw());
            engine.getReportLists().setMarkedCatalogAsList(
                    savedSearchViewObject.reportLists.getMarkedCatalogAsList());
            engine.getReportLists().setSuppressedList(
                    savedSearchViewObject.reportLists.getSuppressedList());
            engine.getReportLists().setNullResultBarcodes(
                    savedSearchViewObject.reportLists.getNullResultBarcodes());
            engine.getReportLists().setEnumWarnings(
                    savedSearchViewObject.reportLists.getEnumWarnings());
            engine.setShelvingError(savedSearchViewObject.engine.getShelvingError());
            engine.getReportLists().setCulpritList(
                    savedSearchViewObject.reportLists.getCulpritList());
            engine.getReportLists().setBarcodesAsMap(savedSearchViewObject.reportLists.getBarcodesAsMap());

            fileName = savedSearchViewObject.getFileName();
            firstCallNumber = savedSearchViewObject.getFirstCallNumber();
            lastCallNumber = savedSearchViewObject.getLastCallNumber();
            finalLocationName = savedSearchViewObject.getFinalLocationName();
            oversize = savedSearchViewObject.getOversize();
            scanDate = savedSearchViewObject.getScanDate();

        } catch (NullPointerException e) {
            sessionMap.remove("HISTORYID");
            clearSessionMap();
            throw new NullPointerException(e.getMessage());
        }
    }

    /**
     * Main method that gets invoked when form is sumbitted. Called directly
     * from xhtml JSF view
     *
     * @return "ok" if file is processed ok
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws IOException
     * @throws HibernateException
     * @throws NullFileException
     */
    public String process() throws IllegalAccessException, InvocationTargetException, IOException,
            HibernateException, NullFileException {
        List<String> toFind;
        Integer persistId = 0;
        Map sessionMap = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();

        try {
            try {
                // Report Header
                setFileName(((UploadedFile) sessionMap.get(PF_FILE_PREFIX))
                        .getFileName());
                toFind = LogicHelper.readFile((UploadedFile) sessionMap.get(PF_FILE_PREFIX));
            } catch (NullPointerException e) // ?
            {
                logger.debug("No filename set for header.");
                return "nullfile";
            }

            engine = new BasicShelfScanEngine();
            logFileProcessing();

            logger.debug("Processing barcodes list.");
            reportLists = engine.process(toFind, finalLocationName, scanDate, oversize);
            logger.debug("Finished processing barcodes list.");

            List<OrbisRecord> catalogList = reportLists.getCatalogAsList();

            firstCallNumber = catalogList.get(0).getDISPLAY_CALL_NO();
            lastCallNumber = catalogList.get(catalogList.size() - 1).getDISPLAY_CALL_NO();

            InputFile inputFile = null;

            inputFile = LogicHelper.getInputFile(
                    (UploadedFile) sessionMap.get(PF_FILE_PREFIX), "netid", "date");

            logger.debug("Saving to history");
            persistId = saveHistory(inputFile, reportLists, user,
                    String.valueOf(toFind.size()), finalLocationName);
            // clear PF uploaded file from session
            FacesContext.getCurrentInstance().getExternalContext().getFlash().put("initId", persistId);
            clearSessionMap();
        } catch (NullFileException e) {
            e.printStackTrace();
        } catch (HibernateException e1) {
            throw new HibernateException(e1);
        } catch (Exception ge) {
            ge.printStackTrace();
        }

        sessionMap.put("HISTORYID", persistId);
        return "ok";
    }


    // TODO: perhaps move to HistoryDAO
    private Integer saveHistory(InputFile inputFile, DataLists reportLists, String netid,
                                String numScanned, String finalLocationName) throws HibernateException {
        logger.debug("Saving shelfscan results for file:" + inputFile.getName() + " , for specified user : "
                + netid);
        ShelvingError shelvingError = reportLists.getShelvingError();

        List<OrbisRecord> catalogList = reportLists.getCatalogAsList();
        OrbisRecord first = catalogList.get(0);
        OrbisRecord last = catalogList.get(catalogList.size() - 1);
        int listSize = catalogList.size();

        History history = new History();
        HistoryDAO historyDAO = new HistoryHibernateDAO();
        Integer savedID = 0;

        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
        Date date = new Date();

        // option set to cascade = 'save-update' //revisit if necessary
        history.setInputFile(inputFile);

        history.setFIRSTCALLNUMBER(edu.yale.sml.logic.Rules.getFirstValidDisplayCallNum(reportLists
                .getCatalogAsList()));
        history.setLASTCALLNUMBER(edu.yale.sml.logic.Rules.getLastValidDisplayCallNum(reportLists
                .getCatalogAsList()));

        if (catalogList.get(0).getNORMALIZED_CALL_NO() != null) {
            history.setNORM_CALL_FIRST(reportLists.getCatalogAsList().get(0).getNORMALIZED_CALL_NO());
        }
        if (catalogList.get(listSize - 1).getNORMALIZED_CALL_NO() != null) {
            history.setNORM_CALL_LAST(catalogList
                    .get(listSize - 1).getNORMALIZED_CALL_NO());
        }

        history.setBARCODE_FIRST(first.getITEM_BARCODE());
        history.setBARCODE_LAST(last.getITEM_BARCODE());

        history.setNETID(netid); // TODO check if no netid can still save to history
        history.setTIMESPENT((short) timeSpent);
        history.setNOTES(notes);
        //DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
        history.setSCANDATE(scanDate);
        history.setRUNDATE(new Date());
        history.setNUMBERSCANNED(new Short(numScanned));
        history.setSCANLOCATION(finalLocationName);
        history.setLOCATION("N/A"); // location error
        history.setOVERSIZE(new Short(String.valueOf(shelvingError.getOversize_errors())));
        history.setACCURACY(new Short(String.valueOf(shelvingError.getAccuracy_errors())));
        history.setSTATUS(new Short(String.valueOf(shelvingError.getStatus_errors())));
        history.setSUPPRESS(new Short(String.valueOf(shelvingError.getSuppress_errors())));
        history.setLOCATIONERROR(shelvingError.getLocation_errors());
        history.setNULLBARCODE(shelvingError.getNull_barcodes());
        history.setFILENAME(getFileName());

        // persist for old history report view functionality
        // uses Serialization -- could be replaced w/ XML in future.
        history.setSEARCHVIEW(SerializationUtils.serialize(this));

        try {
            savedID = historyDAO.save(history);
        } catch (DataException e) {
            throw new HibernateException("Hibernate Serialization Error", e.getSQLException());
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return savedID;
    }

    private void clearSessionMap() {
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap()
                .remove(PF_FILE_PREFIX);
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap()
                .remove(PF_FILE_NAME);
    }


    private void logFileProcessing() {
        if (getFileName() != null) {
            logger.trace("Logging file : " + getFileName() + "processing for user:" + user);
            GenericDAO genericDAO = new GenericHibernateDAO();
            Log log = new Log();
            log.setNet_id(user);
            log.setOperation(APP);
            log.setTimestamp(new Date());
            log.setInput_file(getFileName());
            log.setStacktrace("User uploaded file for processing");
            try {
                genericDAO.save(log);
            } catch (Throwable t) {
                logger.error("Error log file processing", t);

            }
        }
    }

    public void handleFileUpload(FileUploadEvent event) {
        Map sessionMap = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
        uploadedFile = event.getFile();
        uploadedFileName = uploadedFile.getFileName();
        sessionMap.put("PrimeFacesUploadedFile", uploadedFile);
        sessionMap.put("PrimeFacesUploadedFileName", uploadedFile.getFileName());
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("File uploaded, etc."));
    }

    //used by results.xhtml (needed?)
    public static String getReferenceLink(String rowIndex) {
        return "#BarcodeSearchViewFormResult:j_idt25:justsorted2:" + rowIndex + ":wrong";
    }

    // To jump to a particular history report
    public void jump() throws IOException {
        FacesContext.getCurrentInstance().getExternalContext()
                .redirect("/shelfscan/pages/results.xhtml?id=" + redirect_id);
    }

    /*
    public static String getReferenceLink2(String rowIndex)
    {
        return "#BarcodeSearchViewFormResult:j_idt25:justsorted2:" + rowIndex + ":wrong";
    }
    */

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setCatalogAsSortedList(List<OrbisRecord> catalogAsSortedList) {
        // this.catalogAsSortedList = catalogAsSortedList; //?
    }

    public void setEngine(BasicShelfScanEngine engine) {
        this.engine = engine;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setFinalLocationName(String finalLocationName) {
        this.finalLocationName = finalLocationName;
    }

    public void setFirstCallNumber(String firstCallNumber) {
        this.firstCallNumber = firstCallNumber;
    }

    public void setLastCallNumber(String lastCallNumber) {
        this.lastCallNumber = lastCallNumber;
    }

    public void setLocationAsList(List<Location> locationAsList) {
        this.locationAsList = locationAsList;
    }

    public void setLocationCatalog(Location locationCatalog) {
        this.locationCatalog = locationCatalog;
    }

    public void setLocationCode(String locationCode) {
        this.locationCode = locationCode;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
        this.finalLocationName = locationName;
        this.locationName = ""; // ?
    }

    public void setLocationNames(List<String> locationNames) {
        this.locationNames = locationNames;
    }

    public void setNullBarcodes(int nullBarcodes) {
        this.nullBarcodes = nullBarcodes;
    }

    public void setOversize(String oversize) {
        this.oversize = oversize;
    }

    public void setOversizeAsList(List<String> oversizeAsList) {
        this.oversizeAsList = oversizeAsList;
    }

    public void setReportLists(DataLists reportLists) {
        this.reportLists = reportLists;
    }

    public void setScanDate(Date scanDate) {
        this.scanDate = scanDate;
    }

    public void setSelectBoxFileName(String selectBoxFileName) {
        this.selectBoxFileName = selectBoxFileName;
    }

    public void setTimeSpent(int timeSpent) {
        this.timeSpent = timeSpent;
    }

    public void setUser(String user) {
        this.user = user;
    }

    @Deprecated
    public boolean validateFile() {
        return true;
    }

    public String getRedirect_id() {
        return redirect_id;
    }

    public void setRedirect_id(String redirect_id) {
        this.redirect_id = redirect_id;
    }

    @Deprecated
    public History doProcess() {
        return null;
    }

    public List<OrbisRecord> getBadBarcodes() {
        return badBarcodes;
    }

    public BasicShelfScanEngine getEngine() {
        return engine;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFinalLocationName() {
        return finalLocationName;
    }

    public String getFirstCallNumber() {
        return firstCallNumber;
    }

    public String getLastCallNumber() {
        return lastCallNumber;
    }

    public List<Location> getLocationAsList() {
        return new LocationView().findAll();
    }

    public Location getLocationCatalog() {
        return locationCatalog;
    }

    public String getLocationCode() {
        return locationCode;
    }

    public String getLocationName() {
        return locationName;
    }

    public List<String> getLocationNames() {
        return new LocationView().findLocationNames();
    }

    public int getNullBarcodes() {
        return nullBarcodes;
    }

    public String getOversize() {
        return oversize;
    }

    public List<String> getOversizeAsList() {
        return oversizeAsList;
    }

    public DataLists getReportLists() {
        return reportLists;
    }

    public Date getScanDate() {
        return scanDate;
    }

    public String getSelectBoxFileName() {
        return selectBoxFileName;
    }

    public synchronized int getTimeSpent() {
        return timeSpent;
    }

    public String getUser() {
        return user;
    }

    public String getUploadedFileName() {
        return uploadedFileName;
    }

    public void setUploadedFileName(String uploadedFileName) {
        this.uploadedFileName = uploadedFileName;
    }
}
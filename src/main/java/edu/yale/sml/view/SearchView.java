package edu.yale.sml.view;

import edu.yale.sml.logic.BasicShelfScanEngine;
import edu.yale.sml.logic.InvalidFormatException;
import edu.yale.sml.logic.LogicHelper;
import edu.yale.sml.logic.Rules;
import edu.yale.sml.model.*;
import edu.yale.sml.persistence.BarcodeSearchDAO;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/*
 * Main entry point for report creation.
 * Important methods: initialize, populateSearchView (loads history), saveHistory, process
 */
@ManagedBean
@ViewScoped
public class SearchView implements Serializable {

    final static Logger logger = LoggerFactory.getLogger(SearchView.class);

    /**Do not remove/rename fields without running tests first
     * see SerializationCheckIT
     */
    private static final long serialVersionUID = 8064034317364105517L;

    private static final String PF_FILE_PREFIX = "PrimeFacesUploadedFile";

    private static final String PF_FILE_NAME = "PrimeFacesUploadedFileName";

    private static final String APP = "ShelfScan";

    /** Identifier for session */
    transient static final String SESSION_HISTORY_ID = "HISTORYID";

    /** Identifier for session netid */
    transient static final String SESSION_NETID = "netid";

    private BasicShelfScanEngine engine;

    private String fileName = "";

    private String finalLocationName = "";

    private String firstCallNumber = "", lastCallNumber = "";

    private String locationName = "sml";

    private List<String> locationNames = new ArrayList<String>();

    private String oversize = "N";

    private List<String> oversizeAsList = new ArrayList<String>();

    private DataLists reportLists = new DataLists();

    private Date scanDate = new Date();

    private int timeSpent = 30;

    private UploadedFile uploadedFile;

    private List<OrbisRecord> badBarcodes;

    private String uploadedFileName = "";

    private String user = "";

    private String notes = "";

    private String redirect_id = "";

    /**
     * Init bean
     */
    @PostConstruct
    public void initialize() {
        ExternalContext JsfExternalContext = FacesContext.getCurrentInstance().getExternalContext();
        Map sessionMap = JsfExternalContext.getSessionMap();
        Map<String, String> requestMap = JsfExternalContext.getRequestParameterMap();

        if (sessionMap.get(SESSION_NETID) == null) {
            //ignore
        } else {
            setUser(sessionMap.get(SESSION_NETID).toString());
        }

        oversizeAsList.add("Y");
        oversizeAsList.add("Intermixed");
        oversizeAsList.add("N");
        locationNames = new LocationView().findLocationNames();
        Integer historyId;

        try {
            if (requestMap.get("id") != null)  {
                historyId = Integer.parseInt(requestMap.get("id"));
                populateSearchView(historyId);
            } else if (sessionMap.get(SESSION_HISTORY_ID) != null) {
                historyId = (Integer) sessionMap.get(SESSION_HISTORY_ID);
                populateSearchView(historyId);
            }

            FacesContext.getCurrentInstance().getExternalContext().getFlash().remove("initId");
        } catch (InvalidFormatException e) {
            try {
                JsfExternalContext.redirect(new PropertiesConfiguration("messages.properties").getString("generic_error_redirect"));
            } catch (Exception ce) {
                logger.error("Error init bean={}", ce);
            }
        } catch (IOException io) {
            logger.error("Error init bean={}", io);
        }
    }

    /**
     * De-serializes old objects.
     * @param ID id of the report
     * @throws InvalidFormatException
     * @throws IOException
     */
    public void populateSearchView(Integer ID) throws InvalidFormatException, IOException {
        History historyCatalog;
        HistoryDAO historyDAO = new HistoryHibernateDAO();
        Map sessionMap = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();

        if (engine == null){
            engine = new BasicShelfScanEngine();
        }

        try {
            List<History> historyList;
            try {
                historyList = historyDAO.findById(ID);
                historyCatalog = historyList.get(0);
            } catch (Exception e) {
                logger.debug("Failed to get report # " + ID + " Redirecting . . .");
                sessionMap.remove(SESSION_HISTORY_ID); // out of caution
                clearSessionMap();
                FacesContext.getCurrentInstance().getExternalContext().redirect("/shelfscan/pages/error/oops.xhtml");
                return;
            }

            final SearchView deserializedObj;

            try {
                deserializedObj = (SearchView) SerializationUtils.deserialize(historyCatalog.getSEARCHVIEW());
            } catch (RuntimeException re) {
                logger.error("Error de-serializing object={}", ID);
                throw new InvalidFormatException("Serialization format exception.");
            }

            engine.getReportLists().setCatalogAsList(deserializedObj.reportLists.getCatalogAsList());
            engine.getReportLists().setReportCatalogAsList(deserializedObj.reportLists.getReportCatalogAsList());
            engine.getReportLists().setCatalogSortedRaw(deserializedObj.reportLists.getCatalogSortedRaw());
            engine.getReportLists().setMarkedCatalogAsList(deserializedObj.reportLists.getMarkedCatalogAsList());
            engine.getReportLists().setSuppressedList(deserializedObj.reportLists.getSuppressedList());
            engine.getReportLists().setNullResultBarcodes(deserializedObj.reportLists.getNullResultBarcodes());
            engine.getReportLists().setEnumWarnings(deserializedObj.reportLists.getEnumWarnings());
            engine.setShelvingError(deserializedObj.engine.getShelvingError());
            engine.getReportLists().setCulpritList(deserializedObj.reportLists.getCulpritList());
            engine.getReportLists().setBarcodesAsMap(deserializedObj.reportLists.getBarcodesAsMap());

            fileName = deserializedObj.getFileName();
            firstCallNumber = deserializedObj.getFirstCallNumber();
            lastCallNumber = deserializedObj.getLastCallNumber();
            finalLocationName = deserializedObj.getFinalLocationName();
            oversize = deserializedObj.getOversize();
            scanDate = deserializedObj.getScanDate();

        } catch (NullPointerException e) {
            sessionMap.remove(SESSION_HISTORY_ID);
            clearSessionMap();
            throw new NullPointerException(e.getMessage());
        }
    }

    /**
     * Main method that gets invoked when form is submitted. Called directly from xhtml JSF view
     *
     * @return "ok" if file is processed ok
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws IOException
     * @throws HibernateException
     * @throws NullFileException
     */
    public String process() throws IllegalAccessException, InvocationTargetException, IOException, HibernateException, NullFileException {
        final List<String> toFind;
        Integer persistId = 0;
        Map sessionMap = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();

        try {
            try {
                // Report Header
                setFileName(((UploadedFile) sessionMap.get(PF_FILE_PREFIX)).getFileName());
                toFind = LogicHelper.readFile((UploadedFile) sessionMap.get(PF_FILE_PREFIX));
            } catch (NullPointerException e){
                logger.error("No filename set for header.");
                return "nullfile";
            }

            engine = new BasicShelfScanEngine();
            logFileProcessing();

            logger.debug("Processing barcodes list.");

            engine.setBarcodeSearchDAO(new BarcodeSearchDAO()); //TODO
            reportLists = engine.process(toFind, finalLocationName, scanDate, oversize);

            logger.debug("Done processing barcodes list.");

            List<OrbisRecord> catalogList = reportLists.getCatalogAsList();
            firstCallNumber = catalogList.get(0).getDISPLAY_CALL_NO();
            lastCallNumber = catalogList.get(catalogList.size() - 1).getDISPLAY_CALL_NO();

            InputFile inputFile;
            inputFile = LogicHelper.getInputFile((UploadedFile) sessionMap.get(PF_FILE_PREFIX), SESSION_NETID, "date");

            logger.trace("Saving to history table");

            persistId = saveHistory(inputFile, reportLists, user, String.valueOf(toFind.size()), finalLocationName);

            // clear PF uploaded file from session
            FacesContext.getCurrentInstance().getExternalContext().getFlash().put("initId", persistId);
            clearSessionMap();
        } catch (NullFileException e) {
            logger.error("Error={}", e);
        } catch (HibernateException e1) {
            throw new HibernateException(e1);
        } catch (Exception ge) {
            logger.error("Error={}", ge);
        }

        sessionMap.put(SESSION_HISTORY_ID, persistId);
        return "ok";
    }

    /**
     * History is saved using reportLists.
     * @param inputFile
     * @param reportLists
     * @param netid
     * @param numScanned
     * @param loc
     * @return
     * @throws HibernateException
     */
    private Integer saveHistory(InputFile inputFile, DataLists reportLists, String netid, String numScanned, String loc)
            throws HibernateException {
        logger.debug("Saving shelfscan results for file={} for specified user={}", inputFile.getName(), netid);

        final ShelvingError shelvingError = reportLists.getShelvingError();

        final List<OrbisRecord> orbisRecordList = reportLists.getCatalogAsList();
        final OrbisRecord first = orbisRecordList.get(0);
        final OrbisRecord last = orbisRecordList.get(orbisRecordList.size() - 1);
        final int listSize = orbisRecordList.size();

        HistoryDAO historyDAO = new HistoryHibernateDAO();
        Integer savedID = 0;

        final History history = new History();
        history.setInputFile(inputFile);
        history.setFIRSTCALLNUMBER(edu.yale.sml.logic.Rules.getFirstValidDisplayCallNum(reportLists.getCatalogAsList()));
        history.setLASTCALLNUMBER(edu.yale.sml.logic.Rules.getLastValidDisplayCallNum(reportLists.getCatalogAsList()));

        if (orbisRecordList.get(0).getNORMALIZED_CALL_NO() != null) {
            history.setNORM_CALL_FIRST(reportLists.getCatalogAsList().get(0).getNORMALIZED_CALL_NO());
        }
        if (orbisRecordList.get(listSize - 1).getNORMALIZED_CALL_NO() != null) {
            history.setNORM_CALL_LAST(orbisRecordList.get(listSize - 1).getNORMALIZED_CALL_NO());
        }

        history.setBARCODE_FIRST(first.getITEM_BARCODE());
        history.setBARCODE_LAST(last.getITEM_BARCODE());
        history.setNETID(netid); // TODO check if no netid can still save to history
        history.setTIMESPENT((short) timeSpent);
        history.setNOTES(notes);
        history.setSCANDATE(scanDate);
        history.setRUNDATE(new Date());
        history.setNUMBERSCANNED(new Short(numScanned));
        history.setSCANLOCATION(loc);
        history.setLOCATION("N/A"); // location error
        history.setOVERSIZE(new Short(String.valueOf(shelvingError.getOversize_errors())));
        history.setACCURACY(new Short(String.valueOf(shelvingError.getAccuracy_errors())));
        history.setSTATUS(new Short(String.valueOf(shelvingError.getStatus_errors())));
        history.setSUPPRESS(new Short(String.valueOf(shelvingError.getSuppress_errors())));
        history.setLOCATIONERROR(shelvingError.getLocation_errors());
        history.setNULLBARCODE(shelvingError.getNull_barcodes());
        history.setFILENAME(getFileName());

        // persist for old history report view functionality
        // uses Serialization
        history.setSEARCHVIEW(SerializationUtils.serialize(this));

        try {
            savedID = historyDAO.save(history);
        } catch (DataException e) {
            throw new HibernateException("Hibernate Serialization Error", e.getSQLException());
        } catch (Throwable t) {
            logger.error("Error={}", t);
        }
        return savedID;
    }

    /**
     * Clears session map variables related to file upload
     */
    private void clearSessionMap() {
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove(PF_FILE_PREFIX);
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove(PF_FILE_NAME);
    }

    /**
     * Sort of a helper to enable UI logging. See logs.xhtml
     */
    private void logFileProcessing() {
        if (getFileName() != null) {
            logger.trace("Logging file : " + getFileName() + "processing for user:" + user);
            final GenericDAO genericDAO = new GenericHibernateDAO();
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

    /**
     * Used by PF to handle file upload. Puts some file related variables in session.
     * @param event
     */
    public void handleFileUpload(FileUploadEvent event) {
        Map sessionMap = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
        uploadedFile = event.getFile();
        uploadedFileName = uploadedFile.getFileName();
        sessionMap.put("PrimeFacesUploadedFile", uploadedFile);
        sessionMap.put("PrimeFacesUploadedFileName", uploadedFile.getFileName());
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("File uploaded, etc."));
    }

    /** To jump to a particular history report */
    public void jump() throws IOException {
        FacesContext.getCurrentInstance().getExternalContext().redirect("/shelfscan/pages/results.xhtml?id=" + redirect_id);
    }

    /** used by results.xhtml */
    public static String getReferenceLink(String rowIndex) {
        return "#BarcodeSearchViewFormResult:j_idt25:justsorted2:" + rowIndex + ":wrong";
    }

    public static boolean isLocationError(final String locationName, final String finalLocationName) {
        return Rules.isLocationError(locationName.trim(), finalLocationName.trim());
    }

    //getters and setters --------------------------------------------------------------------------------------------

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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

    public void setLocationName(String locationName) {
        this.locationName = locationName;
        this.finalLocationName = locationName;
        this.locationName = ""; // ?
    }

    public void setLocationNames(List<String> locationNames) {
        this.locationNames = locationNames;
    }

    public void setOversize(String oversize) {
        this.oversize = oversize;
    }

    public void setOversizeAsList(List<String> oversizeAsList) {
        this.oversizeAsList = oversizeAsList;
    }

    public void setScanDate(Date scanDate) {
        this.scanDate = scanDate;
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

    public String getLocationName() {
        return locationName;
    }

    public List<String> getLocationNames() {
        // Yue Ji Added on 10/24/2018 5:11 PM to sort the location list
        List<String> listLocationNames = new ArrayList<String>(new LocationView().findLocationNames()); 
        Collections.sort(listLocationNames);
        /*
        for (int i =0; i < listLocationNames.size(); i++) {
            System.out.print(listLocationNames.get(i) + ","); 
        }
        */
        //return new LocationView().findLocationNames();
        return listLocationNames;
    }

    public String getOversize() {
        return oversize;
    }

    public List<String> getOversizeAsList() {
        return oversizeAsList;
    }

    public Date getScanDate() {
        return scanDate;
    }

    public int getTimeSpent() {
        return timeSpent;
    }

    public String getUser() {
        return user;
    }

    public void setBadBarcodes(List<OrbisRecord> badBarcodes) {
        this.badBarcodes = badBarcodes;
    }

    /* Was Used to navigate links
    public static String getReferenceLink2(String rowIndex) {
        return "#BarcodeSearchViewFormResult:j_idt25:justsorted2:" + rowIndex + ":wrong";
    }*/

}
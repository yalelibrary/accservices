package edu.yale.sml.view;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import oracle.net.aso.f;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang3.SerializationUtils;
import org.hibernate.HibernateException;
import org.hibernate.exception.DataException;

import edu.yale.sml.logic.BasicShelfScanEngine;
import edu.yale.sml.logic.InvalidFormatException;
import edu.yale.sml.logic.LogicHelper;
import edu.yale.sml.logic.ShelfScanEngine;
import edu.yale.sml.model.DataLists;
import edu.yale.sml.model.InputFile;
import edu.yale.sml.model.Log;
import edu.yale.sml.model.OrbisRecord;
import edu.yale.sml.model.History;
import edu.yale.sml.model.Location;
import edu.yale.sml.model.ShelvingError;
import edu.yale.sml.persistence.FileDAO;
import edu.yale.sml.persistence.FileHibernateDAO;
import edu.yale.sml.persistence.GenericDAO;
import edu.yale.sml.persistence.GenericHibernateDAO;
import edu.yale.sml.persistence.HistoryHibernateDAO;
import edu.yale.sml.persistence.HistoryDAO;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ManagedBean
@ViewScoped

// TODO: Search View has the search and results components on the same page.
// Note for future: Should have separated the form elements from the processing elements.
/*
 * Main entry point for report creation
 */
public class SearchView implements Serializable
{
    final static Logger logger = LoggerFactory.getLogger(SearchView.class);
    final static String PF_FILE_PREFIX = "PrimeFacesUploadedFile";
    private static final long serialVersionUID = 8064034317364105517L;

    // Report
    
    BasicShelfScanEngine engine; // not abstract
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
    DataLists reportLists = new DataLists(); // returned?
    Date scanDate = new Date();
    String selectBoxFileName = "";
    List<String> selectBoxFileNames = new ArrayList<String>();
    int timeSpent = 30;
    UploadedFile uploadedFile;
    List<OrbisRecord> badBarcodes; 
    String uploadedFileName = "";
    String user = ""; 
    String notes = "";
    String redirect_id = ""; 
    public SearchView()
    {
    }


    @Deprecated
    public History doProcess()
    {
        return null;
    }

    public List<OrbisRecord> getBadBarcodes()
    {
        return badBarcodes;
    }

    public ShelfScanEngine getEngine()
    {
        return engine;
    }

    public String getFileName()
    {
        return fileName;
    }

    public String getFinalLocationName()
    {
        return finalLocationName;
    }

    public String getFirstCallNumber()
    {
        return firstCallNumber;
    }

    public String getLastCallNumber()
    {
        return lastCallNumber;
    }

    public List<Location> getLocationAsList()
    {
        // TODO: BUG (bug because it's hit many a times)
        return new LocationView().findAll();
    }

    public Location getLocationCatalog()
    {
        return locationCatalog;
    }

    public String getLocationCode()
    {
        return locationCode;
    }

    public String getLocationName()
    {
        return locationName;
    }

    public List<String> getLocationNames()
    {
        return new LocationView().findLocationNames();
    }

    public int getNullBarcodes()
    {
        return nullBarcodes;
    }

    public String getOversize()
    {
        return oversize;
    }

    public List<String> getOversizeAsList()
    {
        return oversizeAsList;
    }

    public DataLists getReportLists()
    {
        return reportLists;
    }

    public Date getScanDate()
    {
        return scanDate;
    }

    public String getSelectBoxFileName()
    {
        return selectBoxFileName;
    }

    public List<String> getSelectBoxFileNames()
    {
        return selectBoxFileNames;
    }

    public synchronized int getTimeSpent()
    {
        return timeSpent;
    }

    public String getUser()
    {
        return user;
    }

    public void handleFileUpload(FileUploadEvent event)
    {
        FacesMessage msg = new FacesMessage("File uploaded.");
        uploadedFile = event.getFile(); 
        uploadedFileName = uploadedFile.getFileName();
        // TODO Don't have to place whole object in session below since it's not used
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("PrimeFacesUploadedFile", uploadedFile);
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("PrimeFacesUploadedFileName", uploadedFile.getFileName());
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public String getUploadedFileName()
    {
        return uploadedFileName;
    }

    public void setUploadedFileName(String uploadedFileName)
    {
        this.uploadedFileName = uploadedFileName;
    }

    /**
     * initialize() TODO what happens if ID is null or history table is emtpy. exception thrown and user gets the stack trace? or how to get a friendly message across the problem is with the design of picking up hisotry object. really need to check @conversationscoped TODO inject hibernate dao
     */
    @PostConstruct
    public void initialize()
    {

        if (FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("netid") == null)
        {
        }
        else
        {
            setUser(FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("netid").toString());
        }

        oversizeAsList.add("Y");
        oversizeAsList.add("Intermixed");
        oversizeAsList.add("N");
        locationNames = new LocationView().findLocationNames();
        Integer historyID = -1;
        try
        {
            if (FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("id") != null) // check in param
            {
                historyID = Integer.parseInt(FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("id"));
                populateSearchView(historyID);
            }
            else if (FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("HISTORYID") != null)
            {
                historyID = (Integer) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("HISTORYID");
                populateSearchView(historyID);
            }
            else
            {
            }
        }
        catch (InvalidFormatException e)
        {
            try
            {
                FacesContext.getCurrentInstance().getExternalContext().redirect(new PropertiesConfiguration("messages.properties").getString("generic_error_redirect"));
            }
            catch (ConfigurationException e1) // TODO msgs propeties exception, nto needed
            {
                e1.printStackTrace();
            }
            catch (IOException e1) // if redirect fails then what?
            {
                e1.printStackTrace();
            }

            e.printStackTrace();
        }
        catch (IOException io)
        {
            io.printStackTrace();
        }

        // TODO remove. this was for file drop down box
        selectBoxFileNames = new FileView().getInputFilesByName();
    }

    public void populateSearchView(Integer ID) throws InvalidFormatException, IOException
    {
        History historyCatalog = new History();
        HistoryDAO historyDAO = new HistoryHibernateDAO();
        try
        {
            List<History> historyList = null;
            try
            {
                historyList = historyDAO.findById(ID);
                historyCatalog = historyList.get(0); // WARNING
            }
            catch (Exception e)
            {
                logger.debug("About to redirect. Omitting stacktrace. Hibernate failed to rereive object by specified report ID: " + ID);
                FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove("HISTORYID"); // caution, out of cautio0n
                FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove("PrimeFacesUploadedFileName");
                FacesContext.getCurrentInstance().getExternalContext().redirect("/shelfscan/pages/error/oops.xhtml");
                return;
            }

            SearchView savedSearchViewObject = null;

            try
            {
                savedSearchViewObject = (SearchView) SerializationUtils.deserialize(historyCatalog.getSEARCHVIEW());
            }
            catch (RuntimeException apachecommonsexception)
            {
                throw new InvalidFormatException("Serialization format exception."); // business logic exception
            }

            if (engine == null)
            {
                engine = new BasicShelfScanEngine();
            }

            engine.getReportLists().setCatalogAsList(savedSearchViewObject.reportLists.getCatalogAsList());
            engine.getReportLists().setReportCatalogAsList(savedSearchViewObject.reportLists.getReportCatalogAsList());
            engine.getReportLists().setCatalogSortedRaw(savedSearchViewObject.reportLists.getCatalogSortedRaw());
            engine.getReportLists().setMarkedCatalogAsList(savedSearchViewObject.reportLists.getMarkedCatalogAsList());
            engine.getReportLists().setSuppressedList(savedSearchViewObject.reportLists.getSuppressedList());
            engine.getReportLists().setNullResultBarcodes(savedSearchViewObject.reportLists.getNullResultBarcodes());
            engine.getReportLists().setEnumWarnings(savedSearchViewObject.reportLists.getEnumWarnings());
            engine.setShelvingError(savedSearchViewObject.engine.getShelvingError());
            fileName = savedSearchViewObject.getFileName();
            firstCallNumber = savedSearchViewObject.getFirstCallNumber();
            lastCallNumber = savedSearchViewObject.getLastCallNumber();
            finalLocationName = savedSearchViewObject.getFinalLocationName();
            oversize = savedSearchViewObject.getOversize();
            scanDate = savedSearchViewObject.getScanDate();
            engine.getReportLists().setCulpritList(savedSearchViewObject.reportLists.getCulpritList());

        }
        catch (NullPointerException e)
        {
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove("HISTORYID"); 
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove("PrimeFacesUploadedFileName");
            throw new NullPointerException(e.getMessage()); 
        }
    }
    
    
    /**
     * Main method that gets invoked when form is sumbitted. Called directly from xhtml JSF view
     * 
     * @return "ok" if file is processed ok
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws IOException
     * @throws HibernateException
     * @throws NullFileException
     */

    //FIXME 'ufso'
    
    public String process() throws IllegalAccessException, InvocationTargetException, IOException, HibernateException, NullFileException
    {
        boolean allowExistingFileProcessing = LogicHelper.isApplicationPropertyChecked("form.shelfscan.allowProcessingExistingFiles"); //TODO
        List<String> toFind = new ArrayList<String>();
        Integer persistId = 0;
        boolean selectBoxInput = false;
        String fileName = "";
        try
        {
            try
            {
                if ( allowExistingFileProcessing && ((UploadedFile) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(PF_FILE_PREFIX)) == null)
                {
                    if (selectBoxFileName != null)
                    {
                        fileName = selectBoxFileName.replace("[", "");
                        fileName = fileName.replace("]", "");
                        logger.debug("Processing existing file:" + fileName);
                        setFileName(fileName);
                        toFind = LogicHelper.readFile(fileName);
                        selectBoxInput = true;
                    }
                }
                else
                {
                    // Report Header
                    setFileName(((UploadedFile) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(PF_FILE_PREFIX)).getFileName());
                    toFind = LogicHelper.readFile((UploadedFile) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(PF_FILE_PREFIX));
                }
            }
            catch (NullPointerException e) //?
            {
                logger.debug("No filename set for header.");
                return "nullfile";
            }

            // Save file for later search by barcode feature

            engine = new BasicShelfScanEngine(); // DI? but only one engine is ever used

            // (optional feature) Save record of user processing this file:
            logFileProcessing();       
            
            logger.debug("Starting process file");
            reportLists = engine.process(toFind, finalLocationName, scanDate, oversize); //
    
            firstCallNumber = reportLists.getCatalogAsList().get(0).getDISPLAY_CALL_NO();
            lastCallNumber = reportLists.getCatalogAsList().get(reportLists.getCatalogAsList().size() - 1).getDISPLAY_CALL_NO();

            InputFile inputFile = null;

            if (allowExistingFileProcessing && selectBoxInput)
            {
                FileDAO fileDAO = new FileHibernateDAO();
                inputFile = fileDAO.findInputFileByName(fileName);
            }
            else
            {
                inputFile = LogicHelper.getInputFile((UploadedFile) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(PF_FILE_PREFIX), "netid", "date");
            }
            persistId = saveHistory(inputFile, reportLists.getShelvingError(), user, String.valueOf(toFind.size()), finalLocationName);
            // clear session map for PrimeFaces uploade file
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove(PF_FILE_PREFIX);
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove("PrimeFacesUploadedFileName");
        }
        catch (NullFileException e)
        {
            e.printStackTrace();
        }
        catch (HibernateException e1)
        {
            throw new HibernateException(e1);
        }
        catch (Exception ge)
        {
            ge.printStackTrace();
        }

        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("HISTORYID", persistId);
        return "ok";
    }
    
    private void logFileProcessing()
    {
        if (getFileName() != null)
        {
            logger.debug("Logging file : " + getFileName() + "processing for user:" + user);
            GenericDAO genericDAO = new GenericHibernateDAO();
            Log log = new Log();
            log.setNet_id(user);
            log.setOperation("Shelfscan");
            log.setTimestamp(new Date());
            log.setInput_file(getFileName());
            try
            {
                genericDAO.save(log);
            }
            catch (Throwable t)
            {
                logger.debug("Error logging file processing for user:" + user + " , file name:" + getFileName());
                t.printStackTrace();
            }
        }
        else
        {
        }
    }

    // TODO: MAJOR HEADACHE perhaps move to HistoryDAO
    // not passed: reportLists
    private Integer saveHistory(InputFile inputFile, ShelvingError shelvingError, String netid, String numScanned, String finalLocationName) throws HibernateException
    {
        logger.debug("Saving results for file:" + inputFile.getName() + " , for user specified: " + netid);

        History history = new History();
        HistoryDAO historyDAO = new HistoryHibernateDAO();
        Integer savedID = 0;

        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
        Date date = new Date();
        // fetch InputFile
        // option set to cascade = 'save-update' //revisit if necessary
        history.setInputFile(inputFile);

        OrbisRecord first = reportLists.getCatalogAsList().get(0);
        OrbisRecord last = reportLists.getCatalogAsList().get(reportLists.getCatalogAsList().size() - 1);

        history.setFIRSTCALLNUMBER(edu.yale.sml.logic.Rules.getFirstValidDisplayCallNum(reportLists.getCatalogAsList()));
        history.setLASTCALLNUMBER(edu.yale.sml.logic.Rules.getLastValidDisplayCallNum(reportLists.getCatalogAsList()));

        if (reportLists.getCatalogAsList().get(0).getNORMALIZED_CALL_NO() != null)
        {
            history.setNORM_CALL_FIRST(reportLists.getCatalogAsList().get(0).getNORMALIZED_CALL_NO());
        }
        if (reportLists.getCatalogAsList().get(reportLists.getCatalogAsList().size() - 1).getNORMALIZED_CALL_NO() != null)
        {
        	history.setNORM_CALL_LAST(reportLists.getCatalogAsList().get(reportLists.getCatalogAsList().size() - 1).getNORMALIZED_CALL_NO());
        }            

        history.setBARCODE_FIRST(first.getITEM_BARCODE());
        history.setBARCODE_LAST(last.getITEM_BARCODE());

        history.setNETID(netid); // check if no netid can still save to history
        history.setTIMESPENT((short) timeSpent);
        history.setNOTES(notes);
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
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
        history.setTRANSCATIONMESSAGES(reportLists.getTransactionMessages().toString());
        // persist object

        history.setSEARCHVIEW(SerializationUtils.serialize(this));

        try
        {
            savedID = historyDAO.save(history);
        }
        catch (DataException e)
        {
            throw new HibernateException("Hibernate Serialization Error", e.getSQLException());
        }
        catch (Throwable t)
        {
            t.printStackTrace();
        }
        return savedID;
    }

    public String getNotes()
    {
        return notes;
    }

    public void setNotes(String notes)
    {
        this.notes = notes;
    }

    public void setCatalogAsSortedList(List<OrbisRecord> catalogAsSortedList)
    {
        // this.catalogAsSortedList = catalogAsSortedList; //?
    }

    public void setEngine(BasicShelfScanEngine engine)
    {
        this.engine = engine;
    }

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    public void setFinalLocationName(String finalLocationName)
    {
        this.finalLocationName = finalLocationName;
    }

    public void setFirstCallNumber(String firstCallNumber)
    {
        this.firstCallNumber = firstCallNumber;
    }

    public void setLastCallNumber(String lastCallNumber)
    {
        this.lastCallNumber = lastCallNumber;
    }

    public void setLocationAsList(List<Location> locationAsList)
    {
        this.locationAsList = locationAsList;
    }

    public void setLocationCatalog(Location locationCatalog)
    {
        this.locationCatalog = locationCatalog;
    }

    public void setLocationCode(String locationCode)
    {
        this.locationCode = locationCode;
    }

    public void setLocationName(String locationName)
    {
        this.locationName = locationName;
        this.finalLocationName = locationName; 
        this.locationName = ""; // ?
    }

    public void setLocationNames(List<String> locationNames)
    {
        this.locationNames = locationNames;
    }

    public void setNullBarcodes(int nullBarcodes)
    {
        this.nullBarcodes = nullBarcodes;
    }

    public void setOversize(String oversize)
    {
        this.oversize = oversize;
    }

    public void setOversizeAsList(List<String> oversizeAsList)
    {
        this.oversizeAsList = oversizeAsList;
    }

    public void setReportLists(DataLists reportLists)
    {
        this.reportLists = reportLists;
    }

    public void setScanDate(Date scanDate)
    {
        this.scanDate = scanDate;
    }

    public void setSelectBoxFileName(String selectBoxFileName)
    {
        this.selectBoxFileName = selectBoxFileName;
    }

    public void setSelectBoxFileNames(List<String> selectBoxFileNames)
    {
        this.selectBoxFileNames = selectBoxFileNames;
    }

    public synchronized void setTimeSpent(int timeSpent)
    {
        this.timeSpent = timeSpent;
    }

    public void setUser(String user)
    {
        this.user = user;
    }

    @Deprecated
    public boolean validateFile()
    {
        return true;
    }
    
    // TODO - not sure if the following method and corresponding html tab/page is required anymore.
    public static String getReferenceLink(String rowIndex)
    {
        return "#BarcodeSearchViewFormResult:j_idt25:justsorted2:" + rowIndex + ":wrong";
    }

    // TODO - not sure if the following method and corresponding html tab/page is required anymore.
    public static String getReferenceLink2(String rowIndex)
    {
        return "#BarcodeSearchViewFormResult:j_idt25:justsorted2:" + rowIndex + ":wrong";
    }

    // To jump to a particular history report
    public void jump() throws IOException
    {      
            FacesContext.getCurrentInstance().getExternalContext().redirect("/shelfscan/pages/results.xhtml?id=" + redirect_id);     
    }

    public String getRedirect_id()
    {
        return redirect_id;
    }

    public void setRedirect_id(String redirect_id)
    {
        this.redirect_id = redirect_id;
    }
}
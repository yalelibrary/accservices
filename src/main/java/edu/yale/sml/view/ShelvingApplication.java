package edu.yale.sml.view;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.beanutils.converters.DateConverter;
import org.primefaces.event.CellEditEvent;
import org.primefaces.model.LazyDataModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.yale.sml.logic.Rules;
import edu.yale.sml.model.OrbisRecord;
import edu.yale.sml.model.SearchResult;
import edu.yale.sml.model.Shelving;
import edu.yale.sml.persistence.BarcodeSearchDAO;
import edu.yale.sml.persistence.GenericDAO;
import edu.yale.sml.persistence.GenericHibernateDAO;
import edu.yale.sml.persistence.HistoryDAO;
import edu.yale.sml.persistence.HistoryHibernateDAO;
import edu.yale.sml.persistence.ShelvingDAO;
import edu.yale.sml.persistence.ShelvingHibernateDAO;

@ManagedBean
@ViewScoped
public class ShelvingApplication implements java.io.Serializable {
    Logger logger = LoggerFactory.getLogger(ShelvingApplication.class);

    private static final long serialVersionUID = 716362163607646863L;
    List<Shelving> historyAsList = new ArrayList<Shelving>();
    Shelving item = new Shelving();
    private LazyDataModel<Shelving> lazyModel;
    String locationName = "sml";
    List<String> locationNames = new ArrayList<String>();
    List<String> floorNames = new ArrayList<String>();
    SelectItem[] locationSelectOptions;
    SelectItem[] netidOptions;
    List<String> oversizeAsList = new ArrayList<String>();
    private Shelving selectedHistory;
    List<String> teamAsList = new ArrayList<String>();

    @Deprecated
    public void onCellEdit(CellEditEvent event) {
        logger.debug("Deprecated method onCellEdit called");
        Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();
    }

    // for netids list
    private SelectItem[] createFilterOptions(List<Shelving> historyAsList2) {
        List<String> netids = new ArrayList<String>(historyAsList.size());
        for (Shelving h : historyAsList2) {
            netids.add(h.getNETID());
        }
        Set<String> set = new HashSet<String>(netids); // need unique
        SelectItem[] options = new SelectItem[set.size() + 1];
        options[0] = new SelectItem("", "Select");
        int i = 0;
        for (String h : set) {
            options[i + 1] = new SelectItem(h);
            i++;
        }
        return options;
    }

    private SelectItem[] createFilterOptionsPaginated() {
        List<String> netids = new ArrayList<String>();
        HistoryDAO dao = new HistoryHibernateDAO();
        netids = dao.findUniqueNetIds();
        Set<String> set = new HashSet<String>(netids); // need unique
        SelectItem[] options = new SelectItem[set.size() + 1];
        options[0] = new SelectItem("", "Select");
        int i = 0;
        for (String h : set) {
            options[i + 1] = new SelectItem(h);
            i++;
        }
        return options;
    }

    private SelectItem[] createLocationFilterOptions(List<Shelving> historyAsList2) {
        List<String> locations = new ArrayList<String>(historyAsList.size());
        for (Shelving h : historyAsList2) {
            locations.add(h.getSCANLOCATION());
        }

        Set<String> set = new HashSet<String>(locations); // need unique

        SelectItem[] options = new SelectItem[set.size() + 1];

        options[0] = new SelectItem("", "...");

        int i = 0;
        for (String h : set) {
            options[i + 1] = new SelectItem(h);
            i++;
        }
        return options;
    }

    private SelectItem[] createLocationFilterOptionsPaginated() {
        List<String> locations = new ArrayList<String>();
        locations = new HistoryHibernateDAO().findUniqueLocations();
        Set<String> set = new HashSet<String>(locations); // need unique
        SelectItem[] options = new SelectItem[set.size() + 1];
        options[0] = new SelectItem("", "Select");
        int i = 0;
        for (String h : set) {
            options[i + 1] = new SelectItem(h);
            i++;
        }
        return options;
    }

    @PostConstruct
    public void initialize() {
        ShelvingDAO dao = new ShelvingHibernateDAO();
        int historyAsListSize = 0;
        boolean paginate = true;

        oversizeAsList.add("N");
        oversizeAsList.add("Y");
        oversizeAsList.add("Intermixed");

        teamAsList.add("Individual");
        teamAsList.add("Swat");

        item.setNETID(FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("netid").toString());

        locationNames = new LocationView().findLocationNames();
        floorNames = new FloorView().findFloorNames();

        try {
            historyAsList = new ArrayList<Shelving>();
            try {
                if (paginate) {
                    historyAsListSize = dao.count();
                } else
                    historyAsList = dao.findAll(Shelving.class);

            } catch (Throwable e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            System.out.println("Exception initializing");
        }

        netidOptions = createFilterOptions(historyAsList);
        locationSelectOptions = createLocationFilterOptions(historyAsList);


        if (lazyModel == null && paginate) {
            lazyModel = new LazyHistoryNewShelvingDataModel(historyAsListSize);
            locationSelectOptions = createLocationFilterOptionsPaginated();
            netidOptions = createFilterOptionsPaginated();
        }

    }

    public void save(ActionEvent actionEvent) {
        FacesContext context = FacesContext.getCurrentInstance();
        context.getExternalContext().getFlash().setKeepMessages(true);
        context.addMessage(null, new FacesMessage("Saving Shelving Entry"));
        try {
            process();
            context.addMessage(null, new FacesMessage("Saved shelving history entry."));
        } catch (Throwable t) {
            context.addMessage(null, new FacesMessage("Error encountered while saving item. . ."));
        }
    }


    /**
     * TODO needs logic clean up
     * Main method.
     *
     * @return
     * @throws Throwable
     */
    public String process() throws Throwable {
        FacesContext context = FacesContext.getCurrentInstance();
        context.getExternalContext().getFlash().setKeepMessages(true); // http://stackoverflow.com/questions/9932446/how-to-use-primefaces-pgrowl-and-redirect-to-a-page

        GenericDAO<Shelving> dao = new GenericHibernateDAO<Shelving>();

        item.setCreationDate(new java.util.Date());

        if (item.getNETID() == null) // o/wise use what use entered
        {
            item.setNETID(FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("netid").toString()); // TODO thread
        }

        // set item first/last call number.:

        BarcodeSearchDAO barcodeSearchDAO = new BarcodeSearchDAO();

        List<String> toFind = new ArrayList<String>();
        List<OrbisRecord> orbisList = new ArrayList<OrbisRecord>();
        toFind.add(item.getBarcodeStart());
        toFind.add(item.getBarcodeEnd());

        Map<String, Date> barcodesAdded = new HashMap<String, Date>();

        try {
            List<SearchResult> list = barcodeSearchDAO.findAllById(toFind);
            for (SearchResult searchResult : list) {
                if (searchResult.getResult().size() == 0) {
                    continue; // skip full object populating
                }
                for (Map<String, Object> m : searchResult.getResult()) {
                    OrbisRecord catalogObj = new OrbisRecord();
                    java.sql.Date date = null;
                    Converter dc = new DateConverter(date);
                    ConvertUtils.register(dc, java.sql.Date.class);
                    BeanUtils.populate(catalogObj, m);


                    // we want only one entry per barcode... 

                    String barcode = catalogObj.getITEM_BARCODE();

                    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

                    if (barcodesAdded.containsKey(barcode)) {
                        logger.debug("Have seen this barcode before." + catalogObj.getITEM_BARCODE());

                        if (barcodesAdded.get(barcode) != null) {

                            if (!Rules.isValidItemStatus(catalogObj.getITEM_STATUS_DESC()) && catalogObj.getITEM_STATUS_DATE().compareTo(barcodesAdded.get(barcode)) < 0) {
                                logger.debug("Ignoring in favor of a more recent status date: ");
                                continue;
                            } else if (!Rules.isValidItemStatus(catalogObj.getITEM_STATUS_DESC())) {
                                logger.debug("Have the more recent date. So removing older entry");
                                barcodesAdded.remove(barcode);
                                int found = -1;
                                for (int i = 0; i < orbisList.size(); i++) {
                                    if (orbisList.get(i).getITEM_BARCODE().equals(barcode)) {
                                        found = i;
                                        break;
                                    }
                                }
                                if (found >= 0) {
                                    orbisList.remove(found);
                                }
                            } else {
                                logger.debug("Unknown case:" + barcode);
                            }
                        } else {
                            //logger.debug("Date null. So will SKIP");
                            continue;
                        }
                    }
                    if (catalogObj.getITEM_STATUS_DESC() != null && catalogObj.getITEM_STATUS_DATE() != null) {
                        barcodesAdded.put(catalogObj.getITEM_BARCODE(), catalogObj.getITEM_STATUS_DATE());
                    } else {
                        //logger.debug("Cannot add" + catalogObj.getITEM_BARCODE());
                    }
                    orbisList.add(catalogObj);
                }
            }
            String status_desc = "";
            java.util.Date status_date = null;

            if (orbisList.size() == 1) {
                logger.debug("Orbis List Size 1");

                if (orbisList.get(0).getITEM_STATUS_DESC() != null && orbisList.get(0).getITEM_STATUS_DESC().length() > 1) {
                    status_desc = orbisList.get(0).getITEM_STATUS_DESC();
                }

                if (orbisList.get(0).getITEM_STATUS_DATE() != null) {
                    status_date = orbisList.get(0).getITEM_STATUS_DATE();
                }
                if ((item.getBarcodeStart().length() == 0) || orbisList.get(0).getDISPLAY_CALL_NO().equalsIgnoreCase(item.getDisplayEnd())) {
                    item.setDisplayEnd(orbisList.get(0).getDISPLAY_CALL_NO());
                    item.setNormalizedEnd(orbisList.get(0).getDISPLAY_CALL_NO());
                    item.setEndItemStatus(status_desc);
                    item.setEndItemStatusDate(status_date);
                } else {
                    item.setDisplayStart(orbisList.get(0).getDISPLAY_CALL_NO());
                    item.setStartItemStatus(status_desc);
                    item.setStartItemStatusDate(status_date);
                }
            } else if (orbisList.size() == 2) {
                logger.debug("orbis list size 2");

                if (item.getBarcodeStart().equals(orbisList.get(0).getITEM_BARCODE())) {
                    item.setDisplayStart(orbisList.get(0).getDISPLAY_CALL_NO());
                    item.setNormalizedStart(orbisList.get(0).getNORMALIZED_CALL_NO());

                    if (orbisList.get(0).getITEM_STATUS_DESC() != null) {
                        item.setStartItemStatus(orbisList.get(0).getITEM_STATUS_DESC());
                    }

                    if (orbisList.get(0).getITEM_STATUS_DATE() != null) {
                        item.setStartItemStatusDate(orbisList.get(0).getITEM_STATUS_DATE());
                    }
                }

                if (item.getBarcodeEnd().equals(orbisList.get(1).getITEM_BARCODE())) {

                    item.setDisplayEnd(orbisList.get(1).getDISPLAY_CALL_NO());
                    item.setNormalizedEnd(orbisList.get(1).getNORMALIZED_CALL_NO());

                    if (orbisList.get(1).getITEM_STATUS_DESC() != null) {
                        item.setEndItemStatus(orbisList.get(1).getITEM_STATUS_DESC());
                    }
                    if (orbisList.get(1).getITEM_STATUS_DATE() != null) {
                        item.setEndItemStatusDate(orbisList.get(1).getITEM_STATUS_DATE());
                    }
                }
            } else {
                //System.out.println("ALERT: Wrong orbis size: " + orbisList.size());
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }

        //Finally save item!
        try {
            dao.save(item);
            context.addMessage(null, new FacesMessage("Saved Shelving Entry"));
            return "ok";
        } catch (Throwable e) {
            logger.debug(e.getCause().toString());
            logger.debug(e.getMessage());
            context.addMessage(null, new FacesMessage("Error Saving Shelving Entry"));
            return "failed";
        }
    }

    public void setHistoryAsList(List<Shelving> historyAsList) {
        this.historyAsList = historyAsList;
    }

    public void setItem(Shelving item) {
        this.item = item;
    }

    public void setLazyModel(LazyDataModel<Shelving> lazyModel) {
        this.lazyModel = lazyModel;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public void setLocationNames(List<String> locationNames) {
        this.locationNames = locationNames;
    }

    public void setLocationSelectOptions(SelectItem[] locationSelectOptions) {
        this.locationSelectOptions = locationSelectOptions;
    }

    public void setNetidOptions(SelectItem[] netidOptions) {
        this.netidOptions = netidOptions;
    }

    public void setOversizeAsList(List<String> oversizeAsList) {
        this.oversizeAsList = oversizeAsList;
    }

    public void setSelectedHistory(Shelving selectedHistory) {
        this.selectedHistory = selectedHistory;
    }

    public void setTeamAsList(List<String> teamAsList) {
        this.teamAsList = teamAsList;
    }

    public List<String> getFloorNames() {
        return floorNames;
    }

    public void setFloorNames(List<String> floorNames) {
        this.floorNames = floorNames;
    }

    public String selectElement() {
        return "edit_shelving.xhtml?faces-redirect=true&id=" + selectedHistory.getId();
    }

    public List<Shelving> getHistoryAsList() {
        return historyAsList;
    }

    public Shelving getItem() {
        return item;
    }

    public LazyDataModel<Shelving> getLazyModel() {
        return lazyModel;
    }

    public String getLocationName() {
        return locationName;
    }

    public List<String> getLocationNames() {
        return locationNames;
    }

    public SelectItem[] getLocationSelectOptions() {
        return locationSelectOptions;
    }

    public SelectItem[] getNetidOptions() {
        return netidOptions;
    }

    public List<String> getOversizeAsList() {
        return oversizeAsList;
    }

    public Shelving getSelectedHistory() {
        return selectedHistory;
    }

    public List<String> getTeamAsList() {
        return teamAsList;
    }

    public void removeAllPaginated() {
        ShelvingDAO dao = new ShelvingHibernateDAO();
        try {
            List<Shelving> historyAsList = dao.findAll(Shelving.class);
            logger.debug("WARNING: Deleting all Shelving History objects");
            dao.delete(historyAsList);
            historyAsList.clear();
        } catch (Throwable t) {
            logger.debug("Exception removing object");
            t.printStackTrace();
        }
    }

    // PF backed
    public void removeElement() {
        ShelvingDAO dao = new ShelvingHibernateDAO();
        try {
            dao.delete(selectedHistory);
            historyAsList.remove(selectedHistory);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

}
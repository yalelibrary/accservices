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

import edu.yale.sml.model.ShelvingLiveRowCount;
import edu.yale.sml.persistence.ShelvingLiveRowCountDAO;
import edu.yale.sml.persistence.ShelvingLiveRowCountHibernateDAO;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.beanutils.converters.DateConverter;
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

    private Logger logger = LoggerFactory.getLogger(ShelvingApplication.class);

    private static final long serialVersionUID = 716362163607646863L;

    private List<Shelving> historyAsList = new ArrayList<Shelving>();

    private Shelving item = new Shelving();

    private LazyDataModel<Shelving> lazyModel;


    private List<String> locationNames = new ArrayList<String>();

    private List<String> floorNames = new ArrayList<String>();

    private SelectItem[] locationSelectOptions;

    private SelectItem[] netidOptions;

    private List<String> oversizeAsList = new ArrayList<String>();

    private Shelving selectedHistory;

    private List<String> teamAsList = new ArrayList<String>();

    /**
     * Used for updating oldest cart logic
     */
    private ShelvingLiveRowCountDAO shelvingLiveRowCountDAO = new ShelvingLiveRowCountHibernateDAO();

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
        final ShelvingDAO dao = new ShelvingHibernateDAO();
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
                logger.error("Error", e);
            }

        } catch (Exception e) {
            logger.error("Error init", e);
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
     * Main method.
     */
    public String process() throws Throwable {
        final GenericDAO<Shelving> dao = new GenericHibernateDAO<Shelving>();
        BarcodeSearchDAO barcodeSearchDAO = new BarcodeSearchDAO();

        // http://stackoverflow.com/questions/9932446/how-to-use-primefaces-pgrowl-and-redirect-to-a-page
        FacesContext context = FacesContext.getCurrentInstance();
        context.getExternalContext().getFlash().setKeepMessages(true);

        item.setCreationDate(new java.util.Date());

        if (item.getNETID() == null) {
            item.setNETID(FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("netid").toString());
        }

        // set item first/last call number:
        List<String> toFind = new ArrayList<String>();
        List<OrbisRecord> orbisList = new ArrayList<OrbisRecord>();
        toFind.add(item.getBarcodeStart());
        toFind.add(item.getBarcodeEnd());

        final Map<String, Date> barcodesAdded = new HashMap<String, Date>();

        try {
            List<SearchResult> list = barcodeSearchDAO.findAllById(toFind);
            for (SearchResult searchResult : list) {
                if (searchResult.getResult().size() == 0) {
                    continue; // skip full object populating
                }
                for (Map<String, Object> m : searchResult.getResult()) {
                    final OrbisRecord catalogObj = new OrbisRecord();
                    java.sql.Date date = null;
                    Converter dc = new DateConverter(date);
                    ConvertUtils.register(dc, java.sql.Date.class);
                    BeanUtils.populate(catalogObj, m);

                    // we want only one entry per barcode... 

                    final String barcode = catalogObj.getITEM_BARCODE();

                    if (barcodesAdded.containsKey(barcode)) {
                        logger.trace("Have seen this barcode before={}", catalogObj.getITEM_BARCODE());

                        if (barcodesAdded.get(barcode) != null) {

                            if (!Rules.isValidItemStatus(catalogObj.getITEM_STATUS_DESC())
                                    && catalogObj.getITEM_STATUS_DATE().compareTo(barcodesAdded.get(barcode)) < 0) {
                                logger.trace("Ignoring in favor of a more recent status date");
                                continue;
                            } else if (!Rules.isValidItemStatus(catalogObj.getITEM_STATUS_DESC())) {
                                logger.trace("Have the more recent date. So removing older entry");
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
                                logger.trace("Unknown case:" + barcode);
                            }
                        } else {
                            continue;
                        }
                    }
                    if (catalogObj.getITEM_STATUS_DESC() != null && catalogObj.getITEM_STATUS_DATE() != null) {
                        barcodesAdded.put(catalogObj.getITEM_BARCODE(), catalogObj.getITEM_STATUS_DATE());
                    }
                    orbisList.add(catalogObj);
                }
            }
            String status_desc = "";
            java.util.Date status_date = null;

            if (orbisList.size() == 1) {
                logger.trace("Orbis List Size 1");

                if (orbisList.get(0).getITEM_STATUS_DESC() != null && orbisList.get(0).getITEM_STATUS_DESC().length() > 1) {
                    status_desc = orbisList.get(0).getITEM_STATUS_DESC();
                }

                if (orbisList.get(0).getITEM_STATUS_DATE() != null) {
                    status_date = orbisList.get(0).getITEM_STATUS_DATE();
                }

                if ((item.getBarcodeStart().length() == 0)
                        || orbisList.get(0).getDISPLAY_CALL_NO().equalsIgnoreCase(item.getDisplayEnd())) {
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
                logger.trace("orbis list size 2");

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
            }
        } catch (Throwable t) {
            logger.error("Error", t);
        }

        //Finally save the item
        try {
            dao.save(item);
            context.addMessage(null, new FacesMessage("Saved Shelving Entry"));

            // Update Shelving row table:

            if (item.getOldestCartDate() != null && item.getSCANLOCATION().equalsIgnoreCase("SML")) {
                logger.info("Existing count:{}", shelvingLiveRowCountDAO.count());
                logger.info("Floor:{}", item.getFloor());

                // look up the floor row. This should give the entry that needs to be updated
                List<ShelvingLiveRowCount> list = shelvingLiveRowCountDAO.findById(item.getFloor());

                logger.debug("Found matches:{}", list.toString());

                if (list.size() == 0 || list.size() > 1) {
                    logger.error("Invalid number of rows found");
                }

                ShelvingLiveRowCount shelvingCount = list.get(0);
                shelvingCount.setLastUpdateSystem("P");
                shelvingCount.setOldestCart(item.getOldestCartDate());
                shelvingCount.setLastUpdateTimeStamp(new Date());
                shelvingCount.setOldestCartDated(new Date());

                int rowChange = shelvingCount.getRows() - Integer.parseInt(item.getNumRows());
                shelvingCount.setRows(rowChange);

                logger.info("Updating row shelving count:{}", shelvingCount);
                shelvingLiveRowCountDAO.update(shelvingCount);
            } else {
                logger.info("Cannot process oldest cart item");
            }

            return "ok";
        } catch (Throwable e) {
            logger.error("Error processing", e);
            context.addMessage(null, new FacesMessage("Error Saving Shelving Entry or Updating Live Count"));
            return "failed";
        }
    }

    public void setItem(Shelving item) {
        this.item = item;
    }

    public void setLazyModel(LazyDataModel<Shelving> lazyModel) {
        this.lazyModel = lazyModel;
    }

    public void setLocationNames(List<String> locationNames) {
        this.locationNames = locationNames;
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

    public Shelving getItem() {
        return item;
    }

    public LazyDataModel<Shelving> getLazyModel() {
        return lazyModel;
    }

    public List<String> getLocationNames() {
        return locationNames;
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

    public void removeElement() {
        ShelvingDAO dao = new ShelvingHibernateDAO();
        try {
            dao.delete(selectedHistory);
            historyAsList.remove(selectedHistory);
        } catch (Throwable t) {
            logger.error("Error removing element", t);
        }
    }

}
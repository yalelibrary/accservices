package edu.yale.sml.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.annotation.*;

import org.primefaces.event.RowEditEvent;
import org.primefaces.model.LazyDataModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.yale.sml.model.History;
import edu.yale.sml.model.InputFile;
import edu.yale.sml.persistence.FileDAO;
import edu.yale.sml.persistence.FileHibernateDAO;
import edu.yale.sml.persistence.HistoryHibernateDAO;
import edu.yale.sml.persistence.HistoryDAO;

@ManagedBean
@ViewScoped
public class HistoryView implements Serializable {

    final static Logger logger = LoggerFactory.getLogger(HistoryView.class);

    private static final long serialVersionUID = -8625177943611718289L;

    private String currentNotes = ""; // TODO

    private List<History> historyAsList = new ArrayList<History>();

    private LazyDataModel<History> lazyModel;

    private SelectItem[] locationSelectOptions;

    private SelectItem[] netidOptions;

    private String opMsg = ""; // result of operation (used anywhere?)

    private String paramView = "edit.xhtml?id=10";

    private History selectedHistory;

    private HistoryDAO dao = new HistoryHibernateDAO();


    public HistoryView() {
        super();
    }

    @Deprecated
    public String browseTransaction() {
        return "transaction.xhtml?faces-redirect=true&id=" + selectedHistory.getID();
    }

    /**
     * For Netid Filtering
     * @param historyAsList
     * @return
     */
    private SelectItem[] createFilterOptions(List<History> historyAsList) {
        List<String> netids = new ArrayList<String>(this.historyAsList.size());
        for (History h : historyAsList) {
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
        List<String> netids = dao.findUniqueNetIds();
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

    private SelectItem[] createLocationFilterOptions(List<History> historyAsList2) {
        List<String> locations = new ArrayList<String>(historyAsList.size());
        for (History h : historyAsList2) {
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
        List<String> locations = new HistoryHibernateDAO().findUniqueLocations();
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

    @Deprecated
    public String find(String id) {
        for (History h : historyAsList) {
            if (h.getID().equals(id)) {
                return h.getNOTES();
            }
        }

        return null;
    }

    public List<History> getHistoryAsList() {
        return historyAsList;
    }

    public LazyDataModel<History> getLazyModel() {
        return lazyModel;
    }

    public String getOpMsg() {
        return opMsg;
    }

    public History getSelectedHistory() {
        return selectedHistory;
    }

    @PostConstruct
    public void initialize() {
        HistoryDAO historyDAO = new HistoryHibernateDAO();
        int historyAsListSize = 0;
        boolean paginate = true;
        try {
            historyAsList = new ArrayList<History>();
            try {
                if (paginate) {
                    historyAsListSize = historyDAO.count();
                } else {
                    historyAsList = historyDAO.findAll(History.class);
                }
            } catch (Throwable e) {
                logger.debug("Exception encountered finding objects", e);
            }
        } catch (Exception e) {
            logger.debug("Exception initializing");
            opMsg = e.getMessage();
        }

        // Populate PF Filter table ui

        netidOptions = createFilterOptions(historyAsList);
        locationSelectOptions = createLocationFilterOptions(historyAsList);

        if (lazyModel == null && paginate) {
            lazyModel = new LazyHistoryDataModel(historyAsListSize);
            locationSelectOptions = createLocationFilterOptionsPaginated();
            netidOptions = createFilterOptionsPaginated();
        }
    }

    @Deprecated
    public void onEdit(RowEditEvent event) {
        History item = (History) event.getObject();
        try {
            HistoryDAO dao = new HistoryHibernateDAO();
            dao.update(item);
        } catch (Throwable e) {
            logger.debug("Exception editing cell", e);
        }
    }


    public void remove(History history) {
        HistoryDAO historyDAO = new HistoryHibernateDAO();
        FileDAO fileDAO = new FileHibernateDAO();
        InputFile inputFile = fileDAO.findInputFileById(history.getFileId());
        history.setInputFile(new InputFile());
        try {
            historyDAO.delete(history);
            historyAsList.remove(history);
        } catch (Throwable e) {
            logger.debug("Exception removing object", e);
        }
    }

    public void removeElement() {
        HistoryDAO historyDAO = new HistoryHibernateDAO();
        logger.debug("Removing history element : " + selectedHistory.getId() + ":" + selectedHistory.getID());
        try {
            historyDAO.delete(selectedHistory);
            historyAsList.remove(selectedHistory);
            logger.trace("Removing history id from session");
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove("HISTORYID");
        } catch (Throwable e) {
            logger.error("Error removing element", e);
        }
    }

    public void save(History history) {
        HistoryDAO historyDAO = new HistoryHibernateDAO();
        logger.trace("Saving:" + history.toString());
        try {
            historyDAO.save(history);
        } catch (Throwable e) {
            // TODO Auto-generated catch block
            logger.error("Error saving item", e);
        }
    }

    public String selectElement() {
        return "edit.xhtml?faces-redirect=true&id=" + selectedHistory.getID();
    }

    public void setHistoryAsList(List<History> historyAsList) {
        this.historyAsList = historyAsList;
    }

    public void setLazyModel(LazyDataModel<History> lazyModel) {
        this.lazyModel = lazyModel;
    }

    public void setOpMsg(String opMsg) {
        this.opMsg = opMsg;
    }

    public void setSelectedHistory(History selectedHistory) {
        this.selectedHistory = selectedHistory;
    }

}
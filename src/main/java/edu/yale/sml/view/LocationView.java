package edu.yale.sml.view;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import edu.yale.sml.model.Location;
import edu.yale.sml.persistence.GenericHibernateDAO;
import edu.yale.sml.persistence.GenericDAO;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ManagedBean
@ViewScoped
public class LocationView implements java.io.Serializable {

    private final Logger logger = LoggerFactory.getLogger(LocationView.class);

    private static final long serialVersionUID = -1090685442307176628L;

    private List<Location> locationAsList = new ArrayList<Location>();

    /** session identifier */
    public static final String NETID = "netid";

    //UI bound objects:
    private Date date = new Date();

    private String editor = "";

    private String name = "";

    /**
     * @see SearchView
     */
    public List findAll() {
        initialize();
        return locationAsList;
    }

    /**
     * @see SearchView
     */
    public List<String> findLocationNames() {
        initialize();
        List<String> locationNameList = new ArrayList<String>();
        for (Location l : locationAsList) {
            locationNameList.add(l.getName());
        }
        // Yue Ji Added on 10/24/2018 5:11 PM to sort the location list
        Collections.sort(locationNameList);
        return locationNameList;
    }

    @PostConstruct
    public void initialize() {
        GenericDAO<Location> dao = new GenericHibernateDAO<Location>();
        try {
            locationAsList = dao.findAll(Location.class);
        } catch (Throwable e) {
            logger.error("Error init bean", e);
        }
    }

    public void remove(Location locationCatalog) {
        FacesContext.getCurrentInstance().addMessage(null, getSuccessFacesMessage("Deleted!"));
        GenericDAO<Location> dao = new GenericHibernateDAO<Location>();
        try {
            dao.delete(locationCatalog);
            locationAsList.remove(locationCatalog);
        } catch (Throwable e) {
            logger.error("Error removing object", e);
        }
    }

    public void saveAll() {
        if (FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(NETID) != null) {
            editor = FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(NETID).toString();
        }

        final Location item = new Location(name, editor, date);

        try {
            GenericDAO<Location> dao = new GenericHibernateDAO<Location>();
            dao.save(item);
            initialize();
        } catch (Throwable e) {
            logger.error("Error saving object", e);
        }
    }

    /** used by locations.xhtml */
    public void addInfo(ActionEvent actionEvent) {
        FacesContext.getCurrentInstance().addMessage(null, getSuccessFacesMessage("Changes Saved."));
    }

    public FacesMessage getSuccessFacesMessage(String message) {
        return  new FacesMessage(FacesMessage.SEVERITY_INFO, "INFO", message);
    }

    public Date getDate() {
        return date;
    }

    public String getEditor() {
        return editor;
    }

    public List<Location> getLocationAsList() {
        return locationAsList;
    }

    public String getName() {
        return name;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setEditor(String editor) {
        this.editor = editor;
    }

    public void setLocationAsList(List<Location> locationAsList) {
        this.locationAsList = locationAsList;
    }

    public void setName(String name) {
        this.name = name;
    }

}
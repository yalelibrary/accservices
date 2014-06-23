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

@ManagedBean
@ViewScoped
public class LocationView implements java.io.Serializable {

    private static final long serialVersionUID = -1090685442307176628L;
    Date date = new Date();
    String editor = "";
    List<Location> locationAsList = new ArrayList<Location>();
    Location locationCatalog;
    String name = "";

    public void addInfo(ActionEvent actionEvent) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "INFO", "Changes Saved."));
    }

    // Used by SearchView
    public List findAll() {
        initialize();
        return locationAsList;
    }

    // Used by SearchView
    public List<String> findLocationNames() {
        initialize();
        List<String> locationNameList = new ArrayList<String>();
        for (Location l : locationAsList) {
            locationNameList.add(l.getName());
        }
        return locationNameList;
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

    public Location getLocationCatalog() {
        return locationCatalog;
    }

    public String getName() {
        return name;
    }

    @PostConstruct
    public void initialize() {
        GenericDAO<Location> dao = new GenericHibernateDAO<Location>();
        try {
            locationAsList = dao.findAll(Location.class);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void remove(Location locationCatalog) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "INFO", "Deleted!"));
        GenericDAO<Location> dao = new GenericHibernateDAO<Location>();
        try {
            dao.delete(locationCatalog);
            locationAsList.remove(locationCatalog);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void saveAll() {
        if (FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("netid") != null) {
            editor = FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("netid").toString();
        }

        Location item = new Location(name, editor, date);

        try {
            GenericDAO<Location> dao = new GenericHibernateDAO<Location>();
            dao.save(item);

            initialize();
        } catch (Throwable e) {
            e.printStackTrace();
        }
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

    public void setLocationCatalog(Location locationCatalog) {
        this.locationCatalog = locationCatalog;
    }

    public void setName(String name) {
        this.name = name;
    }
}
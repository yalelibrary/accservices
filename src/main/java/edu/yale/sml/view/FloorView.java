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

import edu.yale.sml.model.Floor;
import edu.yale.sml.persistence.GenericHibernateDAO;
import edu.yale.sml.persistence.GenericDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ManagedBean
@ViewScoped
public class FloorView implements java.io.Serializable {

    private static final long serialVersionUID = 6712445546185842422L;

    private static Logger logger = LoggerFactory.getLogger(FloorView.class);

    private Date date = new Date();

    private String editor = "";

    private List<Floor> locationAsList = new ArrayList<Floor>();

    private Floor locationCatalog;

    private String name = "";

    private GenericDAO<Floor> dao = new GenericHibernateDAO<Floor>();


    public void addInfo(ActionEvent actionEvent) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "INFO", "Changes Saved."));
    }

    // Used by SearchView
    public List findAll() {
        initialize();
        return locationAsList;
    }

    // Used by SearchView
    public List<String> findFloorNames() {
        initialize(); //TODO remove
        List<String> locationNameList = new ArrayList<String>();
        for (Floor l : locationAsList) {
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

    public List<Floor> getLocationAsList() {
        return locationAsList;
    }

    public void setLocationAsList(List<Floor> locationAsList) {
        this.locationAsList = locationAsList;
    }


    public String getName() {
        return name;
    }

    @PostConstruct
    public void initialize() {
        try {
            locationAsList = dao.findAll(Floor.class);
        } catch (Throwable e) {
            logger.error("Error init bean", e);
        }
    }

    public void remove(Floor locationCatalog) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "INFO", "Deleted!"));
        GenericDAO<Floor> dao = new GenericHibernateDAO<Floor>();
        try {
            dao.delete(locationCatalog);
            locationAsList.remove(locationCatalog);
        } catch (Throwable e) {
            logger.error("Error removing", e);
        }
    }

    public void saveAll() {
        if (FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("netid") != null) {
            editor = FacesContext.getCurrentInstance().getExternalContext()
                    .getSessionMap().get("netid").toString();
        }

        Floor item = new Floor(name, editor, date);
        try {
            GenericDAO<Floor> dao = new GenericHibernateDAO<Floor>();
            dao.save(item);
            initialize();
        } catch (Throwable e) {
            logger.error("Error saving", e);
        }
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setEditor(String editor) {
        this.editor = editor;
    }

    public void setName(String name) {
        this.name = name;
    }
}
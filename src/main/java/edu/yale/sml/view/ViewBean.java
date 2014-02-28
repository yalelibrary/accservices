

package edu.yale.sml.view;

import edu.yale.sml.model.History;
import edu.yale.sml.persistence.HistoryDAO;
import edu.yale.sml.persistence.HistoryHibernateDAO;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.ObjectInputStream;


@ManagedBean
@ViewScoped
public class ViewBean implements java.io.Serializable {

    private static final long serialVersionUID = 6223995917417414208L;

    History historyCatalog; // history object

    HistoryDAO historyDAO;

    private Integer ID = 0;

    @PostConstruct
    public void initialize() {
        historyDAO = new HistoryHibernateDAO();
        ID = Integer.parseInt(FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("id")); // >
        historyCatalog = new History();
        try {
            historyCatalog = historyDAO.findById(ID).get(0); // TODO WARNING
            // id
            byte[] b = historyCatalog.getSEARCHVIEW();
            ObjectInputStream objectIn = null;
            if (b != null)
                objectIn = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(b));
            Object deSerializedObject = objectIn.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Integer getID() {
        return ID;
    }

    public void setID(Integer iD) {
        ID = iD;
    }

    public History getHistoryCatalog() {
        return historyCatalog;
    }

    public void setHistoryCatalog(History historyCatalog) {
        this.historyCatalog = historyCatalog;
    }

    public String save() {
        historyDAO.update(historyCatalog);
        return "ok";
    }
}
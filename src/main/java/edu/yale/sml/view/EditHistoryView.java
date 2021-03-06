package edu.yale.sml.view;

import java.io.ObjectInputStream;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.yale.sml.model.History;
import edu.yale.sml.persistence.HistoryHibernateDAO;
import edu.yale.sml.persistence.HistoryDAO;


@ManagedBean
@ViewScoped
public class EditHistoryView implements java.io.Serializable {

    private static final Logger logger = LoggerFactory.getLogger(EditHistoryView.class);

    private static final long serialVersionUID = 6223995917417414208L;

    /** history **/
    private History historyCatalog;

    private HistoryDAO historyDAO = new HistoryHibernateDAO();

    private Integer ID = 0;

    @PostConstruct
    public void initialize() {
        ID = Integer.parseInt(FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("id"));
        historyCatalog = new History();

        try {
            historyCatalog = historyDAO.findById(ID).get(0);
            byte[] b = historyCatalog.getSEARCHVIEW();
            ObjectInputStream objectIn = null;
            if (b != null) {
                objectIn = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(b));
            }
            objectIn.readObject();
        } catch (Exception e) {
            logger.error("Error init bean", e);
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
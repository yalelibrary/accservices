package edu.yale.sml.view;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.yale.sml.model.Shelving;
import edu.yale.sml.persistence.ShelvingDAO;
import edu.yale.sml.persistence.ShelvingHibernateDAO;

@ManagedBean
@ViewScoped
public class EditHistoryShelvingView implements java.io.Serializable {

    private static final Logger logger = LoggerFactory.getLogger(EditHistoryShelvingView.class);

    private static final long serialVersionUID = 6223995917417414208L;

    private Shelving historyCatalog; // history object

    private ShelvingDAO historyDAO  = new ShelvingHibernateDAO();;

    private Integer ID = 0;

    @PostConstruct
    public void initialize() {
        ID = Integer.parseInt(FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("id"));
        historyCatalog = new Shelving();
        try {
            historyCatalog = historyDAO.findById(ID).get(0);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public Integer getID() {
        return ID;
    }

    public Shelving getHistoryCatalog() {
        return historyCatalog;
    }

    public void setHistoryCatalog(Shelving historyCatalog) {
        this.historyCatalog = historyCatalog;
    }

    public void setID(Integer id) {
        ID = id;
    }

    public String save() {
        historyDAO.update(historyCatalog);
        return "ok";
    }
}
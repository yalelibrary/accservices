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
public class EditHistoryShelvingView implements java.io.Serializable
{

    private static final long serialVersionUID = 6223995917417414208L;
    private static final Logger logger = LoggerFactory.getLogger(EditHistoryShelvingView.class);

    Shelving historyCatalog; // history object

    ShelvingDAO historyDAO;

    private Integer ID = 0;

    @PostConstruct
    public void initialize()
    {
        historyDAO = new ShelvingHibernateDAO();
        ID = Integer.parseInt(FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("id")); // >
        historyCatalog = new Shelving();
        try
        {
            historyCatalog = historyDAO.findById(ID).get(0); // TODO id
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public Integer getID()
    {
        return ID;
    }

    public Shelving getHistoryCatalog()
    {
        return historyCatalog;
    }

    public void setHistoryCatalog(Shelving historyCatalog)
    {
        this.historyCatalog = historyCatalog;
    }

    public ShelvingDAO getHistoryDAO()
    {
        return historyDAO;
    }

    public void setHistoryDAO(ShelvingDAO historyDAO)
    {
        this.historyDAO = historyDAO;
    }

    public void setID(Integer iD)
    {
        ID = iD;
    }

    public String save()
    {
        historyDAO.update(historyCatalog);
        return "ok";
    }
}
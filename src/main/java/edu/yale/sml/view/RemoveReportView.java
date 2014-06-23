package edu.yale.sml.view;

import edu.yale.sml.model.History;
import edu.yale.sml.model.InputFile;
import edu.yale.sml.persistence.FileDAO;
import edu.yale.sml.persistence.FileHibernateDAO;
import edu.yale.sml.persistence.HistoryDAO;
import edu.yale.sml.persistence.HistoryHibernateDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import java.io.IOException;

@ManagedBean
@RequestScoped
public class RemoveReportView {

    private final Logger logger = LoggerFactory.getLogger(RemoveReportView.class);

    /**
     * Removes individual history report.
     *
     * @param history
     * @see HistoryView remove methods. Use HistoryView instead.
     */
    public String save(History history) {
        logger.debug("Request for deletion for report{}=", history.toString());

        //TODO proper auth check
        if (history.getNETID() == null || history.getNETID().length() < 3) {
            logger.debug("Ignoring delete for this netid.");
            return "failed";
        }
        try {
            remove(history);
            logger.trace("Deleted report.");
            return "ok";

        } catch (Throwable t) {
            logger.error("Error deleting report", t);
        }
        return "failed";
    }

    public void remove(History history) throws Throwable {
        HistoryDAO historyDAO = new HistoryHibernateDAO();
        historyDAO.delete(history);
    }
}

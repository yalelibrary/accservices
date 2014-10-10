package edu.yale.sml.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.yale.sml.model.History;
import edu.yale.sml.persistence.HistoryHibernateDAO;
import edu.yale.sml.persistence.HistoryDAO;

@ManagedBean
@ViewScoped
public class ExcelHistoryView implements Serializable {

    private final static Logger logger = LoggerFactory.getLogger(ExcelHistoryView.class);

    private static final long serialVersionUID = -2165470636629035577L;

    private List<History> historyAsList = new ArrayList<History>();

    private boolean nonColumns = false;

    public ExcelHistoryView() {
        super();
    }

    public boolean isNonColumns() {
        return nonColumns;
    }

    public void setNonColumns(boolean nonColumns) {
        this.nonColumns = nonColumns;
    }

    public List<History> getHistoryAsList() {
        return historyAsList;
    }

    @Deprecated
    public void toggle() {
    }

    @PostConstruct
    public void initialize() {
        HistoryDAO historyDAO = new HistoryHibernateDAO();
        boolean paginate = false;
        try {
            historyAsList = new ArrayList<History>();

            if (paginate) {
                //ignore
            } else {
                historyAsList = historyDAO.findAll(History.class);
            }
        } catch (Throwable e) {
            logger.error("Init error", e);
        }
    }

    public void setHistoryAsList(List<History> historyAsList) {
        this.historyAsList = historyAsList;
    }

}
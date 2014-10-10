package edu.yale.sml.view;


import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.yale.sml.model.Log;
import edu.yale.sml.persistence.GenericHibernateDAO;

@ManagedBean
@ViewScoped
public class LogView implements java.io.Serializable {

    private final static Logger logger = LoggerFactory.getLogger(LogView.class);

    private static final long serialVersionUID = 1778L;

    /** limit of messages to display */
    private static final int MAX_RESULTS = 100;

    private List<Log> logList = new ArrayList<Log>();

    @PostConstruct
    public void initialize() {
        try {
            logList = getLogs();
        } catch (Throwable e) {
            logger.error("Error init bean", e);
        }
    }

    public List<Log> getLogList() {
        return logList;
    }

    public void setLogList(List<Log> logList) {
        this.logList = logList;
    }

    public List<Log> getLogs() {
        try {
            return new GenericHibernateDAO().findAllSorted(Log.class, "timestamp", MAX_RESULTS);
        } catch (Throwable e) {
            logger.error("error getting logs={}", e);
        }
        return null;
    }
}
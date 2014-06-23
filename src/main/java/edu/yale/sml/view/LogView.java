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

    final static Logger logger = LoggerFactory.getLogger(LogView.class);

    private static final long serialVersionUID = 1778L;

    private static final int MAX_RESULTS = 100;

    List<Log> logList = new ArrayList<Log>();

    @PostConstruct
    public void initialize() {
        try {
            logList = getLogs();
        } catch (Throwable e) {
            e.printStackTrace();
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
            //TODO change to interface call
            return new GenericHibernateDAO().findAllSorted(Log.class, "timestamp", MAX_RESULTS);
        } catch (Throwable e) {
            logger.error("error getting logs={}", e);
        }
        return null;
    }
}
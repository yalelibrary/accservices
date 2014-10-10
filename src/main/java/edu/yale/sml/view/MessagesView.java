package edu.yale.sml.view;

import edu.yale.sml.model.Log;
import edu.yale.sml.model.Messages;
import edu.yale.sml.persistence.GenericDAO;
import edu.yale.sml.persistence.GenericHibernateDAO;
import edu.yale.sml.persistence.MessagesDAO;
import edu.yale.sml.persistence.MessagesHibernateDAO;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import static org.slf4j.LoggerFactory.getLogger;

@ManagedBean
@SessionScoped
public class MessagesView {

    private Logger logger = getLogger(this.getClass());

    private List<Messages> messagesList = new ArrayList<Messages>();

    private List<Log> logList = new ArrayList<Log>();

    private HashMap hashMap = new HashMap<String, String>();

    private Properties props = new Properties();

    public List findAll() {
        return messagesList;
    }

    @PostConstruct
    public void initialize() {
        MessagesDAO dao = new MessagesHibernateDAO();
        GenericDAO genericDAO = new GenericHibernateDAO();
        messagesList = new ArrayList<Messages>();
        logList = new ArrayList<Log>();

        try {
            messagesList = dao.findAll(Messages.class);
            logList = genericDAO.findAll(Log.class);

            for (Messages m : messagesList) {
                hashMap.put(m.getNAME(), m.getVALUE());
            }

            props.putAll(hashMap);
        } catch (Throwable e) {
            logger.error("Error", e);
        }
    }

    public List<Messages> getMessagesList() {
        return messagesList;
    }

    public void setMessagesList(List<Messages> messagesList) {
        this.messagesList = messagesList;
    }

    public HashMap getHashMap() {
        return hashMap;
    }

    public void setHashMap(HashMap hashMap) {
        this.hashMap = hashMap;
    }

    public String updateAll() {
        MessagesDAO dao = new MessagesHibernateDAO();
        dao.updateAll(messagesList);
        hashMap.clear();
        initialize();
        return "ok";
    }
}
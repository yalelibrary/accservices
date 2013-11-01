package edu.yale.sml.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.yale.sml.model.Log;
import edu.yale.sml.persistence.GenericDAO;
import edu.yale.sml.persistence.GenericHibernateDAO;
import edu.yale.sml.persistence.SettingsDAO;
import edu.yale.sml.persistence.SettingsHibernateDAO;
import edu.yale.sml.persistence.config.HibernateSQLServerUtil;

@ManagedBean
@SessionScoped
/**
 * TODO Remove ( since it's not used anywhere anymore )
 * 
 * Class for configuring application settings. This configures both the form UI messages as well as toggle switches. 
 * The bean scope has to be checked as well.
 *
 */
public class SettingsView
{
    final static Logger logger = LoggerFactory.getLogger(SettingsView.class);
    List<Log> logList = new ArrayList<Log>();

    List<SettingsView.Messages> messagesList = new ArrayList<SettingsView.Messages>();
    List<SettingsView.Toggles> togglesList = new ArrayList<SettingsView.Toggles>();
    HashMap<String,String> hashMap = new HashMap<String, String>();
    HashMap<String,String> messagesMap = new HashMap<String, String>();
    HashMap<String,Boolean> togglesMap = new HashMap<String, Boolean>();

    @PostConstruct
    public void initialize()
    {
        GenericDAO dao = new GenericHibernateDAO();
        messagesList = new ArrayList<SettingsView.Messages>();
        togglesList = new ArrayList<SettingsView.Toggles>();        
        logList = getLogs();

        try
        {
            
            messagesList = dao.findAll(SettingsView.Messages.class); // if Hibernate can't find internal sub classes?
            togglesList =  dao.findAll(SettingsView.Toggles.class);

            for (SettingsView.Messages m : messagesList)
            {
                messagesMap.put(m.getNAME(), m.getVALUE()); // refer to as #{messagesView.hashMap.get("form.search.insructions"); or whatever jsf shorthand
            }
            
            for (SettingsView.Toggles t : togglesList)
            {
                togglesMap.put(t.getNAME(), t.getVALUE()); // refer to as #{messagesView.hashMap.get("form.search.insructions"); or whatever jsf shorthand
            }
            
            // approach 2 (not used):
            
            List<Settings<String>> messages = new ArrayList<Settings<String>>();
            List<Settings<Boolean>> toggles = new ArrayList<Settings<Boolean>>();
            Object unknownType = dao.findAll(Settings.class);            
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }
    
    //To be replaced with update Settings
    @Deprecated
    public String updateAll()
    {
        return "not ok";
    }
    
    public String updateSettings()
    {
        logger.debug("Saving all settings");        
        return "ok";
    }   
    
    /*
     * Model objects. Inner class since there's little use outside. Inner class is abstract to avoid writing duplicate code in sub classes Toggle and Messages.
     * But sub classes don't have to exist if the only difference is the type of value stored. This is what generics are for -- so you could have a Settings<String>, a Settings<Boolean>, a Settings<Blob>
     * 
     */
    public abstract class Settings<T>
    {
        Integer ID;
        String NAME ="";
        String TOOLTIP = "";
        T VALUE;
            
        public T getVALUE()
        {
            return VALUE;
        }
        public void setVALUE(T vALUE)
        {
            VALUE = vALUE;
        }
        public Integer getID()
        {
            return ID;
        }
        public void setID(Integer iD)
        {
            ID = iD;
        }
        public String getNAME()
        {
            return NAME;
        }
        public void setNAME(String nAME)
        {
            NAME = nAME;
        }
        public String getTOOLTIP()
        {
            return TOOLTIP;
        }
        public void setTOOLTIP(String tOOLTIP)
        {
            TOOLTIP = tOOLTIP;
        }
    }
    
    public class Messages extends Settings<String>
    {
        @Override
        public String getVALUE()
        {
            return VALUE;
        }

        @Override
        public void setVALUE(String vALUE)
        {
            VALUE = vALUE;
        }
    }    
    
    public class Toggles extends Settings<Boolean>
    {
        @Override
        public Boolean getVALUE()
        {
            return VALUE;
        }

        @Override
        public void setVALUE(Boolean vALUE)
        {
            VALUE = vALUE;
        }
    }   
    
    public List<Log> getLogList()
    {
        return logList;
    }

    public void setLogList(List<Log> logList)
    {
        this.logList = logList;
    }   

    public List<Messages> getMessagesList()
    {
        return messagesList;
    }

    public void setMessagesList(List<Messages> messagesList)
    {
        this.messagesList = messagesList;
    }

    public HashMap getHashMap()
    {
        return hashMap;
    }

    public void setHashMap(HashMap hashMap)
    {
        this.hashMap = hashMap;
    }
    
    /**
     * Find all log messages
     * @return
     */
    public List<Log> getLogs()
    {
        try
        {
            return new GenericHibernateDAO().findAll(Log.class);
        }
        catch (Throwable e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace(); //ok to swallow
        }
        return null;
    }
}
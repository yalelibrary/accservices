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

import edu.yale.sml.model.Log;
import edu.yale.sml.model.Messages;
import edu.yale.sml.persistence.GenericDAO;
import edu.yale.sml.persistence.GenericHibernateDAO;
import edu.yale.sml.persistence.MessagesHibernateDAO;
import edu.yale.sml.persistence.MessagesDAO;
import edu.yale.sml.persistence.config.HibernateSQLServerUtil;

@ManagedBean
@SessionScoped
public class MessagesView
{

    List<Messages> messagesList = new ArrayList<Messages>();
    List<Log> logList = new ArrayList<Log>();
    HashMap hashMap = new HashMap<String, String>();
    Properties props = new Properties(); // use me

    public List findAll()
    {
        return messagesList;
    }

    @PostConstruct
    public void initialize()
    {
        MessagesDAO dao = new MessagesHibernateDAO();

        GenericDAO genericDAO = new GenericHibernateDAO();
        messagesList = new ArrayList<Messages>();

        logList = new ArrayList<Log>();

        try
        {
            messagesList = dao.findAll(Messages.class);

            logList = genericDAO.findAll(Log.class);

            for (Messages m : messagesList)
            {
                hashMap.put(m.getNAME(), m.getVALUE()); // refer to as #{messagesView.hashMap.get("form.search.insructions"); or whatever jsf shorthand
            }

            props.putAll(hashMap); // wtf
        }
        catch (Throwable e)
        {
            e.printStackTrace();
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

    public Properties getProps()
    {
        return props;
    }

    public void setProps(Properties props)
    {
        this.props = props;
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

    public String updateAll()
    {
        MessagesDAO dao = new MessagesHibernateDAO();
        dao.updateAll(messagesList);
        hashMap.clear();
        initialize();
        return "ok";
    }

    // TODO tx handling
    public void saveAll()
    {
        try
        {
            try
            {
                Session s = HibernateSQLServerUtil.getSessionFactory().openSession();
                Transaction t = s.beginTransaction();
                for (Messages m : messagesList)
                {
                    s.save(m);
                }
                s.flush();
                t.commit();
                s.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
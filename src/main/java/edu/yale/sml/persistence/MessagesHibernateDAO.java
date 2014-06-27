package edu.yale.sml.persistence;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import edu.yale.sml.model.Messages;
import edu.yale.sml.persistence.config.HibernateSQLServerUtil;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public final class MessagesHibernateDAO extends GenericHibernateDAO<Messages> implements java.io.Serializable, MessagesDAO {

    private Logger logger = getLogger(MessagesHibernateDAO.class);

    private static final long serialVersionUID = -481304207357582739L;


    public MessagesHibernateDAO() {
        super();
    }

    public void updateAll(List<Messages> messagesList) {
        Session s = HibernateSQLServerUtil.getSessionFactory().openSession();
        Transaction t = s.beginTransaction();
        try {
            for (Messages m : messagesList) {
                Messages oldObj = (Messages) s.load(Messages.class, m.getID());
                oldObj.setVALUE(m.getVALUE());
                oldObj.setTOOLTIP(m.getTOOLTIP());
            }
            s.flush();
            t.commit();
        } catch (HibernateException e) {
            logger.error("Error updating", e);
            try {
                t.rollback();
            } catch (Throwable r) {
                logger.error("Error rolling back", e);
            }
        } finally {
            s.close();
        }
    }

}
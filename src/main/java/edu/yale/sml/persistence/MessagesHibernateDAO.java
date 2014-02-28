package edu.yale.sml.persistence;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import edu.yale.sml.model.Messages;
import edu.yale.sml.persistence.config.HibernateSQLServerUtil;

public final class MessagesHibernateDAO extends GenericHibernateDAO<Messages> implements java.io.Serializable, MessagesDAO {
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
            e.printStackTrace();
            try {
                t.rollback();
            } catch (Throwable r) {
                r.printStackTrace();
            }
        } finally {
            s.close();
        }
    }

}
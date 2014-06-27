package edu.yale.sml.persistence;

import edu.yale.sml.model.Shelving;
import edu.yale.sml.persistence.config.HibernateSQLServerUtil;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;

import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

public final class ShelvingHibernateDAO extends GenericHibernateDAO<Shelving> implements java.io.Serializable, ShelvingDAO {

    private Logger logger = getLogger(this.getClass());

    private static final long serialVersionUID = -4044166542029569019L;

    public ShelvingHibernateDAO() {
        super();
    }

    @Override
    public void update(Shelving Shelving) {
        Session s = null;
        Transaction tx = null;
        try {
            s = HibernateSQLServerUtil.getSessionFactory().openSession();
            tx = s.beginTransaction();
            Shelving oldObj = (Shelving) s.load(Shelving.class, Shelving.getId());
            oldObj.setNotes(Shelving.getNotes());
            s.flush();
            tx.commit();
        } catch (HibernateException e) {
            logger.error("Error", e);
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Throwable rt) {
                logger.error("Error", rt);
            }
        } finally {
            if (s != null) {
                s.close();
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Shelving> findById(Integer ID) {
        Session session = null;
        try {
            session = HibernateSQLServerUtil.getSessionFactory().openSession();
            Query q = session.createQuery("from Shelving where ID = " + ID);
            return q.list();
        } catch (HibernateException e) {
            logger.error("Error", e);
            throw new HibernateException(e);
        } finally {
            session.close();
        }
    }

    /** Used for Pagination */
    @Override
    public int count() {
        Session session = HibernateSQLServerUtil.getSessionFactory().openSession();
        try {
            return ((Long) session.createQuery("select count(*) from  edu.yale.sml.model.Shelving h").uniqueResult()).intValue();
        } catch (Throwable e) {
            throw new HibernateException(e);
        } finally {
            session.close();
        }
    }

}
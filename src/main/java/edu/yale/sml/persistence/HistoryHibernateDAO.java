package edu.yale.sml.persistence;

import edu.yale.sml.model.History;
import edu.yale.sml.persistence.config.HibernateSQLServerUtil;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

public final class HistoryHibernateDAO extends GenericHibernateDAO<History> implements java.io.Serializable, HistoryDAO {

    private static final long serialVersionUID = -4044166542029569019L;

    private Logger logger = getLogger(this.getClass());

    public HistoryHibernateDAO() {
        super();
    }

    @Override
    public void update(History history) {
        Session s = null;
        Transaction tx = null;
        try {
            s = HibernateSQLServerUtil.getSessionFactory().openSession();
            tx = s.beginTransaction();
            History oldObj = (History) s.load(History.class, history.getID());
            oldObj.setNOTES(history.getNOTES());
            oldObj.setACCURACY(history.getACCURACY());
            oldObj.setLOCATIONERROR(history.getLOCATIONERROR());
            oldObj.setMISLABELLED(history.getMISLABELLED());
            oldObj.setSTATUS(history.getSTATUS());
            oldObj.setTIMESPENT(history.getTIMESPENT());
            oldObj.setNETID(history.getNETID());
            oldObj.setSUPPRESS(history.getSUPPRESS());
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
    public List<History> findById(Integer ID) {
        Session session = null;
        try {
            session = HibernateSQLServerUtil.getSessionFactory().openSession();
            Query q = session.createQuery("from History where ID = " + ID);
            return q.list();
        } catch (HibernateException e) {
            logger.error("Error", e);
            throw new HibernateException(e);
        } finally {
            session.close();
        }
    }

    @Override
    public List<List<Integer>> findByFileId(int id) {
        Session session = HibernateSQLServerUtil.getSessionFactory().openSession();
        List<ArrayList<Map<String, Object>>> aggregateList = new ArrayList<ArrayList<Map<String, Object>>>();
        try {
            Query q = session.createQuery("select new list (h.ID) from edu.yale.sml.model.History h where File_ID = " + id);
            List<List<Integer>> historyIDs = q.list();
            return historyIDs;
        } catch (Throwable e) {
            logger.debug("Exception in findByfileID" + e.getMessage() + ":" + e.getCause());
            throw new HibernateException(e);
        } finally {
            session.close();
        }
    }


    /**
     *  Used for Pagination
    */
    @Override
    public int count() {
        Session session = HibernateSQLServerUtil.getSessionFactory().openSession();
        try {
            return ((Long) session.createQuery("select count(*) from  edu.yale.sml.model.History h").uniqueResult()).intValue();
        } catch (Throwable e) {
            logger.debug("Exception in findByfileID" + e.getMessage() + ":" + e.getCause());
            throw new HibernateException(e);
        } finally {
            session.close();
        }
    }


    @SuppressWarnings("unchecked")
    @Override
    public List<String> findUniqueNetIds() {
        Session session = HibernateSQLServerUtil.getSessionFactory().openSession();
        try {
            return ((List<String>) session.createQuery("select distinct h.NETID from edu.yale.sml.model.History h").list());
        } catch (Throwable e) {
            logger.debug("Exception in findByfileID" + e.getMessage() + ":" + e.getCause());
            throw new HibernateException(e);
        } finally {
            session.close();
        }
    }


    @SuppressWarnings("unchecked")
    @Override
    public List<String> findUniqueLocations() {
        Session session = HibernateSQLServerUtil.getSessionFactory().openSession();
        try {
            return ((List<String>) session.createQuery("select distinct h.SCANLOCATION from edu.yale.sml.model.History h").list());
        } catch (Throwable e) {
            logger.debug("Exception in findByfileID" + e.getMessage() + ":" + e.getCause());
            throw new HibernateException(e);
        } finally {
            session.close();
        }
    }


}
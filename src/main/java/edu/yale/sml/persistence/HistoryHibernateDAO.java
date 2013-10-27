package edu.yale.sml.persistence;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Expression;
import org.hibernate.transform.AliasToEntityMapResultTransformer;

import edu.yale.sml.model.History;
import edu.yale.sml.persistence.*;
import edu.yale.sml.persistence.config.HibernateSQLServerUtil;

public final class HistoryHibernateDAO extends GenericHibernateDAO<History> implements java.io.Serializable, HistoryDAO
{

    private static final long serialVersionUID = -4044166542029569019L;

    public HistoryHibernateDAO()
    {
        super();
    }

    @Override
    public void update(History history)
    {
        Session s = null;
        Transaction tx = null;
        try
        {
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
        }
        catch (HibernateException e)
        {
            e.printStackTrace();
            try
            {
                if (tx != null)
                {
                    tx.rollback();
                }
            }
            catch (Throwable rt)
            {
                rt.printStackTrace();
            }
        }
        finally
        {
            if (s != null)
            {
                s.close();
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<History> findById(Integer ID)
    {
        Session session = null;
        try
        {
            session = HibernateSQLServerUtil.getSessionFactory().openSession();
            Query q = session.createQuery("from History where ID = " + ID);
            return q.list();
        }
        catch (HibernateException e)
        {
            e.printStackTrace();
            throw new HibernateException(e);
        }
        finally
        {
            session.close();
        }
    }

    // TODO remove if one-to-many mapping is established between InputFile and History
    @Override
    public List<List<Integer>> findByFileId(int id)
    {
        Session session = HibernateSQLServerUtil.getSessionFactory().openSession();
        List<ArrayList<Map<String, Object>>> aggregateList = new ArrayList<ArrayList<Map<String, Object>>>();
        try
        {
            Query q = session.createQuery("select new list (h.ID) from edu.yale.sml.model.History h where File_ID = " + id);
            List<List<Integer>> historyIDs = q.list();
            return historyIDs;
        }
        catch (Throwable e)
        {
            logger.debug("Exception in findByfileID" + e.getMessage() + ":" + e.getCause());
            throw new HibernateException(e);
        }
        finally
        {
            session.close();
        }
    }

    public List<List<Integer>> findByFileIdCrieria(int id, Date scanStartDate, Date scanEndDate, Date runStartDate, Date runEndDate)
    {

        logger.debug("Searching by criteria");

        Session session = HibernateSQLServerUtil.getSessionFactory().openSession();
        List<ArrayList<Map<String, Object>>> aggregateList = new ArrayList<ArrayList<Map<String, Object>>>();
        try
        {
            Criteria criteria = session.createCriteria("select new list (h.ID) from edu.yale.sml.model.History h where File_ID = " + id);

            if (scanStartDate != null)
            {
                criteria.add(Expression.ge("SCANDATE", scanStartDate));
            }

            if (scanEndDate != null)
            {
                criteria.add(Expression.le("SCANDATE", scanEndDate));

            }

            List<List<Integer>> historyIDs = criteria.list();
            return historyIDs;
        }
        catch (Throwable e)
        {
            logger.debug("Exception in findByfileID" + e.getMessage() + ":" + e.getCause());
            throw new HibernateException(e);
        }
        finally
        {
            session.close();
        }
    }

    public List<List<Integer>> findByFileId(int id, Date scanStartDate, Date scanEndDate, Date runStartDate, Date runEndDate)
    {

        Session session = HibernateSQLServerUtil.getSessionFactory().openSession();
        try
        {
            Query q = session.createQuery("select new list (h.ID) from edu.yale.sml.model.History h where " + "h.SCANDATE between" + scanStartDate + "and" + scanEndDate + " and " + "File_ID = " + id);
            List<List<Integer>> historyIDs = q.list();
            return historyIDs;
        }
        catch (Throwable e)
        {
            logger.debug("Exception in findByfileID" + e.getMessage() + ":" + e.getCause());
            throw new HibernateException(e);
        }
        finally
        {
            session.close();
        }
    }
    
    /* Used for Pagination*/
    @Override
    public int count()
    {
        Session session = HibernateSQLServerUtil.getSessionFactory().openSession();
        try
        {
            return ((Long)session.createQuery("select count(*) from  edu.yale.sml.model.History h").uniqueResult()).intValue();
        }
        catch (Throwable e)
        {
            logger.debug("Exception in findByfileID" + e.getMessage() + ":" + e.getCause());
            throw new HibernateException(e);
        }
        finally
        {
            session.close();
        }
    }

        
    @SuppressWarnings("unchecked")
    @Override
    public List<String> findUniqueNetIds()
    {
        Session session = HibernateSQLServerUtil.getSessionFactory().openSession();
        try
        {
            return ((List<String>) session.createQuery("select distinct h.NETID from edu.yale.sml.model.History h").list());
        }
        catch (Throwable e)
        {
            logger.debug("Exception in findByfileID" + e.getMessage() + ":" + e.getCause());
            throw new HibernateException(e);
        }
        finally
        {
            session.close();
        }
    }
    
    
    @SuppressWarnings("unchecked")
    @Override
    public List<String> findUniqueLocations()
    {
        Session session = HibernateSQLServerUtil.getSessionFactory().openSession();
        try
        {
            return ((List<String>) session.createQuery("select distinct h.SCANLOCATION from edu.yale.sml.model.History h").list());
        }
        catch (Throwable e)
        {
            logger.debug("Exception in findByfileID" + e.getMessage() + ":" + e.getCause());
            throw new HibernateException(e);
        }
        finally
        {
            session.close();
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public int findUniqueLocationsOccurrence(String location)
    {
        Session session = HibernateSQLServerUtil.getSessionFactory().openSession();
        try
        {
            Query query =  session.createQuery("select count(h) from  edu.yale.sml.model.History h where h.SCANLOCATION = :param");
            query.setParameter("param", location);
            return ((Long)query.uniqueResult()).intValue();
        }
        catch (Throwable e)
        {
            logger.debug("Exception in findByfileID" + e.getMessage() + ":" + e.getCause());
            throw new HibernateException(e);
        }
        finally
        {
            session.close();
        }
    }

    @Override
    public Number findUniqueNetIdOccurrence(String s)
    {
        Session session = HibernateSQLServerUtil.getSessionFactory().openSession();
        try
        {
            Query query =  session.createQuery("select count(h) from  edu.yale.sml.model.History h where h.NETID = :param");
            query.setParameter("param", s);
            return ((Long)query.uniqueResult()).intValue();
        }
        catch (Throwable e)
        {
            logger.debug("Exception in findByfileID" + e.getMessage() + ":" + e.getCause());
            throw new HibernateException(e);
        }
        finally
        {
            session.close();
        }
    }

    @Override
    public Number findMonthlyScanByNetId(String arg0, Date arg1, Date arg2)
    {
        Session session = HibernateSQLServerUtil.getSessionFactory().openSession();
        try
        {
            Query query =  session.createQuery("select count(h) from  edu.yale.sml.model.History h where h.NETID = :param and h.SCANDATE between :paramStartDate and :paramEndDate");
            query.setParameter("param", arg0);
            query.setParameter("paramStartDate", arg1);
            query.setParameter("paramEndDate", arg2);
            return ((Long)query.uniqueResult()).intValue();
        }
        catch (Throwable e)
        {
            logger.debug("Exception in findByfileID" + e.getMessage() + ":" + e.getCause());
            throw new HibernateException(e);
        }
        finally
        {
            session.close();
        }
    }

}
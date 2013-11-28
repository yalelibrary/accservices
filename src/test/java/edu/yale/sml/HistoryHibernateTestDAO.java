package edu.yale.sml;

import edu.yale.sml.model.History;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;

import java.util.List;

/**
 * TODO refactor class and hibernate ref.
 */
public final class HistoryHibernateTestDAO
{


    public HistoryHibernateTestDAO()
    {
        super();
    }


    @SuppressWarnings("unchecked")
    public List<History> findById(Integer ID)
    {
        Session session = null;
        try
        {
            session = HibernateSQLServerUtilTest.getSessionFactory().openSession();
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

}
package edu.yale.sml.persistence;

import java.util.List;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.yale.sml.model.Admin;
//import edu.yale.sml.persistence.config.HibernateMySqlUtil;
import edu.yale.sml.persistence.config.HibernateSQLServerUtil;

public  class AdminHibernateDAO extends GenericHibernateDAO<Admin> implements java.io.Serializable, AdminDAO
{

	private static final long serialVersionUID = -481304207357582739L;
	Logger logger = LoggerFactory.getLogger(AdminHibernateDAO.class);

	public AdminHibernateDAO()
	{
	    super();
	}
	
	@Override
    @SuppressWarnings("unchecked")
    public String findByNetId(String netid) throws HibernateException
    {
        Session session = null;
        try
        {
            session = HibernateSQLServerUtil.getSessionFactory().openSession();
            Query q = session.createQuery("from Admin where netid = :netid");
            q.setParameter("netid", netid);
            List<Admin> list =  q.list();
            
            if (list != null && list.size() > 0)
            {
                return list.get(0).getAdminCode();
            }
            else
            {
                return null;
            }
        }
        catch (HibernateException e)
        {
            throw new HibernateException(e);
        }       
        finally
        {
            if (session!=null)
            {
                session.close();
            }
        }
    }
}
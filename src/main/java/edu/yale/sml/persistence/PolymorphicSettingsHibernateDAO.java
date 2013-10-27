package edu.yale.sml.persistence;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import edu.yale.sml.persistence.config.HibernateSQLServerUtil;
import edu.yale.sml.view.SettingsView;

public final class PolymorphicSettingsHibernateDAO extends GenericHibernateDAO<SettingsView.Settings>  implements java.io.Serializable, SettingsDAO
{
	private static final long serialVersionUID = -481304207357582739L;

	public PolymorphicSettingsHibernateDAO()
	{
	    super();
	}

	public void updateAll(List<SettingsView.Settings> list)
	{
		Session s = HibernateSQLServerUtil.getSessionFactory().openSession();
		Transaction t = s.beginTransaction();
		try
        {
		    for (SettingsView.Settings l : list)
            {
            	SettingsView.Settings oldObj = (SettingsView.Settings) s.load(SettingsView.Settings.class, l.getID());
            	oldObj.setVALUE(l.getVALUE());
            	oldObj.setTOOLTIP(l.getTOOLTIP());            	
            }
            s.flush();
            t.commit();
        }
        catch (HibernateException e)
        {
            e.printStackTrace();
            try
            {
                t.rollback();
            }
            catch (Throwable r)
            {
            	r.printStackTrace();
            }
        }
		finally
		{
		    s.close();
		}
	}

}
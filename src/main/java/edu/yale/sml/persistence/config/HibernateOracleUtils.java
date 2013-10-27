package edu.yale.sml.persistence.config;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateOracleUtils
{

	private static SessionFactory sessionFactory = null;

	private static SessionFactory buildSessionFactory()
	{
		try
		{
			return new Configuration().configure("shelfscan.oracle.hibernate.cfg.xml")
					.buildSessionFactory();
		}
		catch (Throwable ex)
		{
		    ex.printStackTrace();
			throw new ExceptionInInitializerError(ex);
		}
	}

	public static SessionFactory getSessionFactory()
	{
		if (sessionFactory == null)
		{
			sessionFactory = buildSessionFactory();
		}
		else
		{
		}
		return sessionFactory;
	}

	public static void shutdown()
	{
	    if (sessionFactory == null)
		{
			return;
		}
		
		try
		{
			getSessionFactory().close();
			sessionFactory = null; 
		}
		catch (HibernateException he)
		{
			throw new HibernateException(he);
		}
	}
}
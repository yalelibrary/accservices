package edu.yale.sml;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used for integration test. Move to test folder.
 * Only difference is that it doesn't invoke the Lucene index in Configure()
 */
public class HibernateSQLServerUtilTest
{

    final static Logger logger = LoggerFactory.getLogger(HibernateSQLServerUtilTest.class);

    private static final SessionFactory sessionFactory = buildSessionFactory();	
    	
	private static SessionFactory buildSessionFactory()
	{
	
	 	logger.debug("Building Hibernate Session Factory");

		try
		{
			Configuration configuration = new Configuration();
            return new Configuration().configure("shelfscan.sqlserver.hibernate.cfg.xml").buildSessionFactory();
       }
		catch (Throwable ex)
		{
			logger.error("Exception encountered while building session factory");
            ex.printStackTrace();
			throw new ExceptionInInitializerError(ex); //or ex.
		}
	}

	public static SessionFactory getSessionFactory()
	{
		return sessionFactory;
	}

	public static void shutdown()
	{
		logger.debug("Shutting down Hibernate Session Factory");
		try
		{
			getSessionFactory().close();
		}
		catch (HibernateException he)
		{
			throw new HibernateException(he);
		}
	}
}
package edu.yale.sml.persistence.config;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HibernateSQLServerUtil {

    final static Logger logger = LoggerFactory.getLogger(HibernateSQLServerUtil.class);

    private static final SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {

        logger.debug("Building Hibernate Session Factory");

        try {
            Configuration configuration = new Configuration();
            configuration.configure("shelfscan.sqlserver.hibernate.cfg.xml");
            ServiceRegistryBuilder serviceRegistryBuilder = new ServiceRegistryBuilder().applySettings(configuration
                    .getProperties());
            SessionFactory sessionFactory = configuration
                    .buildSessionFactory(serviceRegistryBuilder.buildServiceRegistry());
            return sessionFactory;
        } catch (Throwable ex) {
            logger.error("Exception encountered while building session factory");
            ex.printStackTrace();
            throw new ExceptionInInitializerError(ex); //or ex.
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void shutdown() {
        logger.debug("Shutting down Hibernate Session Factory");
        try {
            getSessionFactory().close();
        } catch (HibernateException he) {
            throw new HibernateException(he);
        }
    }
}
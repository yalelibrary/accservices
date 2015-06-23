package edu.yale.sml.persistence.config;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShelvingLiveRowCountSQLServerUtil {

    private final static Logger logger = LoggerFactory.getLogger(ShelvingLiveRowCountSQLServerUtil.class);

    private static final SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        logger.debug("Building Shelving Live Row Count Session Factory . . .");

        try {
            final Configuration config = new Configuration();
            config.configure("shelvingcount.sqlserver.hibernate.cfg.xml");
            ServiceRegistryBuilder serviceRegistryBuilder =
                    new ServiceRegistryBuilder().applySettings(config.getProperties());
            SessionFactory sessionFactory = config.buildSessionFactory(serviceRegistryBuilder.buildServiceRegistry());
            return sessionFactory;
        } catch (Throwable t) {
            logger.error("Exception encountered while building session factory", t);
            throw new ExceptionInInitializerError(t);
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
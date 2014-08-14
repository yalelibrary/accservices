/**
 *
 */
package edu.yale.sml.servlet;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import edu.yale.sml.persistence.config.HibernateSQLServerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppContextListener implements ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(AppContextListener.class);

    private static long start = 0;

    /*   Checking SessionFactory ensures that all database errors are caught and handled before the landing page is displayed.
     *   Only build and test factory for MySql. Oracle/Voyager is not hit until the user actually runs the report. 
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("Application Start up.");
        try {
            start = HibernateSQLServerUtil.getSessionFactory().getStatistics().getStartTime();
            logger.info("OK. Built Session Factory");
        } catch (Throwable t) {
            logger.error("Error in context initialization", t);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            HibernateSQLServerUtil.shutdown();
            logger.info("Closed Hibernate Session Factory. Time Usage={} ms ", (System.currentTimeMillis() - start));

        } catch (Throwable t) {
            logger.error("Error in context shutdown", t);
        }
    }

}

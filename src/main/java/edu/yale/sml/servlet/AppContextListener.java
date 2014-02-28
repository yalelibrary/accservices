/**
 *
 */
package edu.yale.sml.servlet;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import edu.yale.sml.persistence.config.HibernateSQLServerUtil;

/**
 * Init Central place for Hibernate MySql set up, e.g.
 *
 * @author od26
 */
public class AppContextListener implements ServletContextListener {

    public static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AppContextListener.class);
    static long start = 0;

    /*   Checking SessionFactory ensures that all database errors are caught and handled before the landing page is displayed.
     *   Only build and test factory for MySql. Oracle/Voyager is not hit until the user actually runs the report. 
	 *
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("Application Start up.");
        try {
            start = HibernateSQLServerUtil.getSessionFactory().getStatistics().getStartTime();
            logger.info("OK. Built Session Factory");
        } catch (Throwable t) {
            logger.error("Error in context initialization", t);
            t.printStackTrace();
        }
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            HibernateSQLServerUtil.shutdown();
            logger.info("Closed Hibernate Session Factory. Time Usage : " + (System.currentTimeMillis() - start) + " ms");

        } catch (Throwable t) {
            logger.error("Error in context shutdown", t);
            t.printStackTrace();
        }
    }

}

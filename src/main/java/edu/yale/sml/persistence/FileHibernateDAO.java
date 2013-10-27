package edu.yale.sml.persistence;

import java.util.List;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.yale.sml.model.InputFile;
import edu.yale.sml.persistence.config.HibernateSQLServerUtil;

/**
 * @author od26
 * 
 */
@SuppressWarnings("rawtypes")
public class FileHibernateDAO extends GenericHibernateDAO implements FileDAO
{

    final static Logger logger = LoggerFactory.getLogger(FileHibernateDAO.class);

    @SuppressWarnings("unchecked")
    @Override
    public String findByMD5(String md5)
    {
        Session session = null;
        try
        {
            session = HibernateSQLServerUtil.getSessionFactory().openSession();
            Query q = session.createQuery("select name from InputFile where md5 = :md5");
            q.setParameter("md5", md5);
            List<InputFile> l = q.list();

            if (l != null && l.size() > 0)
            {
                return l.get(0).getName().toString();
            }
            return null;
        }
        catch (HibernateException e)
        {
            throw new HibernateException(e);
        }
        finally
        {
            if (session != null)
                session.close();
        }
    }

    @Override
    public List findInputFileByMD5(String md5)
    {
        Session session = null;

        try
        {
            session = HibernateSQLServerUtil.getSessionFactory().openSession();
            Query q = session.createQuery("from edu.yale.sml.model.InputFile where md5 = :md5");
            q.setParameter("md5", md5);
            List l = q.list();

            if (l != null && l.size() > 0)
            {
                return l;
            }
            else
            {
            }
            return null;
        }
        catch (Throwable t)
        {
            logger.debug("Exception finding file by MD5");
            logger.error("Exception", t);
            throw new HibernateException(t);
        }
        finally
        {
            if (session != null)
                session.close();
        }
    }

    //TODO Refactor if search feature is used often. Currently, session is created anytime there's serch (which is ok for minimal use)
    @Override
    public List<InputFile> search(String queryString) throws Throwable
    {

        List<InputFile> itemList = null;
        Session session = null;

        try
        {

            logger.debug("\nObtaining session for Hibernate Session (Lucene).");
            session = HibernateSQLServerUtil.getSessionFactory().openSession();
            //logger.debug("\nInst. FullTextSession.");

            org.hibernate.search.FullTextSession fullTextSession = Search.getFullTextSession(session);
            //logger.debug("\nInst. Build Query for entity.");

            org.hibernate.search.query.dsl.QueryBuilder queryBuilder = fullTextSession.getSearchFactory().buildQueryBuilder().forEntity(InputFile.class).get();
            //logger.debug("\nInst. Lucene Query.");

            org.apache.lucene.search.Query luceneQuery = queryBuilder.keyword().onFields("contents").matching(queryString).createQuery();

            // wrap Lucene query in a javax.persistence.Query
            //logger.debug("\nInst. Full Text Query");

            org.hibernate.Query fullTextQuery = fullTextSession.createFullTextQuery(luceneQuery, InputFile.class);

            //logger.debug("\nInst. Query List.");

            itemList = fullTextQuery.list();

            //logger.debug("\nClosing Lucene Session.");

            fullTextSession.close();

            logger.debug("Closed lucene sensesion");

            //logger.debug("\nReturning list size : " + itemList.size());
        }
        catch (Throwable t)
        {
            // TODO Auto-generated catch block
            t.printStackTrace();
            logger.debug("Exception in Hibernate Search");
            logger.debug("Message :   " + t.getMessage());
            throw t;
        }
        finally
        {
        }

        return itemList;
    }

    @Override
    public void doIndex() throws Throwable
    {
        logger.debug("FileHIbernateDAO: In build index()");
        Session session = null;
        try
        {
            logger.debug("FileHIbernateDAO: Opening session\n");
            session = HibernateSQLServerUtil.getSessionFactory().openSession();
            logger.debug("FileHIbernateDAO: Getting full text session");

            FullTextSession fullTextSession = Search.getFullTextSession(session);
            logger.debug("FileHIbernateDAO: Got full text session");
            logger.debug("FileHIbernateDAO: Creating Indexer. . .");
            
            //NOTE -- ENSURE MIN. NUMBER OF THREADS FOR POOL, OTHERWISE IT HANGS
            
            fullTextSession.createIndexer().startAndWait();
            logger.debug("FileHIbernateDAO: Created Index and startandWait()  ... OK");

            if (fullTextSession.isOpen())
            {
                logger.debug("skipping Closing Full Text session");
            }

        }
        catch (Throwable e)
        {
            logger.debug("Exception in doIdnex");
            logger.debug("Message : " + e.getMessage());
            logger.debug("Cause:" + e.getCause());
            e.printStackTrace();
            throw e;
        }

        finally
        {
            if (session != null)
            {
                session.close();
            }
        }
    }

    @Override
    public InputFile findInputFileById(Integer fileId)
    {
        Session session = null;
        try
        {
            session = HibernateSQLServerUtil.getSessionFactory().openSession();
            Query q = session.createQuery("from InputFile where id = " + fileId);
            List<InputFile> inputFile = q.list();
            return inputFile.get(0);
        }
        catch (Throwable e)
        {
            System.out.println("Exception finding inputfile by id");
            throw new HibernateException(e);
        }
        finally
        {
            if (session != null)
            {
                session.close();
            }
        }
    }

    @Override
    public List<String> findByName()
    {
        Session session = null;
        try
        {
            session = HibernateSQLServerUtil.getSessionFactory().openSession();
            Query q = session.createQuery("select new list (f.name) from edu.yale.sml.model.InputFile f");
            List inputFileNameList = q.list();
            return inputFileNameList;
        }
        catch (Throwable e)
        {
            System.out.println("Exception finding inputfile by id");
            throw new HibernateException(e);
        }
        finally
        {
            if (session != null)
            {
                session.close();
            }
        }
    }

    @Override
    public String findContentsByFileName(String fileName)
    {
        Session session = null;
        try
        {
            session = HibernateSQLServerUtil.getSessionFactory().openSession();
            Query q = session.createQuery("select f from edu.yale.sml.model.InputFile f where f.name = :fileName");
            q.setParameter("fileName", fileName);
            InputFile inputFile = (InputFile) q.list().get(0);
            return inputFile.getContents();
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            throw new HibernateException(e);
        }
        finally
        {
            if (session != null)
            {
                session.close();
            }
        }
    }

    //TODO merge with findById. There should be a generic method. @see test method below
    @Override
    public InputFile findInputFileByName(String fileName)
    {
        Session session = null;
        try
        {
            session = HibernateSQLServerUtil.getSessionFactory().openSession();
            Query q = session.createQuery("from InputFile f where f.name = :fileName");
            q.setParameter("fileName", fileName);
            List<InputFile> inputFile = q.list();
            return inputFile.get(0);   //get only the first. how to SELECT (1)
        }
        catch (Throwable e)
        {
            System.out.println("Exception finding inputfile by id");
            throw new HibernateException(e);
        }
        finally
        {
            if (session != null)
            {
                session.close();
            }
        }
    }
    
    //tmp test
    public <T> InputFile findInputFileByField(T arg, String prop)
    {
        Session session = null;
        try
        {
            session = HibernateSQLServerUtil.getSessionFactory().openSession();
            Query q = session.createQuery("from InputFile f where f." + prop + "= :fileName");
            q.setParameter(prop, arg);
            List<InputFile> inputFile = q.list();
            return inputFile.get(0);   //get only the first. how to SELECT (1)

        }
        catch (Throwable e)
        {
            System.out.println("Exception finding inputfile by id");
            throw new HibernateException(e);
        }
        finally
        {
            if (session != null)
            {
                session.close();
            }
        }
    }
}

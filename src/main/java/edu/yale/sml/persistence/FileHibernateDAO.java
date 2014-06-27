package edu.yale.sml.persistence;

import edu.emory.mathcs.backport.java.util.Collections;
import edu.yale.sml.model.InputFile;
import edu.yale.sml.persistence.config.HibernateSQLServerUtil;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.slf4j.Logger;

import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;


@SuppressWarnings("rawtypes")
public class FileHibernateDAO extends GenericHibernateDAO implements FileDAO {

    private Logger logger = getLogger(this.getClass());

    @Override
    public List findInputFileByMD5(String md5) {
        Session session = null;

        try {
            session = HibernateSQLServerUtil.getSessionFactory().openSession();
            Query q = session.createQuery("from edu.yale.sml.model.InputFile where md5 = :md5");
            q.setParameter("md5", md5);
            List l = q.list();

            if (l != null && l.size() > 0) {
                return l;
            } else {
            }
            return null;
        } catch (Throwable t) {
            logger.error("Exception", t);
            throw new HibernateException(t);
        } finally {
            if (session != null)
                session.close();
        }
    }

    @Override
    public List<InputFile> search(String queryString) throws Throwable {

        List<InputFile> itemList;

        try {

            logger.trace("\nObtaining session for Hibernate Session (Lucene).");

            Session session = HibernateSQLServerUtil.getSessionFactory().openSession();

            logger.trace("Inst. FullTextSession.");

            FullTextSession fullTextSession = Search.getFullTextSession(session);

            logger.trace("Inst. Build Query for entity.");

            QueryBuilder queryBuilder = fullTextSession.getSearchFactory().buildQueryBuilder().forEntity(InputFile.class).get();

            logger.trace("Inst. Lucene Query.");

            org.apache.lucene.search.Query luceneQuery = queryBuilder.keyword().onFields("contents").matching(queryString).createQuery();

            // wrap Lucene query in a javax.persistence.Query
            logger.trace("Inst. Full Text Query");

            org.hibernate.Query fullTextQuery = fullTextSession.createFullTextQuery(luceneQuery, InputFile.class);

            logger.trace("Inst. Query List.");

            itemList = fullTextQuery.list();

            logger.trace("Closing Lucene Session.");

            fullTextSession.close();

            logger.debug("Closed lucene session");
        } catch (Throwable t) {
            logger.error("Error in search", t);
            throw t;
        }

        return (itemList == null) ? Collections.emptyList() : itemList;
    }

    @Override
    public void doIndex() throws Throwable {
        logger.debug("Building index");

        Session session = null;
        try {

            logger.trace("FileHIbernateDAO: Opening session\n");

            session = HibernateSQLServerUtil.getSessionFactory().openSession();

            logger.trace("FileHIbernateDAO: Getting full text session");

            FullTextSession fullTextSession = Search.getFullTextSession(session);

            logger.trace("FileHIbernateDAO: Got full text session");
            logger.trace("FileHIbernateDAO: Creating Indexer. . .");

            //N.B. -- ENSURE MIN. NUMBER OF THREADS FOR POOL

            fullTextSession.createIndexer().startAndWait();

            logger.debug("Created Index  ... OK");

            if (fullTextSession.isOpen()) {
                logger.debug("skipping Closing Full Text session");
            }

        } catch (Throwable e) {
            logger.error("Error", e);
            throw e;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public InputFile findInputFileById(Integer fileId) {
        Session session = null;
        try {
            session = HibernateSQLServerUtil.getSessionFactory().openSession();
            Query q = session.createQuery("from InputFile where id = " + fileId);
            List<InputFile> inputFile = q.list();
            return inputFile.get(0);
        } catch (Throwable e) {
            logger.error("Exception finding inputfile by id");
            throw new HibernateException(e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public List<String> findByName() {
        Session session = null;
        try {
            session = HibernateSQLServerUtil.getSessionFactory().openSession();
            Query q = session.createQuery("select new list (f.name) from edu.yale.sml.model.InputFile f");
            List inputFileNameList = q.list();
            return inputFileNameList;
        } catch (Throwable e) {
            logger.error("Exception finding inputfile by id");
            throw new HibernateException(e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public String findContentsByFileName(String fileName) {
        Session session = null;
        try {
            session = HibernateSQLServerUtil.getSessionFactory().openSession();
            Query q = session.createQuery("select f from edu.yale.sml.model.InputFile f where f.name = :fileName");
            q.setParameter("fileName", fileName);
            InputFile inputFile = (InputFile) q.list().get(0);
            return inputFile.getContents();
        } catch (Throwable e) {
            logger.error("Error", e);
            throw new HibernateException(e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

}

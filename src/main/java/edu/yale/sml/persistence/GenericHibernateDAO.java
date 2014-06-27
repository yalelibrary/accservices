package edu.yale.sml.persistence;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.yale.sml.persistence.config.HibernateSQLServerUtil;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Generic DAO for all entities, except Voyager
 *
 * @param <T>
 */

//TODO delete(List<T>) needs tx handling if necessary. also remove tx from save()
public class GenericHibernateDAO<T> implements GenericDAO<T> {

    private Logger logger = getLogger(this.getClass());

    Class<T> persistentClass;

    public GenericHibernateDAO() {
        super();
    }

    @Override
    public void delete(List<T> items) throws Throwable {
        Session s = null;
        try {
            s = HibernateSQLServerUtil.getSessionFactory().openSession();
            Transaction t = s.beginTransaction();
            for (T item : items) {
                s.delete(item);
            }
            s.flush();
            t.commit();
        } catch (Throwable t) {
            throw t;
        } finally {
            if (s != null) {
                s.close();
            }
        }

    }

    @Override
    public void delete(T object) throws Throwable {
        Session s = null;
        try {
            s = HibernateSQLServerUtil.getSessionFactory().openSession();
            Transaction t = s.beginTransaction();
            s.delete(object);
            s.flush();
            t.commit();
        } catch (Throwable t) {
            logger.error("Error", t);
            throw t;
        } finally {
            if (s != null) {
                s.close();
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<T> findAll(Class classz) throws Throwable {
        Session session = HibernateSQLServerUtil.getSessionFactory().openSession();
        try {
            Query q = session.createQuery("from " + classz.getName());
            List<T> list = q.list();
            return list;
        } catch (Throwable t) {
            logger.error("Exception in query.");
            throw t;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    /**
     * Finds all, sorted (desc) and up to a number
     *
     * @param classz
     * @param field
     * @return
     * @throws Throwable
     */
    @SuppressWarnings("unchecked")
    public List<T> findAllSorted(Class classz, String field, int limit) throws Throwable {
        Session session = HibernateSQLServerUtil.getSessionFactory().openSession();
        try {
            Query q = session.createQuery("from " + classz.getName() + " c order by c." + field + " desc");
            q.setMaxResults(limit);
            List<T> list = q.list();
            return list;
        } catch (Throwable t) {
            logger.error("Exception in query.");
            throw t;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public int findByLevelCount(Class classz, String level, String field) {
        Session session = null;
        try {
            session = HibernateSQLServerUtil.getSessionFactory().openSession();
            Query q = session.createQuery("select count(*) from " + classz.getName() + " l where l." + field + " = :param");
            q.setParameter("param", level);
            return ((Long) (q.uniqueResult())).intValue();

        } catch (Throwable e) {
            throw new HibernateException(e);
        } finally {
            session.close();
        }
    }

    public int findByLevelCount(Class classz, String level, String field, String level2, String field2) {
        Session session = null;
        try {
            session = HibernateSQLServerUtil.getSessionFactory().openSession();
            Query q = session.createQuery("select count(*) from  " + classz.getName() + " l where l." + field + " = :param and l." + field2 + " = :param2");
            q.setParameter("param", level);
            q.setParameter("param2", level2);
            return ((Long) (q.uniqueResult())).intValue();
        } catch (Throwable e) {
            throw new HibernateException(e);
        } finally {
            session.close();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<T> findPagedResult(Class classz, int first, int last, String orderClause) throws Throwable {
        Session session = HibernateSQLServerUtil.getSessionFactory().openSession();
        try {
            Query q = session.createQuery("from " + classz.getName() + " c order by " + orderClause);
            q.setFirstResult(first);
            q.setMaxResults(last);
            List<T> list = q.list();
            return list;
        } catch (Throwable t) {
            throw t;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public List findPagedResultByType(Class classz, int first, int last, String orderClause, String type, String field) throws Throwable {
        Session session = null;
        try {
            session = HibernateSQLServerUtil.getSessionFactory().openSession();
            Query q = session.createQuery("from " + classz.getName() + " c where c." + field + " = :param order by " + orderClause);
            q.setParameter("param", type);
            q.setFirstResult(first);
            q.setMaxResults(last);
            return q.list();
        } catch (Throwable t) {
            logger.error("Error", t);
            throw t;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public List findPagedResultByType(Class classz, int first, int last, String orderClause, String type, String field, String type2, String field2) throws Throwable {
        Session session = null;
        try {
            session = HibernateSQLServerUtil.getSessionFactory().openSession();
            Query q = session.createQuery("from " + classz.getName() + " c where c." + field + " = :param and c." + field2 + " =  :param2  order by " + orderClause);
            q.setParameter("param", type);
            q.setParameter("param2", type2);
            q.setFirstResult(first);
            q.setMaxResults(last);
            return q.list();
        } catch (Throwable t) {
            logger.error("Error", t);
            throw t;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public Integer save(T object) throws Throwable {
        Integer id = -1;
        Session s = null;
        Transaction tx = null;
        try {
            s = HibernateSQLServerUtil.getSessionFactory().openSession();
            tx = s.beginTransaction();
            id = (Integer) s.save(object);
            s.flush();
            tx.commit();
        } catch (Throwable t) {
            logger.debug("Exception saving item.");
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (Throwable rt) {
                logger.error("Error", rt);
            }
        } finally {
            if (s != null) {
                s.close();
            }
        }
        return id;
    }
}
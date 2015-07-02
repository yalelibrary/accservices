package edu.yale.sml.persistence;

import edu.yale.sml.model.Shelving;
import edu.yale.sml.model.ShelvingLiveRowCount;
import edu.yale.sml.persistence.config.HibernateSQLServerUtil;
import edu.yale.sml.persistence.config.ShelvingLiveRowCountSQLServerUtil;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;

import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

public final class ShelvingLiveRowCountHibernateDAO extends GenericHibernateDAO<ShelvingLiveRowCount> implements java.io.Serializable,
        ShelvingLiveRowCountDAO {

    private Logger logger = getLogger(this.getClass());

    private static final long serialVersionUID = -4044166542029569019L;

    public ShelvingLiveRowCountHibernateDAO() {
        super();
    }

    @Override
    public void update(ShelvingLiveRowCount shelvingLiveRowCount) {
        Session s = null;
        Transaction tx = null;
        try {
            s = ShelvingLiveRowCountSQLServerUtil.getSessionFactory().openSession();
            tx = s.beginTransaction();
            ShelvingLiveRowCount oldObj =
                    (ShelvingLiveRowCount) s.load(ShelvingLiveRowCount.class, shelvingLiveRowCount.getFloor());
            oldObj.setRows(shelvingLiveRowCount.getRows());
            oldObj.setLastUpdateTimeStamp(shelvingLiveRowCount.getLastUpdateTimeStamp());
            oldObj.setOldestCart(shelvingLiveRowCount.getOldestCart());
            oldObj.setOldestCartDated(shelvingLiveRowCount.getOldestCartDated());
            oldObj.setLastUpdateSystem(shelvingLiveRowCount.getLastUpdateSystem());
            s.flush();
            tx.commit();
        } catch (HibernateException e) {
            logger.error("Error", e);
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
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<ShelvingLiveRowCount> findById(String floor) {
        Session session = null;
        try {
            session = ShelvingLiveRowCountSQLServerUtil.getSessionFactory().openSession();
            logger.debug("Looking for floor:{}", floor);
            Query q = session.createQuery("from edu.yale.sml.model.ShelvingLiveRowCount where floor = :param");
            q.setParameter("param", floor);
            return q.list();
        } catch (HibernateException e) {
            logger.error("Error", e);
            throw new HibernateException(e);
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (HibernateException e) {
                logger.error("Error", e);
            }
        }
    }

    @Override
    public int count() {
        Session session = ShelvingLiveRowCountSQLServerUtil.getSessionFactory().openSession();
        try {
            return ((Long) session.createQuery("select count(*) from  edu.yale.sml.model.ShelvingLiveRowCount h")
                    .uniqueResult()).intValue();
        } catch (Throwable e) {
            throw new HibernateException(e);
        } finally {
            session.close();
        }
    }

}
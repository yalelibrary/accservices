package edu.yale.sml.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.transform.AliasToEntityMapResultTransformer;

import edu.yale.sml.model.SearchResult;
import edu.yale.sml.persistence.config.HibernateOracleUtils;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public final class BarcodeSearchDAO implements java.io.Serializable {

    private static final long serialVersionUID = -4044166542029569019L;

    private Logger logger = getLogger(this.getClass());

    public BarcodeSearchDAO() {
    }

    public List<SearchResult> findAllById(final List<String> barcodeList) {
        final List<SearchResult> searchResultList = new ArrayList<SearchResult>();
        Session session;

        try {
            session = HibernateOracleUtils.getSessionFactory().openSession();
        } catch (HibernateException e) {
            logger.error("Error", e);
            throw e;
        }

        try {
            try {
                for (String barcode : barcodeList) {
                    SQLQuery q = session.createSQLQuery("select b.ITEM_BARCODE, " +
                            "s.ITEM_ID, s.ITEM_STATUS_DATE, st.ITEM_STATUS_DESC, mi.YEAR, mi.CHRON," +
                            " mi.ITEM_ENUM, mi.MFHD_ID, mM.NORMALIZED_CALL_NO,"
                            + " mM.DISPLAY_CALL_NO, mM.SUPPRESS_IN_OPAC, mM.ENCODING_LEVEL, "
                            + "i.PERM_LOCATION, l.LOCATION_NAME, mM.CALL_NO_TYPE "
                            + " from YALEDB.ITEM i inner join YALEDB.ITEM_BARCODE b "
                            + "on i.ITEM_ID = b.ITEM_ID"
                            + " left outer join YALEDB.ITEM_STATUS s on i.ITEM_ID=s.ITEM_ID "
                            + " left outer join YALEDB.ITEM_STATUS_TYPE st on s.ITEM_STATUS = st.ITEM_STATUS_TYPE  "
                            + " left outer join YALEDB.MFHD_ITEM mI on i.ITEM_ID = mi.ITEM_ID"
                            + " left outer join YALEDB.MFHD_MASTER mM on mi.MFHD_ID = mM.MFHD_ID"
                            + " left outer join YALEDB.LOCATION l on i.PERM_LOCATION=l.LOCATION_ID"
                            + " where b.ITEM_BARCODE = :param");
                    q.setParameter("param", barcode);
                    q.setResultTransformer(AliasToEntityMapResultTransformer.INSTANCE);
                    SearchResult searchResult = new SearchResult(barcode, (ArrayList<Map<String, Object>>) q.list());
                    searchResultList.add(searchResult);
                }
            } catch (HibernateException e) {
                throw new HibernateException(e); // rethrow
            } finally {
                try {
                    session.close();
                } catch (HibernateException e) {
                    logger.error("Error closing session", e);
                }
            }
        } catch (HibernateException e) {
            throw new HibernateException(e);
        }
        return searchResultList;
    }
}
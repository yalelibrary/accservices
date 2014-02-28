package edu.yale.sml.persistence;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import edu.yale.sml.persistence.config.HibernateSQLServerUtil;
import edu.yale.sml.view.SettingsView;

public final class SettingsHibernateDAO extends GenericHibernateDAO<SettingsView.Settings> implements java.io.Serializable, SettingsDAO {
    private static final long serialVersionUID = -481304207357582739L;

    public SettingsHibernateDAO() {
        super();
    }

    //FIXME
    public void updateAll(List<SettingsView.Settings> objectList) {
        Session s = HibernateSQLServerUtil.getSessionFactory().openSession();
        Transaction t = s.beginTransaction();
        try {
            for (SettingsView.Settings<String> m : objectList) {
                @SuppressWarnings("unchecked")
                SettingsView.Settings<String> oldObj = (SettingsView.Settings<String>) s.load(SettingsView.Settings.class, m.getID());
                oldObj.setVALUE(m.getVALUE());
                oldObj.setTOOLTIP(m.getTOOLTIP());
            }
            s.flush();
            t.commit();
        } catch (HibernateException e) {
            e.printStackTrace();
            try {
                t.rollback();
            } catch (Throwable r) {
                r.printStackTrace();
            }
        } finally {
            s.close();
        }
    }

}
package edu.yale.sml.view;

import edu.yale.sml.model.Admin;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;


public class AdminViewTest {


    @Test
    public void testFindAll() throws Exception {
        //TODO
    }

    @Test
    public void testGetAdminAsList() throws Exception {
        AdminView adminView = new AdminView();
        List<Admin> adminList = new ArrayList<Admin>();
        adminList.add(new Admin());
        adminView.setAdminAsList(adminList);
        assert adminList.size() == 1;
    }

    @Test
    public void testGetAdminCatalog() throws Exception {
        AdminView adminView = new AdminView();
        Admin admin = new Admin("od", "od", "ADMIN");
        adminView.setAdminCatalog(admin);
        assert adminView.getAdminCatalog().getNetid().equals("od");
    }

    @Test
    public void testGetAdminCode() throws Exception {
        AdminView adminView = new AdminView();
        adminView.setAdminCode("ADMIN");
        assert (adminView.getAdminCode().equals("ADMIN"));
    }

    @Test
    public void testGetEditor() throws Exception {
        AdminView adminView = new AdminView();
        adminView.setEditor("od");
        String s = adminView.getEditor();
        assert (s.equals("od"));
    }

    @Test
    public void testGetNetid() throws Exception {
        AdminView adminView = new AdminView();
        adminView.setNetid("netid");
        assert (adminView.getNetid().equals("netid"));
    }

    @Test
    public void testGetPermissionTypes() throws Exception {
        AdminView adminView = new AdminView();
        List<String> permissionTypes = new ArrayList<String>();
        permissionTypes.add("Admin");
        permissionTypes.add("Student");
        adminView.setPermissionTypes(permissionTypes);
        assert (adminView.getPermissionTypes().size() == 2);
    }

    @Ignore("Until can find shelfscan.sqlserver.hibernate.cfg.xml")
    @Test
    public void testInitialize() throws Exception {
        AdminView adminView = new AdminView();
        try {
            adminView.initialize();
        } catch (Exception e) {
            //ignore
        }
        assert (adminView.getPermissionTypes().size() == 2);
    }

    @Test
    public void testSetAdminAsList() throws Exception {
        AdminView adminView = new AdminView();
        List<Admin> adminList = new ArrayList<Admin>();
        adminList.add(new Admin());
        adminView.setAdminAsList(adminList);
        assert adminList.size() == 1;
    }

    @Test
    public void testSetAdminCatalog() throws Exception {
        AdminView adminView = new AdminView();
        Admin admin = new Admin("od", "od", "ADMIN");
        adminView.setAdminCatalog(admin);
        assert adminView.getAdminCatalog().getNetid().equals("od");
    }

    @Test
    public void testSetAdminCode() throws Exception {
        AdminView adminView = new AdminView();
        adminView.setAdminCode("ADMIN");
        assert (adminView.getAdminCode().equals("ADMIN"));
    }

    @Test
    public void testSetEditor() throws Exception {
        AdminView adminView = new AdminView();
        adminView.setEditor("od");
        String s = adminView.getEditor();
        assert (s.equals("od"));
    }

    @Test
    public void testSetNetid() throws Exception {
        AdminView adminView = new AdminView();
        adminView.setNetid("netid");
        assert (adminView.getNetid().equals("netid"));
    }

    @Test
    public void testSetPermissionTypes() throws Exception {
        AdminView adminView = new AdminView();
        List<String> permissionTypes = new ArrayList<String>();
        permissionTypes.add("Admin");
        permissionTypes.add("Student");
        adminView.setPermissionTypes(permissionTypes);
        assert (adminView.getPermissionTypes().size() == 2);
    }
}

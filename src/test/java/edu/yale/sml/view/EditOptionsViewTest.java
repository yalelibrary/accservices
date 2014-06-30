package edu.yale.sml.view;

import org.junit.Test;


public class EditOptionsViewTest {

    @Test
    public void testGetRedirect_id() throws Exception {
        EditOptionsView editOptionsView = new EditOptionsView();
        editOptionsView.setRedirect_id("/shelfscan/index.xhtml");
        assert (editOptionsView.getRedirect_id().contains("/shelfscan/index.xhtml"));
    }

    @Test
    public void testSetRedirect_id() throws Exception {
        EditOptionsView editOptionsView = new EditOptionsView();
        editOptionsView.setRedirect_id("/shelfscan/index.xhtml");
        assert (editOptionsView.getRedirect_id().contains("/shelfscan/index.xhtml"));
    }
}

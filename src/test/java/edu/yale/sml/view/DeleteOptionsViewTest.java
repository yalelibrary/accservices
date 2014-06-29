package edu.yale.sml.view;

import org.junit.Test;

/**
 *
 */
public class DeleteOptionsViewTest {
    @Test
    public void testJump() throws Exception {

    }

    @Test
    public void testSetRedirect_id() throws Exception {
        DeleteOptionsView deleteOptionsView = new DeleteOptionsView();
        deleteOptionsView.setRedirect_id("/pages/index.xhtml");
        assert (deleteOptionsView.getRedirect_id().equals("/pages/index.xhtml"));
    }
}

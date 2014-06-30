package edu.yale.sml.view;

import edu.yale.sml.model.History;
import org.junit.Test;


public class EditHistoryViewTest {

    @Test
    public void testGetID() throws Exception {
        EditHistoryView editHistoryView = new EditHistoryView();
        editHistoryView.setID(3);
        assert (editHistoryView.getID() == 3);

    }

    @Test
    public void testSetID() throws Exception {
        EditHistoryView editHistoryView = new EditHistoryView();
        editHistoryView.setID(3);
        assert (editHistoryView.getID() == 3);

    }

    @Test
    public void testGetHistoryCatalog() throws Exception {
        EditHistoryView editHistoryView = new EditHistoryView();
        editHistoryView.setHistoryCatalog(new History());
        assert (editHistoryView.getHistoryCatalog() != null);
    }

    @Test
    public void testSetHistoryCatalog() throws Exception {
        EditHistoryView editHistoryView = new EditHistoryView();
        editHistoryView.setHistoryCatalog(new History());
        assert (editHistoryView.getHistoryCatalog() != null);
    }

}

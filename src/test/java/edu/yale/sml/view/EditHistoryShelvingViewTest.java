package edu.yale.sml.view;

import edu.yale.sml.model.Shelving;
import org.junit.Test;

public class EditHistoryShelvingViewTest {

    @Test
    public void testGetID() throws Exception {
        EditHistoryShelvingView editHistoryShelvingView = new EditHistoryShelvingView();
        editHistoryShelvingView.setID(2);
        assert (editHistoryShelvingView.getID() == 2);
    }

    @Test
    public void testGetHistoryCatalog() throws Exception {
        EditHistoryShelvingView editHistoryShelvingView = new EditHistoryShelvingView();
        editHistoryShelvingView.setHistoryCatalog(new Shelving());
        assert (editHistoryShelvingView.getHistoryCatalog() != null);
    }

    @Test
    public void testSetHistoryCatalog() throws Exception {
        EditHistoryShelvingView editHistoryShelvingView = new EditHistoryShelvingView();
        editHistoryShelvingView.setHistoryCatalog(new Shelving());
        assert (editHistoryShelvingView.getHistoryCatalog() != null);
    }

    @Test
    public void testSetID() throws Exception {
        EditHistoryShelvingView editHistoryShelvingView = new EditHistoryShelvingView();
        editHistoryShelvingView.setID(2);
        assert (editHistoryShelvingView.getID() == 2);
    }

}

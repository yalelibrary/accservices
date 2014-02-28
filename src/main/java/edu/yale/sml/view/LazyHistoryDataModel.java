package edu.yale.sml.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import edu.yale.sml.logic.HistoryComparator;
import edu.yale.sml.model.History;
import edu.yale.sml.persistence.GenericDAO;
import edu.yale.sml.persistence.GenericHibernateDAO;

/**
 * Used for History paginated table view
 */
public class LazyHistoryDataModel extends LazyDataModel<History> {
    final static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(LazyHistoryDataModel.class);

    private List<History> datasource;
    private int dataSourceSize = 0;

    /*
     * @Override public Object getRowKey(History History) { return History.getModel(); }
     */
    @Override
    public List<History> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, String> filters) {
        int result_count = dataSourceSize;
        List<History> data = new ArrayList<History>();
        boolean filtered = false;
        int filteredCount = 0;
        GenericDAO<History> dao = new GenericHibernateDAO<History>();

        try {
            if (sortField != null && sortOrder == SortOrder.ASCENDING) {
                datasource = dao.findPagedResult(History.class, first, first + pageSize, "c." + sortField + " asc");
            } else if (sortField != null && sortOrder == SortOrder.DESCENDING) {
                datasource = dao.findPagedResult(History.class, first, first + pageSize, "c." + sortField + " desc");
            } else {
                if (filters.get("NETID") != null && filters.size() == 1) {
                    datasource = (List<History>) dao.findPagedResultByType(History.class, first, first + pageSize, "c.RUNDATE desc", filters.get("NETID").toString(), "NETID");
                    // count of all such
                    filteredCount = dao.findByLevelCount(History.class, filters.get("NETID").toString(), "NETID");
                    filtered = true;
                } else if (filters.get("SCANLOCATION") != null && filters.size() == 1) {
                    datasource = (List<History>) dao.findPagedResultByType(History.class, first, first + pageSize, "c.RUNDATE desc", filters.get("SCANLOCATION").toString(), "SCANLOCATION");
                    // count of all such
                    filteredCount = dao.findByLevelCount(History.class, filters.get("SCANLOCATION").toString(), "SCANLOCATION");
                    filtered = true;
                } else if (filters.get("SCANLOCATION") != null && filters.get("NETID") != null && filters.size() == 2) {
                    datasource = (List<History>) dao.findPagedResultByType(History.class, first, first + pageSize, "c.RUNDATE desc", filters.get("SCANLOCATION").toString(), "SCANLOCATION", filters.get("NETID"), "NETID");
                    // count of all such
                    filteredCount = dao.findByLevelCount(History.class, filters.get("NETID").toString(), "NETID", filters.get("SCANLOCATION").toString(), "SCANLOCATION");
                    filtered = true;
                } else {
                    datasource = (List<History>) dao.findPagedResult(History.class, first, first + pageSize, "c.RUNDATE desc");
                }
            }
        } catch (Throwable e1) {
            e1.printStackTrace();
        }

        if (sortField != null) {
            Collections.sort(data, new HistoryComparator(sortField, sortOrder));
        }

        for (History history : datasource) {
            boolean match = true;
            for (Iterator<String> it = filters.keySet().iterator(); it.hasNext(); ) {
                try {
                    String filterProperty = it.next();
                    String filterValue = filters.get(filterProperty);
                    Object h = history.getClass().getDeclaredField(filterProperty.trim()).get(history);
                    String fieldValue = String.valueOf(history.getClass().getDeclaredField(filterProperty.trim()).get(history));
                    if (filterValue == null || fieldValue.startsWith(filterValue)) {
                        match = true;
                    } else {
                        match = false;
                        break;
                    }
                } catch (NoSuchFieldException e) {
                    match = false;
                } catch (NullPointerException n) {
                    match = false;
                } catch (IllegalAccessException i1) {
                    match = false;
                } catch (IllegalArgumentException i2) {
                    match = false;
                } catch (java.lang.ExceptionInInitializerError itr) {
                    match = false;
                } catch (Throwable t) {
                    match = false;
                }
            }
            if (match) {
                data.add(history);
            }
        }
        int dataSize = result_count;

        if (filtered) {
            this.setRowCount(filteredCount);
        } else {
            this.setRowCount(dataSize);
        }

        return data;
    }

    public LazyHistoryDataModel(List<History> datasource) {
        this.datasource = datasource;
    }

    public LazyHistoryDataModel(int historyAsListSize) {
        this.dataSourceSize = historyAsListSize;
    }
}

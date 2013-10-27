package edu.yale.sml.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import edu.yale.sml.logic.ShelvingComparator;
import edu.yale.sml.model.Shelving;
import edu.yale.sml.persistence.GenericDAO;
import edu.yale.sml.persistence.GenericHibernateDAO;

/**
 * Used for History paginated table view
 */
public class LazyHistoryShelvingDataModel extends LazyDataModel<Shelving>
{

    private List<Shelving> datasource;
    private int dataSourceSize = 0;

    final static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(LazyHistoryShelvingDataModel.class);

    public LazyHistoryShelvingDataModel(List<Shelving> datasource)
    {
        this.datasource = datasource;
    }

 

    public LazyHistoryShelvingDataModel(int historyAsListSize)
    {
        this.dataSourceSize = historyAsListSize;
    }

   
    @Override
    public List<Shelving> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, String> filters)
    {



        int result_count = dataSourceSize;
        List<Shelving> data = new ArrayList<Shelving>();

        GenericDAO<Shelving> dao = new GenericHibernateDAO<Shelving>();
        try
        {
            //TODO results in DB hit , but this could be a generic LazyDataModel 'problem'
            if (sortField != null && sortOrder == SortOrder.ASCENDING)
            {
                datasource = dao.findPagedResult(Shelving.class, first, first + pageSize, "c." + sortField +" asc");

            }
            else if (sortField != null && sortOrder == SortOrder.DESCENDING)
            {
                datasource = dao.findPagedResult(Shelving.class, first, first + pageSize, "c." + sortField +" desc");
            }
            else
            {
                datasource = dao.findPagedResult(Shelving.class, first, first + pageSize, "c.creationDate desc");
            }
        }
        catch (Throwable e1)
        {
            e1.printStackTrace();
        }
        
        if (sortField != null)
        {
            //TODO merge with HistoryComparator
            Collections.sort(data, new ShelvingComparator(sortField, sortOrder));
        }
              
        for (Shelving history : datasource)
        {
            boolean match = true;
            for (Iterator<String> it = filters.keySet().iterator(); it.hasNext();)
            {
                try
                {
                    String filterProperty = it.next();
                    String filterValue = filters.get(filterProperty);
                    Object h = history.getClass().getDeclaredField(filterProperty.trim()).get(history);
                    String fieldValue = String.valueOf(history.getClass().getDeclaredField(filterProperty.trim()).get(history));
                    if (filterValue == null || fieldValue.startsWith(filterValue))
                    {
                        match = true;
                    }
                    else
                    {
                        match = false;
                        break;
                    }
                }
                catch (NoSuchFieldException e)
                {
                    match = false;
                }
                catch (NullPointerException n)
                {
                    match = false;
                }
                catch (IllegalAccessException i1)
                {
                    match = false;
                }
                catch (IllegalArgumentException i2)
                {
                    match = false;
                }
                catch (java.lang.ExceptionInInitializerError itr)
                {
                    match = false;
                }
                catch (Throwable t)
                {
                    match = false;
                }
            }
            if (match)
            {
                data.add(history);
            }
        }
        int dataSize = result_count;
        this.setRowCount(dataSize);
        return data;
    }
}

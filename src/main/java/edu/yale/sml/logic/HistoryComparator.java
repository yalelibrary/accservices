package edu.yale.sml.logic;

import java.util.Comparator;

import org.primefaces.model.SortOrder;
import org.slf4j.LoggerFactory;

import edu.yale.sml.model.History;
import edu.yale.sml.model.Report;

/**
 * Sorts / Compares just by Normalized Call Number
 */
public class HistoryComparator implements Comparator<History> // implements Comparator<Report>
{

    String sortField = "";
    SortOrder sortOrder;
    org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(HistoryComparator.class);

    public HistoryComparator(String sortField, SortOrder sortOrder)
    {
        super();
        this.sortField = sortField;
        this.sortOrder = sortOrder;
    }

    public int compare(History o1, History o2)
    {
        return o1.getRUNDATE().compareTo(o2.getRUNDATE());
    }

    public String getSortField()
    {
        return sortField;
    }

    public void setSortField(String sortField)
    {
        this.sortField = sortField;
    }
}

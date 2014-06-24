package edu.yale.sml.logic;

import java.util.Comparator;

import org.primefaces.model.SortOrder;

import edu.yale.sml.model.History;

public class HistoryComparator implements Comparator<History> {
    String sortField = "";
    SortOrder sortOrder;

    public HistoryComparator(String sortField, SortOrder sortOrder) {
        super();
        this.sortField = sortField;
        this.sortOrder = sortOrder;
    }

    public int compare(History o1, History o2) {
        return o1.getRUNDATE().compareTo(o2.getRUNDATE());
    }

    public String getSortField() {
        return sortField;
    }

    public void setSortField(String sortField) {
        this.sortField = sortField;
    }
}

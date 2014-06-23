package edu.yale.sml.logic;

import java.util.Comparator;

import org.primefaces.model.SortOrder;

import edu.yale.sml.model.Shelving;

/**
 * Sorts / Compares just by Normalized Call Number
 */
public class ShelvingComparator implements Comparator<Shelving> {

    String sortField = "";
    SortOrder sortOrder;

    public ShelvingComparator(String sortField, SortOrder sortOrder) {
        super();
        this.sortField = sortField;
        this.sortOrder = sortOrder;
    }

    public int compare(Shelving o1, Shelving o2) {
        return o1.getCreationDate().compareTo(o2.getCreationDate());
    }

    public String getSortField() {
        return sortField;
    }

    public void setSortField(String sortField) {
        this.sortField = sortField;
    }
}
package edu.yale.sml.persistence;

import java.util.Date;
import java.util.List;

import edu.yale.sml.model.History;

public interface HistoryDAO extends GenericDAO<History> {
    public abstract void update(History history);

    public abstract List<History> findById(Integer ID);

    public abstract List<List<Integer>> findByFileId(int id);

    public abstract List<List<Integer>> findByFileId(int id, Date scanStartDate, Date scanEndDate, Date runStartDate, Date runEndDate);

    public abstract int count();

    public abstract List<String> findUniqueNetIds();

    public abstract List<String> findUniqueLocations();

    int findUniqueLocationsOccurrence(String location);

    public abstract Number findUniqueNetIdOccurrence(String s);

    Number findMonthlyScanByNetId(String arg0, Date arg1, Date arg2);
}

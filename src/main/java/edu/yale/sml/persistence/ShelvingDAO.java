package edu.yale.sml.persistence;

import java.util.Date;
import java.util.List;
import edu.yale.sml.model.Shelving;

public interface ShelvingDAO extends GenericDAO<Shelving>
{
    public abstract void update(Shelving Shelving);

    public abstract List<Shelving> findById(Integer ID);

    public abstract List<List<Integer>> findByFileId(int id);

    public abstract List<List<Integer>> findByFileId(int id, Date scanStartDate, Date scanEndDate, Date runStartDate, Date runEndDate);

    public abstract int count();

    public abstract List<String> findUniqueNetIds();

    public abstract List<String> findUniqueLocations();

    int findUniqueLocationsOccurrence(String location);

    public abstract Number findUniqueNetIdOccurrence(String s);
}

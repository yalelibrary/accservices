package edu.yale.sml.persistence;

import edu.yale.sml.model.History;

import java.util.List;

public interface HistoryDAO extends GenericDAO<History> {
    public abstract void update(History history);

    public abstract List<History> findById(Integer ID);

    public abstract List<List<Integer>> findByFileId(int id);

    public abstract int count();

    public abstract List<String> findUniqueNetIds();

    public abstract List<String> findUniqueLocations();

}

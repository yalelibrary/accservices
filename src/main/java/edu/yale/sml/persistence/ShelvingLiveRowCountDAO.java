package edu.yale.sml.persistence;

import edu.yale.sml.model.ShelvingLiveRowCount;

import java.util.List;

public interface ShelvingLiveRowCountDAO extends GenericDAO<ShelvingLiveRowCount> {
    public abstract void update(ShelvingLiveRowCount s);

    public abstract List<ShelvingLiveRowCount> findById(String floor);

   public abstract int count();

}

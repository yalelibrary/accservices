package edu.yale.sml.persistence;

import edu.yale.sml.model.Shelving;

import java.util.List;

public interface ShelvingDAO extends GenericDAO<Shelving> {
    public abstract void update(Shelving Shelving);

    public abstract List<Shelving> findById(Integer ID);

   public abstract int count();

}

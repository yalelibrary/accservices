package edu.yale.sml.persistence;

import edu.yale.sml.model.Admin;

public interface AdminDAO extends GenericDAO<Admin> {

    public abstract String findByNetId(String netid);

}
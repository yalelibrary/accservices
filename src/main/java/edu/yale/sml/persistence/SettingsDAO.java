package edu.yale.sml.persistence;

import java.util.List;

import edu.yale.sml.view.SettingsView;

public interface SettingsDAO extends GenericDAO<SettingsView.Settings> {
    public void updateAll(List<SettingsView.Settings> messagesList);
}
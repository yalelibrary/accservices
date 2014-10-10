package edu.yale.sml.persistence;

import java.util.List;

import edu.yale.sml.model.Messages;

public interface MessagesDAO extends GenericDAO<Messages> {

    public void updateAll(List<Messages> messagesList);

}
package edu.yale.sml.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter @Setter
@ToString(callSuper=true, includeFieldNames=true)
public class Location implements java.io.Serializable {

    private static final long serialVersionUID = 5667310452313292689L;

    private Date date = new Date();

    private String editor = "";

    private Integer id;

    private String name = "";

    public Location() {
        super();
    }

    public Location(String name) {
        super();
        this.name = name;
    }

    public Location(String name, String editor, Date date) {
        super();
        this.name = name;
        this.editor = editor;
        this.date = date;
    }

    public String getName() {
        return name;
    }
}
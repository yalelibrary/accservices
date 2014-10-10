package edu.yale.sml.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter @Setter
@ToString(callSuper=true, includeFieldNames=true)
public class Floor implements java.io.Serializable {

    private static final long serialVersionUID = 5667310452313292689L;

    private Date date = new Date();

    private String editor = "";

    private Integer id;

    private String name = "";

    public Floor(Date date, String editor, Integer id, String name) {
        super();
        this.date = date;
        this.editor = editor;
        this.id = id;
        this.name = name;
    }

    public Floor() {
        super();
    }

    public Floor(String name, String editor, Date date) {
        this.date = date;
        this.editor = editor;
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
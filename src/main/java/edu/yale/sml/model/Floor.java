package edu.yale.sml.model;

import java.util.Date;

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

    public Date getDate() {
        return date;
    }

    public String getEditor() {
        return editor;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setEditor(String editor) {
        this.editor = editor;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Location [date=" + date + ", editor=" + editor + ", id=" + id + ", name=" + name + "]";
    }


}
package edu.yale.sml.model;

import java.util.Date;

public class Location implements java.io.Serializable {
	
	private static final long serialVersionUID = 5667310452313292689L;

	private Date date =  new Date();
    private String editor="";
	private Integer id;
	private String name = "";
	public Location()
	{
		super();
	}

	public Location(String name)
	{
		super();
		this.name = name;
	}

    public Location(String name, String editor, Date date)
    {
        super();
        this.name = name;
        this.editor = editor;
        this.date = date;
    }

    public Date getDate()
    {
        return date;
    }

    public String getEditor()
    {
        return editor;
    }

    public Integer getId()
    {
        return id;
    }

    public String getName()
	{
		return name;
	}

    public void setDate(Date date)
    {
        this.date = date;
    }   

    public void setEditor(String editor)
    {
        this.editor = editor;
    }

	public void setId(Integer id)
    {
        this.id = id;
    }

	public void setName(String name)
	{
		this.name = name;
	}

    @Override
    public String toString()
    {
        return "Location [date=" + date + ", editor=" + editor + ", id=" + id + ", name=" + name + "]";
    }	


}
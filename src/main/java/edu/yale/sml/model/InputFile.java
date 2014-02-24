package edu.yale.sml.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;

@Indexed
public class InputFile implements java.io.Serializable {

    private static final long serialVersionUID = -5760688122331029078L;
    private String author;
    private String contents;
    private Date date;
    //for JSF, used in HistorySearchView
    private List<String> historyList = new ArrayList<String>();
    private int id;
    private String md5;
    private String name;
    private String text;

    public InputFile() {
        super();
    }

    public InputFile(String name, String md5, String author, Date date,
                     String contents, String text) {
        super();
        this.name = name;
        this.md5 = md5;
        this.author = author;
        this.date = date;
        this.contents = contents;
        this.text = text;
    }

    public String getAuthor() {
        return author;
    }

    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    public String getContents() {
        return contents;
    }

    public Date getDate() {
        return date;
    }

    @DocumentId
    public int getId() {
        return id;
    }

    public String getMd5() {
        return md5;
    }

    public String getName() {
        return name;
    }

    public String getText() {
        return text;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<String> getHistoryList() {
        return historyList;
    }

    public void setHistoryList(List<String> historyList) {
        this.historyList = historyList;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "InputFile [id=" + id + ", name=" + name + ", md5=" + md5 + ", author=" + author + ", date=" + date + ", contents=" + contents + ", text=" + text + "]";
    }
}

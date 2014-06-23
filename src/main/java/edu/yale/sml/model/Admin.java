package edu.yale.sml.model;

public class Admin implements java.io.Serializable {

    private String adminCode;
    private String editor;
    private Integer id;
    private String netid;

    public Admin() {
    }

    public String getAdminCode() {
        return adminCode;
    }

    @Override
    public String toString() {
        return "Admin [adminCode=" + adminCode + ", editor=" + editor + ", id=" + id + ", netid=" + netid + "]";
    }

    public Admin(String netid, String editor, String adminCode) {
        super();
        this.netid = netid;
        this.editor = editor;
        this.adminCode = adminCode;
    }

    public String getEditor() {
        return this.editor;
    }

    public Integer getId() {
        return this.id;
    }

    public String getNetid() {
        return this.netid;
    }

    public void setAdminCode(String adminCode) {
        this.adminCode = adminCode;
    }

    public void setEditor(String editor) {
        this.editor = editor;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setNetid(String netid) {
        this.netid = netid;
    }
}

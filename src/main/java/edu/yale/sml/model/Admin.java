package edu.yale.sml.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter
@ToString(callSuper=true, includeFieldNames=true)
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

    public Admin(String netid, String editor, String adminCode) {
        super();
        this.netid = netid;
        this.editor = editor;
        this.adminCode = adminCode;
    }

    public String getNetid() {
        return netid;
    }

}

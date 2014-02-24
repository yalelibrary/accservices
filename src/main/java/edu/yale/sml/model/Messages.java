package edu.yale.sml.model;

public class Messages {

    private Integer ID;
    private String NAME = "";
    private String VALUE = ""; // what if it's value
    private String TOOLTIP = "";

    public Messages() {
        // TODO Auto-generated constructor stub
    }

    public Integer getID() {
        return ID;
    }

    public void setID(Integer iD) {
        ID = iD;
    }

    public Messages(String nAME, String vALUE, String purpose) {
        super();
        NAME = nAME;
        VALUE = vALUE;
        TOOLTIP = purpose;
    }

    public String getTOOLTIP() {
        return TOOLTIP;
    }

    public void setTOOLTIP(String tOOLTIP) {
        TOOLTIP = tOOLTIP;
    }

    @Override
    public String toString() {
        return "Messages [ID=" + ID + ", NAME=" + NAME + ", VALUE=" + VALUE + ", TOOLTIP=" + TOOLTIP + "]";
    }

    public String getNAME() {
        return NAME;
    }

    public void setNAME(String nAME) {
        NAME = nAME;
    }

    public String getVALUE() {
        return VALUE;
    }

    public void setVALUE(String vALUE) {
        VALUE = vALUE;
    }

}

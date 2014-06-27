package edu.yale.sml.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class History implements java.io.Serializable {
    private static final long serialVersionUID = -8463243297163676698L;
    private short ACCURACY;
    private Integer FILE_ID = 0;
    private Integer fileId = 0;
    private String FILENAME = "";
    private Set fileSet = new HashSet(0);
    private String FIRSTCALLNUMBER = "";
    private Integer ID;
    private InputFile inputFile; // FK many-to-one
    private String LASTCALLNUMBER = "";
    private String LOCATION = "";
    private int LOCATIONERROR = 0;
    private String MISLABELLED = "";
    private String NETID = "";
    private String NOTES = "";
    private int NULLBARCODE = 0;
    private String NUMBERBARCODE = "";
    private short NUMBERSCANNED;
    private short OVERSIZE = 0;
    private Date RUNDATE = new Date();
    private Date SCANDATE = new Date();
    private String SCANLOCATION = "";
    private byte[] SEARCHVIEW;
    private short STATUS;
    private short SUPPRESS;
    private short TIMESPENT = 0;
    @Deprecated
    private String TRANSCATIONMESSAGES;
    private String BARCODE_LAST;
    private String BARCODE_FIRST;
    private String NORM_CALL_LAST = "";
    private String NORM_CALL_FIRST = "";

    public History() {
        super();
    }

    public String getBARCODE_LAST() {
        return BARCODE_LAST;
    }

    public void setBARCODE_LAST(String bARCODE_LAST) {
        BARCODE_LAST = bARCODE_LAST;
    }

    public String getBARCODE_FIRST() {
        return BARCODE_FIRST;
    }

    public void setBARCODE_FIRST(String bARCODE_FIRST) {
        BARCODE_FIRST = bARCODE_FIRST;
    }

    public String getNORM_CALL_LAST() {
        return NORM_CALL_LAST;
    }

    public void setNORM_CALL_LAST(String nORM_CALL_LAST) {
        NORM_CALL_LAST = nORM_CALL_LAST;
    }

    public String getNORM_CALL_FIRST() {
        return NORM_CALL_FIRST;
    }

    public void setNORM_CALL_FIRST(String nORM_CALL_FIRST) {
        NORM_CALL_FIRST = nORM_CALL_FIRST;
    }

    public Integer getFILE_ID() {
        return FILE_ID;
    }

    public Integer getFileId() {
        return fileId;
    }

    public String getFILENAME() {
        return FILENAME;
    }

    public Set getFileSet() {
        return fileSet;
    }

    public String getFIRSTCALLNUMBER() {
        return FIRSTCALLNUMBER;
    }

    public Integer getId() {
        return ID;
    }

    public Integer getID() {
        return ID;
    }

    public InputFile getInputFile() {
        return inputFile;
    }

    public String getLASTCALLNUMBER() {
        return LASTCALLNUMBER;
    }

    public String getLOCATION() {
        return LOCATION;
    }

    public int getLOCATIONERROR() {
        return LOCATIONERROR;
    }

    public String getMISLABELLED() {
        return MISLABELLED;
    }

    public String getNETID() {
        return NETID;
    }

    public String getNOTES() {
        return NOTES;
    }

    public int getNULLBARCODE() {
        return NULLBARCODE;
    }

    public String getNUMBERBARCODE() {
        return NUMBERBARCODE;
    }

    public Date getRUNDATE() {
        return RUNDATE;
    }

    public Date getSCANDATE() {
        return SCANDATE;
    }

    public String getSCANLOCATION() {
        return SCANLOCATION;
    }

    public byte[] getSEARCHVIEW() {
        return SEARCHVIEW;
    }

    public short getTIMESPENT() {
        return TIMESPENT;
    }

    public String getTRANSCATIONMESSAGES() {
        return TRANSCATIONMESSAGES;
    }

    public void setFILE_ID(Integer fILE_ID) {
        FILE_ID = fILE_ID;
    }

    public void setFileId(Integer fileId) {
        this.fileId = fileId;
    }

    public void setFILENAME(String fILENAME) {
        FILENAME = fILENAME;
    }

    public void setFileSet(Set fileSet) {
        this.fileSet = fileSet;
    }

    public void setFIRSTCALLNUMBER(String fIRSTCALLNUMBER) {
        FIRSTCALLNUMBER = fIRSTCALLNUMBER;
    }

    public void setId(Integer id) {
        this.ID = id;
    }

    public void setID(Integer iD) {
        ID = iD;
    }

    public void setInputFile(InputFile inputFile) {
        this.inputFile = inputFile;
    }

    public void setLASTCALLNUMBER(String lASTCALLNUMBER) {
        LASTCALLNUMBER = lASTCALLNUMBER;
    }

    public void setLOCATION(String lOCATION) {
        LOCATION = lOCATION;
    }

    public void setLOCATIONERROR(int lOCATIONERROR) {
        LOCATIONERROR = lOCATIONERROR;
    }

    public void setMISLABELLED(String mISLABELLED) {
        MISLABELLED = mISLABELLED;
    }

    public void setNETID(String nETID) {
        NETID = nETID;
    }

    public void setNOTES(String nOTES) {
        NOTES = nOTES;
    }

    public void setNULLBARCODE(int nULLBARCODE) {
        NULLBARCODE = nULLBARCODE;
    }

    public void setNUMBERBARCODE(String nUMBERBARCODE) {
        NUMBERBARCODE = nUMBERBARCODE;
    }

    public void setRUNDATE(Date rUNDATE) {
        RUNDATE = rUNDATE;
    }

    public void setSCANDATE(Date sCANDATE) {
        SCANDATE = sCANDATE;
    }

    public void setSCANLOCATION(String sCANLOCATION) {
        SCANLOCATION = sCANLOCATION;
    }

    public void setSEARCHVIEW(byte[] sEARCHVIEW) {
        SEARCHVIEW = sEARCHVIEW;
    }

    public short getACCURACY() {
        return ACCURACY;
    }

    public void setACCURACY(short aCCURACY) {
        ACCURACY = aCCURACY;
    }

    public short getNUMBERSCANNED() {
        return NUMBERSCANNED;
    }

    public void setNUMBERSCANNED(short nUMBERSCANNED) {
        NUMBERSCANNED = nUMBERSCANNED;
    }

    public short getOVERSIZE() {
        return OVERSIZE;
    }

    public void setOVERSIZE(short oVERSIZE) {
        OVERSIZE = oVERSIZE;
    }

    public short getSTATUS() {
        return STATUS;
    }

    public void setSTATUS(short sTATUS) {
        STATUS = sTATUS;
    }

    public short getSUPPRESS() {
        return SUPPRESS;
    }

    public void setSUPPRESS(short sUPPRESS) {
        SUPPRESS = sUPPRESS;
    }

    public void setTIMESPENT(short tIMESPENT) {
        TIMESPENT = tIMESPENT;
    }

    @Override
    public String toString() {
        return "History [FILENAME=" + FILENAME + ", fileSet=" + fileSet + ", FIRSTCALLNUMBER=" + FIRSTCALLNUMBER
                + ", ID=" + ID + ", LASTCALLNUMBER=" + LASTCALLNUMBER + ", LOCATION=" + LOCATION + ", LOCATIONERROR=" +
                LOCATIONERROR + ", MISLABELLED=" + MISLABELLED + ", NETID=" + NETID + ", NOTES="
                + NOTES + ", NUMBERBARCODE=" + NUMBERBARCODE + ", NUMBERSCANNED=" + NUMBERSCANNED + ", OVERSIZE=" +
                OVERSIZE + ", RUNDATE=" + RUNDATE + ", SCANDATE=" + SCANDATE + "]";
    }

}
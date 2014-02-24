package edu.yale.sml.model;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.util.ArrayList;
import java.util.List;

public class DataLists implements java.io.Serializable {

    private static final long serialVersionUID = 2524269432605035990L;

    private List<OrbisRecord> catalogAsList = new ArrayList<OrbisRecord>();
    private List<OrbisRecord> catalogSortedRaw = new ArrayList<OrbisRecord>();
    private List<Report> culpritList = new ArrayList<Report>(); // for alt logic
    private List<OrbisRecord> markedCatalogAsList = new ArrayList<OrbisRecord>();
    private List<OrbisRecord> nullResultBarcodes = new ArrayList<OrbisRecord>();
    private List<Report> reportCatalogAsList = new ArrayList<Report>();
    private ShelvingError shelvingError = new ShelvingError();
    private List<OrbisRecord> suppressedList = new ArrayList<OrbisRecord>(); // of Report?
    private List<Report> enumWarnings = new ArrayList<Report>();
    private Multimap<String, String> barcodesAsMap = ArrayListMultimap.create(); //currently only {barcode:itemstatus}


    public Multimap<String, String> getBarcodesAsMap() {
        return barcodesAsMap;
    }

    public void setBarcodesAsMap(Multimap<String, String> barcodesAsMap) {
        this.barcodesAsMap = barcodesAsMap;
    }

    public List<Report> getEnumWarnings() {
        return enumWarnings;
    }

    public void setEnumWarnings(List<Report> enumWarnings) {
        this.enumWarnings = enumWarnings;
    }

    public DataLists() {
    }

    public List<OrbisRecord> getCatalogAsList() {
        return catalogAsList;
    }

    public List<OrbisRecord> getCatalogSortedRaw() {
        return catalogSortedRaw;
    }

    public List<Report> getCulpritList() {
        return culpritList;
    }

    public List<OrbisRecord> getMarkedCatalogAsList() {
        return markedCatalogAsList;
    }

    public List<Report> getReportCatalogAsList() {
        return reportCatalogAsList;
    }

    public ShelvingError getShelvingError() {
        return shelvingError;
    }

    public List<OrbisRecord> getSuppressedList() {
        return suppressedList;
    }

    public void setCatalogAsList(List<OrbisRecord> catalogAsList) {
        this.catalogAsList = catalogAsList;
    }

    public void setCatalogSortedRaw(List<OrbisRecord> catalogSortedRaw) {
        this.catalogSortedRaw = catalogSortedRaw;
    }

    public void setCulpritList(List<Report> culpritList) {
        this.culpritList = culpritList;
    }

    public void setMarkedCatalogAsList(List<OrbisRecord> markedCatalogAsList) {
        this.markedCatalogAsList = markedCatalogAsList;
    }

    public List<OrbisRecord> getNullResultBarcodes() {
        return nullResultBarcodes;
    }

    public void setNullResultBarcodes(List<OrbisRecord> nullResultBarcodes) {
        this.nullResultBarcodes = nullResultBarcodes;
    }

    public void setReportCatalogAsList(List<Report> reportCatalogAsList) {
        this.reportCatalogAsList = reportCatalogAsList;
    }

    public void setShelvingError(ShelvingError shelvingError) {
        this.shelvingError = shelvingError;
    }

    public void setSuppressedList(List<OrbisRecord> suppressedList) {
        this.suppressedList = suppressedList;
    }


    @Override
    public String toString() {
        return "DataLists{" +
                "catalogAsList=" + catalogAsList +
                ", catalogSortedRaw=" + catalogSortedRaw +
                ", culpritList=" + culpritList +
                ", markedCatalogAsList=" + markedCatalogAsList +
                ", nullResultBarcodes=" + nullResultBarcodes +
                ", reportCatalogAsList=" + reportCatalogAsList +
                ", shelvingError=" + shelvingError +
                ", suppressedList=" + suppressedList +
                ", enumWarnings=" + enumWarnings +
                '}';
    }


}
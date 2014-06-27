package edu.yale.sml.model;

import lombok.ToString;

import java.util.List;
import java.util.Map;

/**
 * Used by BarcodeSearchDAO and BasicShelfScanEngine
 */
@ToString(callSuper=true, includeFieldNames=true)
public class SearchResult {

    /** Barcode from InputFile */
    private String id = "";

    /** Corresponding search result */
    private List<Map<String, Object>> result = null;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Map<String, Object>> getResult() {
        return result;
    }

    public SearchResult(String id, List<Map<String, Object>> result) {
        super();
        this.id = id;
        this.result = result;
    }
}

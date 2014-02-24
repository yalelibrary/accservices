package edu.yale.sml.model;

import java.util.List;
import java.util.Map;

/**
 * Used by BarcodeSearchDAO and BasicShelfScanEngine
 */
public class SearchResult {

    private String id = ""; // barcode from input file
    private List<Map<String, Object>> result = null; // corresponding result

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Map<String, Object>> getResult() {
        return result;
    }

    public void setResult(List<Map<String, Object>> result) {
        this.result = result;
    }

    public SearchResult(String id, List<Map<String, Object>> result) {
        super();
        this.id = id;
        this.result = result;
    }

    @Override
    public String toString() {
        return "SearchResult [id=" + id + ", result=" + result + "]";
    }
}

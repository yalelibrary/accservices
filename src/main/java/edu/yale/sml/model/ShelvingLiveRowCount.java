package edu.yale.sml.model;

import java.io.Serializable;
import java.util.Date;

/**
 *
 */
public class ShelvingLiveRowCount implements Serializable {

    private String floor;

    private int trucks;

    private int rows;

    private Date oldestCart;

    private int newRows;

    private Date oldestCartDated;

    private Date lastUpdateTimeStamp;

    private String lastUpdateSystem;

    public ShelvingLiveRowCount() {

    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public int getTrucks() {
        return trucks;
    }

    public void setTrucks(int trucks) {
        this.trucks = trucks;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public Date getOldestCart() {
        return oldestCart;
    }

    public void setOldestCart(Date oldestCart) {
        this.oldestCart = oldestCart;
    }

    public int getNewRows() {
        return newRows;
    }

    public void setNewRows(int newRows) {
        this.newRows = newRows;
    }

    public Date getOldestCartDated() {
        return oldestCartDated;
    }

    public void setOldestCartDated(Date oldestCartDated) {
        this.oldestCartDated = oldestCartDated;
    }

    public Date getLastUpdateTimeStamp() {
        return lastUpdateTimeStamp;
    }

    public void setLastUpdateTimeStamp(Date lastUpdateTimeStamp) {
        this.lastUpdateTimeStamp = lastUpdateTimeStamp;
    }

    public String getLastUpdateSystem() {
        return lastUpdateSystem;
    }

    public void setLastUpdateSystem(String lastUpdateSystem) {
        this.lastUpdateSystem = lastUpdateSystem;
    }


    @Override
    public String toString() {
        return "ShelvingLiveRowCount{" +
                "floor='" + floor + '\'' +
                ", trucks=" + trucks +
                ", rows=" + rows +
                ", oldestCart=" + oldestCart +
                ", newRows=" + newRows +
                ", oldestCartDated=" + oldestCartDated +
                ", lastUpdateTimeStamp=" + lastUpdateTimeStamp +
                ", lastUpdateSystem='" + lastUpdateSystem + '\'' +
                '}';
    }
}

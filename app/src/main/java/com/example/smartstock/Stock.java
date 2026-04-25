package com.example.smartstock;

import com.google.firebase.Timestamp;

import java.io.Serializable;
import java.sql.Time;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Stock implements Serializable {
    private Item item;
    private int quantity;
    private int StockID;
    private Timestamp lastUpdate;
    private boolean addedToWarehouse;

    public Stock(){
        this.addedToWarehouse = false;
    }

    public Stock(Item item, int quantity) {
        this.item = item;
        this.quantity = quantity;
        this.StockID = item.getItemID();
        this.addedToWarehouse = false;
    }

    public Stock(int stockID, boolean addedToWarehouse, Item item) {
        this.StockID = stockID;
        this.addedToWarehouse = addedToWarehouse;
        this.item = item;
    }

    public void setAddedToWarehouse(boolean addedToWarehouse) {
        this.addedToWarehouse = addedToWarehouse;
    }
    public boolean getAddedToWarehouse(){
        return this.addedToWarehouse;
    }

    public int getStockID() {
        return StockID;
    }

    public int getQuantity() {
        return quantity;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setStockID(int stockID) {
        StockID = stockID;
    }
    public void setLastUpdate(){
        this.lastUpdate = Timestamp.now();
    }
    public void setLastUpdate(Object dateObj) {
        if (dateObj == null) {
            this.lastUpdate = null;
        } else if (dateObj instanceof Timestamp) {
            this.lastUpdate = (Timestamp) dateObj;
        } else if (dateObj instanceof Date) {
            this.lastUpdate = new Timestamp((Date) dateObj);
        } else {
            this.lastUpdate = Timestamp.now();
        }
    }

    public Timestamp getLastUpdate() {
        return lastUpdate;
    }

    public double calculateStockValue(){
        return item.getSellingPrice()*quantity;
    }

}

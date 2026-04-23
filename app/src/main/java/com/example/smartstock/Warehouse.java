package com.example.smartstock;

import java.io.Serializable;
import java.util.ArrayList;

public class Warehouse implements Serializable {
    private int WarehouseId;
    private String WarehouseName;
    private String WarehouseLocation;
    private Employee Manager; // Direct object type
    private ArrayList<Stock> stocks = new ArrayList<>(); // Direct object type

    public Warehouse(){
        // No-argument constructor required for Firebase
    }

    public Warehouse(int WarehouseId, String WarehouseName, String WarehouseLocation, Employee Manager, ArrayList<Stock> stocks){
        this.stocks = stocks;
        this.WarehouseId = WarehouseId;
        this.WarehouseName = WarehouseName;
        this.WarehouseLocation = WarehouseLocation;
        this.Manager = Manager;
    }

    // Getters and Setters
    public int getWarehouseId() {
        return WarehouseId;
    }

    public String getWarehouseName() {
        return WarehouseName;
    }

    public ArrayList<Stock> getStocks() {
        return stocks;
    }

    public String getWarehouseLocation() {
        return WarehouseLocation;
    }

    public Employee getManager() {
        return Manager;
    }

    public void setManager(Employee manager) {
        Manager = manager;
    }

    public void setWarehouseId(int warehouseId) {
        WarehouseId = warehouseId;
    }

    public void setStocks(ArrayList<Stock> stocks) {
        this.stocks = stocks;
    }

    public void setWarehouseName(String warehouseName) {
        WarehouseName = warehouseName;
    }

    public void setWarehouseLocation(String warehouseLocation) {
        this.WarehouseLocation = warehouseLocation;
    }
    public void addStock(Stock stock) {
        if (stocks == null) {
            stocks = new ArrayList<>();
        }
        stocks.add(stock);
    }
}
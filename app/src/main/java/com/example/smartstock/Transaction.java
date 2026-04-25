package com.example.smartstock;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;

public class Transaction implements Serializable {
    private int transactionId;
    private transient Stock stock;
    private transient User handledBy;
    private int quantity;
    private double price;
    private Date transactionDate;
    private String CustomerName;
    private String CustomerPhone;
    private String stockId;
    private String userId;

    public Transaction(){

    }
    public Transaction(int transactionId, Stock stock, User handledBy, int quantity, double price, Date transactionDate) {
        this.transactionId = transactionId;
        this.stock = stock;
        this.handledBy = handledBy;
        this.quantity = quantity;
        this.price = price;
        this.transactionDate = transactionDate;
    }

    public String getStockId() {
        return stock != null ? String.valueOf(stock.getStockID()) : stockId;
    }

    public void setStockId(String stockId) {
        this.stockId = stockId;
    }

    public String getUserId() {
        return handledBy != null ? handledBy.getEmail() : userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCustomerName() {
        return CustomerName;
    }

    public void setCustomerName(String customerName) {
        CustomerName = customerName;
    }

    public String getCustomerPhone() {
        return CustomerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        CustomerPhone = customerPhone;
    }

    public int getTransactionId() {
        return this.transactionId;
    }
    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }
    public Stock getStock() {
        return this.stock;
    }
    public void setStock(Stock stock) {
        this.stock = stock;
    }
    public User getHandledBy() {
        return this.handledBy;
    }
    public void setHandledBy(User handledBy) {
        this.handledBy = handledBy;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Date getTransactionDate() {
        return transactionDate;
    }
    public void setTransactionDate() {
        this.transactionDate = new Date();
    }

    public void setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
    }

    public double calculateProfit() {
        if (stock == null || stock.getItem() == null) return 0;
        return (stock.getItem().getSellingPrice() * quantity) - (stock.getItem().getCostPrice() * quantity);
    }
}


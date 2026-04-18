package com.example.smartstock;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.Timestamp;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Item implements Serializable, Parcelable {
    private int itemID;
    private String itemName;
    private String itemDescription;
    private String itemCategory;
    private double unitWeight;
    private double costPrice;
    private Timestamp date;  // Changed from Date to Timestamp
    private double sellingPrice;
    private String itemCode;

    public Item() {
    }

    public Item(int itemID, String itemName, String itemDescription, String itemCategory,
                double unitWeight, double costPrice, double sellingPrice, String itemCode) {
        this.itemID = itemID;
        this.itemName = itemName;
        this.itemDescription = itemDescription;
        this.itemCategory = itemCategory;
        this.unitWeight = unitWeight;
        this.costPrice = costPrice;
        this.sellingPrice = sellingPrice;
        this.itemCode = itemCode;
    }

    // Getters and Setters
    public int getItemID() {
        return itemID;
    }

    public void setItemID(int itemID) {
        this.itemID = itemID;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    public String getItemCategory() {
        return itemCategory;
    }

    public void setItemCategory(String itemCategory) {
        this.itemCategory = itemCategory;
    }

    public double getUnitWeight() {
        return unitWeight;
    }

    public void setUnitWeight(double unitWeight) {
        this.unitWeight = unitWeight;
    }

    public double getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(double costPrice) {
        this.costPrice = costPrice;
    }

    public double getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(double sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    // Date/Timestamp methods
    public void setDate(Object dateObj) {
        if (dateObj == null) {
            this.date = null;
        } else if (dateObj instanceof Timestamp) {
            this.date = (Timestamp) dateObj;
        } else if (dateObj instanceof Date) {
            this.date = new Timestamp((Date) dateObj);
        } else {
            this.date = Timestamp.now();
        }
    }

    public Timestamp getDate() {
        return date;
    }

    // Parcelable implementation
    protected Item(Parcel in) {
        itemID = in.readInt();
        itemName = in.readString();
        itemDescription = in.readString();
        itemCategory = in.readString();
        unitWeight = in.readDouble();
        costPrice = in.readDouble();
        sellingPrice = in.readDouble();
        itemCode = in.readString();
        long seconds = in.readLong();
        int nanoseconds = in.readInt();
        date = new Timestamp(seconds, nanoseconds);
    }

    public static final Creator<Item> CREATOR = new Creator<Item>() {
        @Override
        public Item createFromParcel(Parcel in) {
            return new Item(in);
        }

        @Override
        public Item[] newArray(int size) {
            return new Item[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(itemID);
        parcel.writeString(itemName);
        parcel.writeString(itemDescription);
        parcel.writeString(itemCategory);
        parcel.writeDouble(unitWeight);
        parcel.writeDouble(costPrice);
        parcel.writeDouble(sellingPrice);
        parcel.writeString(itemCode);
        if (date != null) {
            parcel.writeLong(date.getSeconds());
            parcel.writeInt(date.getNanoseconds());
        } else {
            parcel.writeLong(0);
            parcel.writeInt(0);
        }
    }
}
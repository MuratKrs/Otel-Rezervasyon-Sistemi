package com.otel.model;

public class Room {
    private int id;
    private String roomNumber;
    private String typeName;
    private double price;
    private String status;
    private String features;

    // Veritabanından okurken kullanacağımız Constructor
    public Room(int id, String roomNumber, String typeName, double price, String status, String features) {
        this.id = id;
        this.roomNumber = roomNumber;
        this.typeName = typeName;
        this.price = price;
        this.status = status;
        this.features = features;
    }

    // Yeni oda eklerken kullanacağımız Constructor
    public Room(String roomNumber, String typeName, double price, String features) {
        this.roomNumber = roomNumber;
        this.typeName = typeName;
        this.price = price;
        this.features = features;
        this.status = "AVAILABLE";
    }

    // Getter Metotları
    public int getId() { return id; }
    public String getRoomNumber() { return roomNumber; }
    public String getTypeName() { return typeName; }
    public double getPrice() { return price; }
    public String getStatus() { return status; }
    public String getFeatures() { return features; }
}
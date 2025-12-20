package com.otel.model;

import java.time.LocalDate;

public class Reservation {
    private int id;
    private int userId;
    private int roomId;
    private String roomNumber;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private double totalPrice;
    private String userFullName;

    // Ekranda listeleme yaparken kullanacağımız yapıcı (Constructor)
    public Reservation(int id, int userId, int roomId, String roomNumber, LocalDate checkInDate, LocalDate checkOutDate, double totalPrice) {
        this.id = id;
        this.userId = userId;
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.totalPrice = totalPrice;

    }

    // Rezervasyon yaparken kullandığımız yapıcı
    public Reservation(int userId, int roomId, LocalDate checkInDate, LocalDate checkOutDate, double totalPrice) {
        this.userId = userId;
        this.roomId = roomId;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.totalPrice = totalPrice;

    }
    public Reservation(int id, String userFullName, String roomNumber, LocalDate checkInDate, LocalDate checkOutDate, double totalPrice) {
        this.id = id;
        this.userFullName = userFullName;
        this.roomNumber = roomNumber;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.totalPrice = totalPrice;
    }

    // Getter Metotları
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public int getRoomId() { return roomId; }
    public String getRoomNumber() { return roomNumber; }
    public LocalDate getCheckInDate() { return checkInDate; }
    public LocalDate getCheckOutDate() { return checkOutDate; }
    public double getTotalPrice() { return totalPrice; }
    public String getUserFullName() { return userFullName; }
}
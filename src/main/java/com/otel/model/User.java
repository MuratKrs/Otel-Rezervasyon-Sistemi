package com.otel.model;

public class User {
    private int id;
    private String username;
    private String fullName;
    private String role; // ADMIN veya CUSTOMER

    // Constructor (Yapıcı Metot)
    public User(int id, String username, String fullName, String role) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.role = role;
    }

    // Getter Metotları (Verileri okumak için)
    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public String getRole() { return role; }
}
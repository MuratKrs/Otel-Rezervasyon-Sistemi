package com.otel.dao;

import com.otel.model.User;
import com.otel.util.DbHelper;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserDao {

    // 1. GİRİŞ YAPMA (LOGIN)
    public User login(String username, String password) {
        User user = null;
        // Kullanıcı adı ve şifredeki gereksiz boşlukları temizle (trim)
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";

        try (Connection conn = DbHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username.trim());
            pstmt.setString(2, password.trim());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    user = new User(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("full_name"),
                            rs.getString("role")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }

    // 2. KAYIT OLMA (REGISTER)
    public boolean registerUser(String username, String password, String fullName) {
        // Önce kullanıcı adı var mı kontrol et
        if (isUsernameTaken(username)) {
            return false;
        }

        String sql = "INSERT INTO users (username, password, full_name, role) VALUES (?, ?, ?, 'CUSTOMER')";

        try (Connection conn = DbHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username.trim());
            pstmt.setString(2, password.trim());
            pstmt.setString(3, fullName.trim());

            return pstmt.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Yardımcı Metod: Kullanıcı adı kontrolü
    private boolean isUsernameTaken(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (Connection conn = DbHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username.trim());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0; // Eğer 0'dan büyükse kullanıcı adı alınmış demektir
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // 3. TÜM MÜŞTERİLERİ GETİR (ADMİN İÇİN)
    public java.util.List<User> getAllCustomers() {
        java.util.List<User> list = new java.util.ArrayList<>();
        String sql = "SELECT * FROM users WHERE role = 'CUSTOMER'";

        try (Connection conn = DbHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while(rs.next()) {
                list.add(new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("full_name"),
                        rs.getString("role")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // ARAMA METODU
    public java.util.List<User> searchUsers(String searchTerm) {
        java.util.List<User> list = new java.util.ArrayList<>();
        String sql = "SELECT * FROM users WHERE role = 'CUSTOMER' AND (LOWER(full_name) LIKE ? OR LOWER(username) LIKE ?)";
        try (Connection conn = DbHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String queryTerm = "%" + searchTerm.toLowerCase() + "%";
            pstmt.setString(1, queryTerm);
            pstmt.setString(2, queryTerm);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new User(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("full_name"),
                            rs.getString("role")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // 4. KULLANICI SİLME
    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (Connection conn = DbHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
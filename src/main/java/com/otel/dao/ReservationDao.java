package com.otel.dao;

import com.otel.model.Reservation;
import com.otel.util.DbHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet; // <-- Eksik olan buydu
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ReservationDao {

    // 1. REZERVASYON YAPMA
    public boolean makeReservation(Reservation res) {
        String sql = "CALL sp_create_reservation(?, ?, ?, ?, ?)";

        try (Connection conn = DbHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Parametreleri sırasıyla gönderiyoruz
            pstmt.setInt(1, res.getUserId());
            pstmt.setInt(2, res.getRoomId());

            // Java Date -> SQL Date dönüşümü
            pstmt.setDate(3, java.sql.Date.valueOf(res.getCheckInDate()));
            pstmt.setDate(4, java.sql.Date.valueOf(res.getCheckOutDate()));

            pstmt.setBigDecimal(5, java.math.BigDecimal.valueOf(res.getTotalPrice()));

            // Prosedürü çalıştır
            pstmt.execute();

            // Hata fırlatmadıysa işlem başarılıdır
            return true;

        } catch (SQLException e) {
            // Eğer veritabanı "Oda dolu!" hatası fırlatırsa bunu yakala
            System.out.println("Hata: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // 2. KULLANICININ REZERVASYONLARINI GETİR
    public List<Reservation> getReservationsByUserId(int userId) {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT res.*, r.room_number " +
                "FROM reservations res " +
                "JOIN rooms r ON res.room_id = r.room_id " +
                "WHERE res.user_id = ? " +
                "ORDER BY res.start_date DESC";

        try (Connection conn = DbHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Reservation res = new Reservation(
                            rs.getInt("reservation_id"),
                            rs.getInt("user_id"),
                            rs.getInt("room_id"),
                            rs.getString("room_number"),
                            rs.getDate("start_date").toLocalDate(),
                            rs.getDate("end_date").toLocalDate(),
                            rs.getDouble("total_price")
                    );
                    list.add(res);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // 3. TÜM REZERVASYONLARI GETİR (ADMİN)
    public List<Reservation> getAllReservations() {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT res.reservation_id, u.full_name, r.room_number, res.start_date, res.end_date, res.total_price " +
                "FROM reservations res " +
                "JOIN users u ON res.user_id = u.user_id " +
                "JOIN rooms r ON res.room_id = r.room_id " +
                "ORDER BY res.start_date DESC";

        try (Connection conn = DbHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Reservation res = new Reservation(
                        rs.getInt("reservation_id"),
                        rs.getString("full_name"),
                        rs.getString("room_number"),
                        rs.getDate("start_date").toLocalDate(),
                        rs.getDate("end_date").toLocalDate(),
                        rs.getDouble("total_price")
                );
                list.add(res);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // ARAMA METODU
    public List<Reservation> searchReservations(String searchTerm) {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT res.reservation_id, u.full_name, r.room_number, res.start_date, res.end_date, res.total_price " +
                "FROM reservations res " +
                "JOIN users u ON res.user_id = u.user_id " +
                "JOIN rooms r ON res.room_id = r.room_id " +
                "WHERE LOWER(u.full_name) LIKE ? OR LOWER(r.room_number) LIKE ? " +
                "ORDER BY res.start_date DESC";

        try (Connection conn = DbHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String queryTerm = "%" + searchTerm.toLowerCase() + "%";
            pstmt.setString(1, queryTerm);
            pstmt.setString(2, queryTerm);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Reservation res = new Reservation(
                            rs.getInt("reservation_id"),
                            rs.getString("full_name"),
                            rs.getString("room_number"),
                            rs.getDate("start_date").toLocalDate(),
                            rs.getDate("end_date").toLocalDate(),
                            rs.getDouble("total_price")
                    );
                    list.add(res);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // 4. İPTAL ETME
    public boolean cancelReservation(int reservationId) {
        String sql = "DELETE FROM reservations WHERE reservation_id = ?";
        try (Connection conn = DbHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, reservationId);
            return pstmt.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
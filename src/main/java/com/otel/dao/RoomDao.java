package com.otel.dao;

import com.otel.model.Room;
import com.otel.util.DbHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class RoomDao {

    // 1. TÜM ODALARI LİSTELEME
    public List<Room> getAllRooms() {
        List<Room> rooms = new ArrayList<>();

        // Bu sorgu; Odalar, Oda Tipleri ve Özellikler tablolarını birleştirir.
        // STRING_AGG fonksiyonu, odaya ait özellikleri "Klima, TV" şeklinde virgülle birleştirir.
        String sql = "SELECT r.room_id, r.room_number, r.price_per_night, r.status, " +
                "       rt.type_name, " +
                "       COALESCE(STRING_AGG(f.feature_name, ', '), 'Standart') as features_list " +
                "FROM rooms r " +
                "JOIN room_types rt ON r.type_id = rt.type_id " +
                "LEFT JOIN room_features rf ON r.room_id = rf.room_id " +
                "LEFT JOIN features f ON rf.feature_id = f.feature_id " +
                "GROUP BY r.room_id, rt.type_name " +
                "ORDER BY r.room_number ASC";

        try (Connection conn = DbHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Room room = new Room(
                        rs.getInt("room_id"),
                        rs.getString("room_number"),
                        rs.getString("type_name"), // Artık tipin adını alıyoruz
                        rs.getDouble("price_per_night"),
                        rs.getString("status"),
                        rs.getString("features_list") // Birleştirilmiş özellikler stringi
                );
                rooms.add(room);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return rooms;
    }

    // 2. YENİ ODA EKLEME (OTOMATİK ÖZELLİK ATAMALI)
    public boolean addRoom(Room room) {
        Connection conn = null;
        PreparedStatement pstmtRoom = null;
        PreparedStatement pstmtFeature = null;
        ResultSet rs = null;
        boolean isSuccess = false;

        String sqlRoom = "INSERT INTO rooms (room_number, price_per_night, status, type_id) " +
                "VALUES (?, ?, ?, (SELECT type_id FROM room_types WHERE type_name = ?))";

        // Özellik ismine göre ID bulup ara tabloya ekleyen sorgu
        String sqlFeature = "INSERT INTO room_features (room_id, feature_id) " +
                "SELECT ?, feature_id FROM features WHERE feature_name = ?";

        try {
            conn = DbHelper.getConnection();
            conn.setAutoCommit(false); // Transaction Başlat

            // --- 1. Odayı Ekle ---
            pstmtRoom = conn.prepareStatement(sqlRoom, java.sql.Statement.RETURN_GENERATED_KEYS);
            pstmtRoom.setString(1, room.getRoomNumber());
            pstmtRoom.setDouble(2, room.getPrice());
            pstmtRoom.setString(3, "AVAILABLE");
            pstmtRoom.setString(4, room.getTypeName());

            int affected = pstmtRoom.executeUpdate();
            if (affected == 0) throw new java.sql.SQLException("Oda eklenemedi.");

            // --- 2. Yeni Odanın ID'sini Al ---
            rs = pstmtRoom.getGeneratedKeys();
            int newRoomId = 0;
            if (rs.next()) {
                newRoomId = rs.getInt(1);
            } else {
                throw new java.sql.SQLException("Oda ID alınamadı.");
            }

            // --- 3. Oda Tipine Göre Özellik Listesini Belirle ---
            String[] featuresToAdd;
            String type = room.getTypeName();

            if (type.equals("Kral Dairesi")) {
                featuresToAdd = new String[]{"Klima", "TV", "Ücretsiz Wifi", "Minibar", "Jakuzi", "Kasa", "Deniz Manzarası"};
            } else if (type.equals("Suit")) {
                featuresToAdd = new String[]{"Klima", "TV", "Ücretsiz Wifi", "Minibar", "Jakuzi"};
            } else {
                // Tek Kişilik ve Çift Kişilik için standart paket
                featuresToAdd = new String[]{"Klima", "TV", "Ücretsiz Wifi"};
            }

            // --- 4. Özellikleri Döngüyle Kaydet ---
            pstmtFeature = conn.prepareStatement(sqlFeature);
            for (String featureName : featuresToAdd) {
                pstmtFeature.setInt(1, newRoomId);
                pstmtFeature.setString(2, featureName);
                pstmtFeature.addBatch(); // Hepsini biriktir
            }
            pstmtFeature.executeBatch(); // Hepsini tek seferde gönder

            conn.commit(); // Onayla
            isSuccess = true;

        } catch (Exception e) {
            e.printStackTrace();
            try { if (conn != null) conn.rollback(); } catch (Exception ex) {}
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) {}
            try { if (pstmtRoom != null) pstmtRoom.close(); } catch (Exception e) {}
            try { if (pstmtFeature != null) pstmtFeature.close(); } catch (Exception e) {}
            try { if (conn != null) conn.close(); } catch (Exception e) {}
        }
        return isSuccess;
    }

    // 3. ODA SİLME
    public boolean deleteRoom(int roomId) {
        String sql = "DELETE FROM rooms WHERE room_id = ?";
        try (Connection conn = DbHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, roomId);
            return pstmt.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 4. ODA FİYATI GÜNCELLEME (UPDATE)
    public boolean updateRoomPrice(int roomId, double newPrice) {
        String sql = "UPDATE rooms SET price_per_night = ? WHERE room_id = ?";

        try (Connection conn = DbHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDouble(1, newPrice);
            pstmt.setInt(2, roomId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
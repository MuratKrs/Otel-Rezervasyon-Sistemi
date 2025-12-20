package com.otel.ui;

import com.otel.dao.RoomDao;
import com.otel.model.Room;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AdminDashboard extends JFrame {

    private JTable table;
    private DefaultTableModel tableModel;
    private RoomDao roomDao;

    public AdminDashboard() {
        // Pencere Ayarları
        setTitle("Yönetici Paneli - Oda Listesi");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // DAO'yu başlat
        roomDao = new RoomDao();

        // --- ÜST PANEL (Başlık) ---
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(60, 100, 150)); // Lacivert tonu
        JLabel lblTitle = new JLabel("OTEL YÖNETİM PANELİ");
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 20));
        topPanel.add(lblTitle);
        add(topPanel, BorderLayout.NORTH);

        // --- TABLO OLUŞTURMA ---
        // Sütun başlıkları
        String[] columnNames = {"ID", "Oda No", "Oda Tipi", "Fiyat (TL)", "Durum"};

        // Tablo modeli (Verileri tutan yapı)
        // Hücre düzenlemeyi kapatan özel model
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Hiçbir hücreye tıklayıp değiştirilmesine izin verme
            }
        };
        table = new JTable(tableModel);

        // Tabloyu kaydırma çubuğu içine al (Veri çok olursa aşağı inebilmek için)
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // --- ALT PANEL (Butonlar) ---
        JPanel bottomPanel = new JPanel();
        JButton btnRefresh = new JButton("Listeyi Yenile");
        JButton btnAdd = new JButton("Yeni Oda Ekle");
        JButton btnDelete = new JButton("Sil");
        JButton btnReservations = new JButton("Tüm Rezervasyonlar");
        btnReservations.setBackground(new Color(255, 140, 0)); // Turuncu renk fark edilsin
        btnReservations.setForeground(Color.WHITE);
        JButton btnUpdate = new JButton("Fiyat Güncelle");
        btnUpdate.setBackground(new Color(255, 215, 0)); // Altın Sarısı
        btnUpdate.setForeground(Color.BLACK);
        JButton btnUsers = new JButton("Müşteriler");
        btnUsers.setBackground(Color.CYAN);
        JButton btnLogout = new JButton("ÇIKIŞ YAP");
        btnLogout.setBackground(Color.DARK_GRAY);
        btnLogout.setForeground(Color.WHITE);

        bottomPanel.add(btnRefresh);
        bottomPanel.add(btnAdd);
        bottomPanel.add(btnDelete);
        add(bottomPanel, BorderLayout.SOUTH);
        bottomPanel.add(btnReservations);
        bottomPanel.add(btnUpdate);
        bottomPanel.add(btnUsers);
        bottomPanel.add(btnLogout);

        // Listeyi Yenile Buton Aksiyonu
        btnRefresh.addActionListener(e -> loadRoomData());

        // İlk açılışta verileri yükle
        loadRoomData();

        // "Yeni Oda Ekle" butonu aksiyonu
        btnAdd.addActionListener(e -> {
            // Dialog penceresini oluştur
            AddRoomDialog dialog = new AddRoomDialog(this);
            dialog.setVisible(true); // Pencereyi göster (Program burada bekler)

            // Pencere kapandığında kod devam eder
            if (dialog.isAdded()) {
                // Eğer yeni kayıt yapıldıysa tabloyu yenile
                loadRoomData();
            }
        });
        // "Sil" butonu aksiyonu
        btnDelete.addActionListener(e -> {
            // 1. Tablodan seçili satırı al
            int selectedRow = table.getSelectedRow();

            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Lütfen silinecek odayı seçiniz!", "Uyarı", JOptionPane.WARNING_MESSAGE);
                return;
            }



            // 2. Seçili satırın ID'sini al (0. sütun ID sütunuydu)
            int roomId = (int) tableModel.getValueAt(selectedRow, 0);
            String roomNo = (String) tableModel.getValueAt(selectedRow, 1);

            // 3. Onay sorusu sor
            int choice = JOptionPane.showConfirmDialog(this,
                    roomNo + " numaralı odayı silmek istediğinize emin misiniz?",
                    "Silme Onayı",
                    JOptionPane.YES_NO_OPTION);

            if (choice == JOptionPane.YES_OPTION) {
                // 4. Veritabanından sil
                if (roomDao.deleteRoom(roomId)) {
                    JOptionPane.showMessageDialog(this, "Oda başarıyla silindi.");
                    loadRoomData(); // Tabloyu yenile
                } else {
                    JOptionPane.showMessageDialog(this, "Oda silinemedi!\n(Bu odaya ait rezervasyonlar olabilir)", "Hata", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        btnReservations.addActionListener(e -> {
            new AdminReservationsDialog(this).setVisible(true);
        });
        btnUpdate.addActionListener(e -> {
            // 1. Tablodan seçili satırı kontrol et
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Lütfen fiyatını değiştirmek için bir oda seçin!");
                return;
            }

            // 2. Mevcut bilgileri al
            int roomId = (int) tableModel.getValueAt(selectedRow, 0);
            String roomNo = (String) tableModel.getValueAt(selectedRow, 1);
            double currentPrice = (double) tableModel.getValueAt(selectedRow, 3);

            // 3. Kullanıcıdan yeni fiyatı iste
            String input = JOptionPane.showInputDialog(this,
                    roomNo + " nolu oda için yeni fiyatı giriniz:",
                    currentPrice);

            // Eğer kullanıcı "İptal" derse veya boş bırakırsa işlem yapma
            if (input != null && !input.isEmpty()) {
                try {
                    double newPrice = Double.parseDouble(input);

                    // 4. DAO'yu çağır ve güncelle
                    if (roomDao.updateRoomPrice(roomId, newPrice)) {
                        JOptionPane.showMessageDialog(this, "Fiyat başarıyla güncellendi!");
                        loadRoomData(); // Tabloyu yenile ki yeni fiyat görünsün
                    } else {
                        JOptionPane.showMessageDialog(this, "Güncelleme başarısız oldu.", "Hata", JOptionPane.ERROR_MESSAGE);
                    }

                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Lütfen geçerli bir sayı giriniz!", "Hata", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        btnUsers.addActionListener(e -> new AdminUsersDialog(this).setVisible(true));
        btnLogout.addActionListener(e -> {
            this.dispose(); // Ekranı kapat
            new LoginFrame().setVisible(true); // Giriş ekranını geri aç
        });
    }

    // Veritabanından verileri çekip tabloya dolduran metod
    private void loadRoomData() {
        // Mevcut tabloyu temizle
        tableModel.setRowCount(0);

        // Veritabanından listeyi al
        List<Room> rooms = roomDao.getAllRooms();

        for (Room room : rooms) {
            Object[] row = {
                    room.getId(),
                    room.getRoomNumber(),
                    room.getTypeName(),
                    room.getPrice(),
                    room.getStatus()
            };
            tableModel.addRow(row);
        }
    }
}
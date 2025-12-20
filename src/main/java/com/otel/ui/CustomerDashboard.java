package com.otel.ui;

import com.otel.dao.RoomDao;
import com.otel.dao.ReservationDao;
import com.otel.model.Room;
import com.otel.model.Reservation;
import com.otel.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class CustomerDashboard extends JFrame {

    private JTable table;
    private DefaultTableModel tableModel;
    private RoomDao roomDao;
    private User currentUser; // Giriş yapan müşteri bilgisi

    private JTextField txtCheckIn;
    private JTextField txtCheckOut;

    public CustomerDashboard(User user) {
        this.currentUser = user;
        this.roomDao = new RoomDao();

        setTitle("Otel Rezervasyon Sistemi - Hoşgeldiniz, " + user.getFullName());
        setSize(1100, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- ÜST PANEL (Tarih, İşlemler ve Çıkış) ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        topPanel.setBackground(new Color(245, 245, 245));

        // 1. Tarih Alanları
        topPanel.add(new JLabel("Giriş (Yıl-Ay-Gün):"));
        txtCheckIn = new JTextField(10);
        txtCheckIn.setText(LocalDate.now().toString());
        topPanel.add(txtCheckIn);

        topPanel.add(new JLabel("Çıkış (Yıl-Ay-Gün):"));
        txtCheckOut = new JTextField(10);
        txtCheckOut.setText(LocalDate.now().plusDays(1).toString());
        topPanel.add(txtCheckOut);

        // 2. İşlem Butonları
        JButton btnBook = new JButton("SEÇİLİ ODAYI REZERVE ET");
        btnBook.setBackground(new Color(0, 153, 76)); // Koyu Yeşil
        btnBook.setForeground(Color.WHITE);
        topPanel.add(btnBook);

        JButton btnMyReservations = new JButton("REZERVASYONLARIM");
        btnMyReservations.setBackground(new Color(50, 100, 200)); // Mavi
        btnMyReservations.setForeground(Color.WHITE);
        topPanel.add(btnMyReservations);

        JButton btnRefresh = new JButton("⟳ Yenile");
        btnRefresh.setBackground(Color.ORANGE);
        btnRefresh.setForeground(Color.BLACK);
        topPanel.add(btnRefresh);

        // 3. Çıkış Butonu
        JButton btnLogout = new JButton("ÇIKIŞ YAP");
        btnLogout.setBackground(new Color(200, 50, 50)); // Kırmızı
        btnLogout.setForeground(Color.WHITE);
        topPanel.add(btnLogout);

        add(topPanel, BorderLayout.NORTH);

        // --- ORTA PANEL (Oda Tablosu) ---
        String[] columns = {"ID", "Oda No", "Tip", "Gecelik Fiyat", "Durum", "Özellikler"};

        // Tablo hücresine tıklanıp değiştirilmesini engellemek için Override ediyoruz
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Hücre düzenlemeyi kapat
            }
        };

        table = new JTable(tableModel);

        // -- GÖRSEL AYARLAR --
        table.setRowHeight(30); // Satır yüksekliği
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14)); // Yazı tipi
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14)); // Başlık fontu
        table.getTableHeader().setBackground(new Color(60, 100, 150)); // Başlık Arkaplanı (Lacivert)
        table.getTableHeader().setForeground(Color.WHITE); // Başlık Yazı Rengi
        table.setSelectionBackground(new Color(255, 140, 0)); // Seçilince Turuncu olsun
        table.setSelectionForeground(Color.WHITE);

        add(new JScrollPane(table), BorderLayout.CENTER);

        // Odayı listele
        loadRooms();

        // --- BUTON AKSİYONLARI ---

        // 1. Rezervasyon Yap
        btnBook.addActionListener(e -> makeReservation());

        // 2. Rezervasyonlarım (Kapandığında listeyi OTOMATİK yeniler)
        btnMyReservations.addActionListener(e -> {
            new MyReservationsDialog(this, currentUser).setVisible(true);
            // Pencere kapandığında kod buraya döner:
            loadRooms();
        });

        // 3. Manuel Yenile
        btnRefresh.addActionListener(e -> {
            loadRooms();
            // Kullanıcıyı çok mesajla boğmamak için istersen alttaki satırı silebilirsin
            // JOptionPane.showMessageDialog(this, "Liste güncellendi.");
        });

        // 4. Çıkış Yap
        btnLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Çıkış yapmak istediğinize emin misiniz?", "Çıkış", JOptionPane.YES_NO_OPTION);
            if(confirm == JOptionPane.YES_OPTION) {
                this.dispose(); // Bu ekranı kapat
                new LoginFrame().setVisible(true); // Giriş ekranını aç
            }
        });
    }

    private void loadRooms() {
        // Tabloyu temizle ve yeniden doldur
        tableModel.setRowCount(0);
        List<Room> rooms = roomDao.getAllRooms();
        for (Room r : rooms) {
            // Sadece MÜSAİT (AVAILABLE) olanları göster
            if("AVAILABLE".equals(r.getStatus())) {
                tableModel.addRow(new Object[]{
                        r.getId(),
                        r.getRoomNumber(),
                        r.getTypeName(),
                        r.getPrice(),
                        r.getStatus(),
                        r.getFeatures()
                });
            }
        }
    }

    private void makeReservation() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Lütfen tablodan bir oda seçiniz!");
            return;
        }

        try {
            // Verileri Tablodan Al
            int roomId = (int) tableModel.getValueAt(selectedRow, 0);
            double pricePerNight = (double) tableModel.getValueAt(selectedRow, 3);
            String roomNo = (String) tableModel.getValueAt(selectedRow, 1);

            LocalDate checkIn = LocalDate.parse(txtCheckIn.getText());
            LocalDate checkOut = LocalDate.parse(txtCheckOut.getText());

            if (checkOut.isBefore(checkIn) || checkOut.isEqual(checkIn)) {
                JOptionPane.showMessageDialog(this, "Çıkış tarihi giriş tarihinden sonra olmalıdır!");
                return;
            }

            // Fiyat Hesapla
            long days = ChronoUnit.DAYS.between(checkIn, checkOut);
            double totalPrice = days * pricePerNight;

            // Onay İste
            int choice = JOptionPane.showConfirmDialog(this,
                    "Oda: " + roomNo + "\n" +
                            "Gün Sayısı: " + days + "\n" +
                            "Toplam Tutar: " + totalPrice + " TL\n\nOnaylıyor musunuz?",
                    "Ödeme Onayı", JOptionPane.YES_NO_OPTION);

            if (choice == JOptionPane.YES_OPTION) {
                // Modeli Oluştur
                Reservation res = new Reservation(currentUser.getId(), roomId, checkIn, checkOut, totalPrice);
                ReservationDao resDao = new ReservationDao();

                // Kaydet (Trigger sayesinde oda durumu değişecek)
                if (resDao.makeReservation(res)) {
                    JOptionPane.showMessageDialog(this, "Rezervasyonunuz Başarıyla Oluşturuldu!");
                    loadRooms(); // Listeyi yenile ki rezerve edilen oda listeden düşsün
                } else {
                    JOptionPane.showMessageDialog(this, "Hata oluştu! Lütfen tekrar deneyin.", "Hata", JOptionPane.ERROR_MESSAGE);
                }
            }

        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Lütfen tarihi YYYY-AA-GG formatında giriniz (Örn: 2023-12-01)", "Tarih Hatası", JOptionPane.WARNING_MESSAGE);
        }
    }
}
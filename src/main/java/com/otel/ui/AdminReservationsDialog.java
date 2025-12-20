package com.otel.ui;

import com.otel.dao.ReservationDao;
import com.otel.model.Reservation;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AdminReservationsDialog extends JDialog {
    private JTable table;
    private DefaultTableModel tableModel;
    private ReservationDao reservationDao;
    private JLabel lblTotalIncome;
    private JTextField txtSearch;

    public AdminReservationsDialog(JFrame parent) {
        super(parent, "Tüm Rezervasyonlar & Rapor", true);
        this.reservationDao = new ReservationDao();

        setSize(900, 550);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        // --- ÜST PANEL (Başlık ve Arama) ---
        JPanel topPanel = new JPanel(new BorderLayout());

        // Başlık
        JLabel lblTitle = new JLabel("OTEL REZERVASYON YÖNETİMİ", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        topPanel.add(lblTitle, BorderLayout.NORTH);

        // Arama
        JPanel pnlSearch = new JPanel(new FlowLayout(FlowLayout.CENTER));
        txtSearch = new JTextField(25);
        JButton btnSearch = new JButton("Müşteri Adı veya Oda No ile Ara");
        pnlSearch.add(txtSearch);
        pnlSearch.add(btnSearch);
        topPanel.add(pnlSearch, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);

        // --- TABLO ---
        String[] columns = {"ID", "Müşteri Adı", "Oda No", "Giriş", "Çıkış", "Tutar (TL)"};

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        // Görsel Ayarlar
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(60, 100, 150));
        table.getTableHeader().setForeground(Color.WHITE);

        add(new JScrollPane(table), BorderLayout.CENTER);

        // --- ALT PANEL (Butonlar ve Ciro) ---
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 1. Silme Butonu (SOL Taraf)
        JButton btnCancel = new JButton("Seçili Rezervasyonu İptal Et (Sil)");
        btnCancel.setBackground(Color.RED);
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        bottomPanel.add(btnCancel, BorderLayout.WEST);

        // 2. Ciro Etiketi (SAĞ Taraf)
        lblTotalIncome = new JLabel("Toplam Ciro: 0.0 TL");
        lblTotalIncome.setFont(new Font("Arial", Font.BOLD, 16));
        lblTotalIncome.setForeground(new Color(0, 100, 0));
        bottomPanel.add(lblTotalIncome, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);

        // Verileri Yükle
        loadAllReservations();

        // --- AKSİYONLAR ---
        btnCancel.addActionListener(e -> cancelReservation());
        btnSearch.addActionListener(e -> loadAllReservations(txtSearch.getText()));
    }

    private void loadAllReservations() {
        loadAllReservations(null);
    }

    private void loadAllReservations(String searchTerm) {
        tableModel.setRowCount(0);
        List<Reservation> list;
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            list = reservationDao.getAllReservations();
        } else {
            list = reservationDao.searchReservations(searchTerm);
        }

        double totalIncome = 0;
        for (Reservation r : list) {
            Object[] row = {
                    r.getId(),
                    r.getUserFullName(),
                    r.getRoomNumber(),
                    r.getCheckInDate(),
                    r.getCheckOutDate(),
                    r.getTotalPrice()
            };
            tableModel.addRow(row);
            totalIncome += r.getTotalPrice();
        }
        lblTotalIncome.setText("TOPLAM CİRO: " + totalIncome + " TL");
    }

    private void cancelReservation() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Lütfen silinecek rezervasyonu seçiniz!");
            return;
        }

        // ID'yi al
        int resId = (int) tableModel.getValueAt(selectedRow, 0);
        String customerName = (String) tableModel.getValueAt(selectedRow, 1);

        int choice = JOptionPane.showConfirmDialog(this,
                "Müşteri: " + customerName + "\nBu rezervasyonu iptal etmek (silmek) istediğinize emin misiniz?",
                "İptal Onayı",
                JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            if (reservationDao.cancelReservation(resId)) {
                JOptionPane.showMessageDialog(this, "Rezervasyon başarıyla silindi.");
                loadAllReservations(); // Listeyi yenile
            } else {
                JOptionPane.showMessageDialog(this, "Silme işlemi başarısız oldu.", "Hata", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
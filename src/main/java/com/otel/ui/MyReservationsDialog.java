package com.otel.ui;

import com.otel.dao.ReservationDao;
import com.otel.model.Reservation;
import com.otel.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MyReservationsDialog extends JDialog {
    private JTable table;
    private DefaultTableModel tableModel;
    private ReservationDao reservationDao;
    private User currentUser;

    public MyReservationsDialog(JFrame parent, User user) {
        super(parent, "Rezervasyonlarım", true);
        this.currentUser = user;
        this.reservationDao = new ReservationDao();

        setSize(700, 400);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        // --- TABLO ---
        String[] columns = {"Rez. ID", "Oda No", "Giriş Tarihi", "Çıkış Tarihi", "Tutar (TL)"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Hiçbir hücreye tıklayıp değiştirilmesine izin verme
            }
        };
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // --- ALT PANEL (İPTAL BUTONU) ---
        JPanel bottomPanel = new JPanel();
        JButton btnCancel = new JButton("Seçili Rezervasyonu İptal Et");
        btnCancel.setBackground(Color.RED);
        btnCancel.setForeground(Color.WHITE);
        bottomPanel.add(btnCancel);
        add(bottomPanel, BorderLayout.SOUTH);

        // Verileri Yükle
        loadReservations();

        // İptal Aksiyonu
        btnCancel.addActionListener(e -> cancelSelectedReservation());
    }

    private void loadReservations() {
        tableModel.setRowCount(0); // Tabloyu temizle
        List<Reservation> list = reservationDao.getReservationsByUserId(currentUser.getId());

        for (Reservation r : list) {
            Object[] row = {
                    r.getId(),
                    r.getRoomNumber(),
                    r.getCheckInDate(),
                    r.getCheckOutDate(),
                    r.getTotalPrice()
            };
            tableModel.addRow(row);
        }
    }

    private void cancelSelectedReservation() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "İptal etmek için bir rezervasyon seçin.");
            return;
        }

        int resId = (int) tableModel.getValueAt(selectedRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Rezervasyonu iptal etmek istediğinize emin misiniz?\n(Bu işlem geri alınamaz)",
                "İptal Onayı", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (reservationDao.cancelReservation(resId)) {
                JOptionPane.showMessageDialog(this, "Rezervasyon iptal edildi.");
                loadReservations(); // Listeyi yenile
            } else {
                JOptionPane.showMessageDialog(this, "İşlem başarısız.", "Hata", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
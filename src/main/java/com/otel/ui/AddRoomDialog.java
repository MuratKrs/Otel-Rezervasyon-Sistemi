package com.otel.ui;

import com.otel.dao.RoomDao;
import com.otel.model.Room;

import javax.swing.*;
import java.awt.*;

public class AddRoomDialog extends JDialog {
    private JTextField txtRoomNumber;
    private JComboBox<String> cmbType;
    private JTextField txtPrice;
    private boolean isAdded = false;

    public AddRoomDialog(JFrame parent) {
        super(parent, "Yeni Oda Ekle", true);
        setSize(350, 250);
        setLocationRelativeTo(parent);
        setLayout(new GridLayout(4, 2, 10, 10));

        // 1. Oda Numarası
        add(new JLabel("  Oda Numarası:"));
        txtRoomNumber = new JTextField();
        add(txtRoomNumber);

        // 2. Oda Tipi
        add(new JLabel("  Oda Tipi:"));
        String[] types = {"Tek Kişilik", "Çift Kişilik", "Suit", "Kral Dairesi"};
        cmbType = new JComboBox<>(types);
        add(cmbType);

        // 3. Fiyat
        add(new JLabel("  Gecelik Fiyat (TL):"));
        txtPrice = new JTextField();
        add(txtPrice);


        // 4. Butonlar
        JButton btnCancel = new JButton("İptal");
        JButton btnSave = new JButton("KAYDET");

        add(btnCancel);
        add(btnSave);

        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> saveRoom());
    }

    private void saveRoom() {
        try {
            String number = txtRoomNumber.getText().trim();
            String type = (String) cmbType.getSelectedItem();
            String priceStr = txtPrice.getText().trim();

            if (number.isEmpty() || priceStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Lütfen tüm alanları doldurun!");
                return;
            }

            double price = Double.parseDouble(priceStr);

            // Özellik kısmına null veya boş string gönderiyoruz, DAO zaten bunu görmezden gelip kendisi dolduracak.
            Room newRoom = new Room(number, type, price, "");

            RoomDao dao = new RoomDao();
            if (dao.addRoom(newRoom)) {
                JOptionPane.showMessageDialog(this, "Oda ve Özellikleri başarıyla eklendi!");
                isAdded = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Hata oluştu.", "Hata", JOptionPane.ERROR_MESSAGE);
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Fiyat alanına sayı giriniz!", "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isAdded() { return isAdded; }
}
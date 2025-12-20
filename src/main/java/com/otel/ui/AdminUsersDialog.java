package com.otel.ui;

import com.otel.dao.UserDao;
import com.otel.model.User;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AdminUsersDialog extends JDialog {
    private JTable table;
    private DefaultTableModel model;
    private UserDao dao;
    private JTextField txtSearch;

    public AdminUsersDialog(JFrame parent) {
        super(parent, "Müşteri Yönetimi", true);
        setSize(600, 400);
        setLocationRelativeTo(parent);
        dao = new UserDao();

        // Arama Paneli
        JPanel pnlSearch = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtSearch = new JTextField(20);
        JButton btnSearch = new JButton("Ara");
        pnlSearch.add(new JLabel("Arama:"));
        pnlSearch.add(txtSearch);
        pnlSearch.add(btnSearch);
        add(pnlSearch, BorderLayout.NORTH);

        // Tablo
        String[] cols = {"ID", "Ad Soyad", "Kullanıcı Adı"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Alt Panel (Sil Butonu)
        JPanel botPanel = new JPanel();
        JButton btnDel = new JButton("Seçili Müşteriyi Sil");
        btnDel.setBackground(Color.RED);
        btnDel.setForeground(Color.WHITE);
        botPanel.add(btnDel);
        add(botPanel, BorderLayout.SOUTH);

        loadData();

        btnDel.addActionListener(e -> deleteUser());
        btnSearch.addActionListener(e -> loadData(txtSearch.getText()));
    }

    private void loadData() {
        loadData(null);
    }

    private void loadData(String searchTerm) {
        model.setRowCount(0);
        List<User> userList;
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            userList = dao.getAllCustomers();
        } else {
            userList = dao.searchUsers(searchTerm);
        }

        for (User u : userList) {
            model.addRow(new Object[]{u.getId(), u.getFullName(), u.getUsername()});
        }
    }

    private void deleteUser() {
        int row = table.getSelectedRow();
        if (row == -1) return;

        int id = (int) table.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Kullanıcıyı silmek istiyor musunuz?\n(Rezervasyonları da silinebilir!)");

        if (confirm == JOptionPane.YES_OPTION) {
            if (dao.deleteUser(id)) {
                JOptionPane.showMessageDialog(this, "Silindi.");
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, "Silinemedi (Aktif rezervasyonu olabilir).");
            }
        }
    }
}
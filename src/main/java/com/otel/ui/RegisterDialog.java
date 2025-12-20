package com.otel.ui;

import com.otel.dao.UserDao;
import javax.swing.*;
import java.awt.*;

public class RegisterDialog extends JDialog {
    private JTextField txtName, txtUser;
    private JPasswordField txtPass;

    public RegisterDialog(JFrame parent) {
        super(parent, "Yeni Müşteri Kaydı", true);
        setSize(350, 300);
        setLocationRelativeTo(parent);
        setLayout(new GridLayout(4, 2, 10, 10));

        add(new JLabel("  Ad Soyad:"));
        txtName = new JTextField();
        add(txtName);

        add(new JLabel("  Kullanıcı Adı:"));
        txtUser = new JTextField();
        add(txtUser);

        add(new JLabel("  Şifre:"));
        txtPass = new JPasswordField();
        add(txtPass);

        JButton btnCancel = new JButton("İptal");
        JButton btnSave = new JButton("KAYIT OL");

        add(btnCancel);
        add(btnSave);

        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> register());
    }

    private void register() {
        String name = txtName.getText();
        String user = txtUser.getText();
        String pass = new String(txtPass.getPassword());

        if (name.isEmpty() || user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tüm alanları doldurun!");
            return;
        }

        UserDao dao = new UserDao();
        if (dao.registerUser(user, pass, name)) {
            JOptionPane.showMessageDialog(this, "Kayıt Başarılı! Giriş yapabilirsiniz.");
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Kullanıcı adı alınmış olabilir.", "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }
}
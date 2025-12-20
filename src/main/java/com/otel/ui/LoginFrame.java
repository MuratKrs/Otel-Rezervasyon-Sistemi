package com.otel.ui;

import com.otel.dao.UserDao;
import com.otel.model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginFrame extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;

    public LoginFrame() {
        // Pencere Ayarları
        setTitle("Otel Giriş Sistemi");
        setSize(350, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Ekranın ortasında açılması için
        setLayout(null); // Elemanları koordinatla yerleştirmek için

        // Bileşenleri Oluşturma
        JLabel lblUser = new JLabel("Kullanıcı Adı:");
        lblUser.setBounds(30, 30, 100, 25);
        add(lblUser);

        txtUsername = new JTextField();
        txtUsername.setBounds(130, 30, 150, 25);
        add(txtUsername);

        JLabel lblPass = new JLabel("Şifre:");
        lblPass.setBounds(30, 70, 100, 25);
        add(lblPass);

        txtPassword = new JPasswordField();
        txtPassword.setBounds(130, 70, 150, 25);
        add(txtPassword);

        btnLogin = new JButton("GİRİŞ YAP");
        btnLogin.setBounds(100, 120, 120, 30);
        add(btnLogin);
        JButton btnRegister = new JButton("Kayıt Ol");
        btnRegister.setBounds(100, 160, 120, 30); // Giriş butonunun altı
        add(btnRegister);



        // Butona Tıklama Olayı
        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performLogin();
            }
        });

        btnRegister.addActionListener(e -> {
            new RegisterDialog(this).setVisible(true);
        });
    }

    private void performLogin() {
        String username = txtUsername.getText();
        String password = new String(txtPassword.getPassword());

        UserDao dao = new UserDao();
        User user = dao.login(username, password);

        if (user != null) {
            JOptionPane.showMessageDialog(this, "Hoşgeldiniz, " + user.getFullName());

            if(user.getRole().equals("ADMIN")) {
                // Admin panelini aç
                new AdminDashboard().setVisible(true);
            } else {
                new CustomerDashboard(user).setVisible(true);
            }

            this.dispose(); // Login ekranını kapat
        } else {
            JOptionPane.showMessageDialog(this, "Hatalı kullanıcı adı veya şifre!", "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }
}
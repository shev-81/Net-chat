package com.client.chat;

import javax.swing.*;

public class LoginFrame extends JFrame {
    JLabel labelLogin;
    JLabel labelPass;
    JLabel labelNickName;
    JTextField loginField;
    JTextField passField;
    JTextField nickNameField;
    JButton okButton;
    JCheckBox checkBox;
    sampleController objController;

    LoginFrame(sampleController objController) {
        this.objController = objController;
        setSize(220, 200);
        setTitle("login");
        setDefaultCloseOperation(LoginFrame.DISPOSE_ON_CLOSE);

        // поле логина
        loginField = new JTextField();
        loginField.setBounds(10, 25, 70, 25);

        //поле пароля
        passField = new JTextField();
        passField.setBounds(10, 70, 70, 25);

        // Поле ввода ник нейма для регистрации
        nickNameField = new JTextField();
        nickNameField.setBounds(10, 115, 70, 25);
        nickNameField.setVisible(false);

        // кнопка подтверждения отправки
        okButton = new JButton("OK");
        okButton.setBounds(95, 25, 70, 25);
        okButton.addActionListener(e -> {
            if (!checkBox.isSelected()) {
                // если регистрация не выбрана посылаем запрос на проверку логина и пароля
                objController.sendMessage("/auth " + loginField.getText() + " " + passField.getText());
                dispose();  // закрываем оконо
            } else {
                // Регистрация нового пользователя.... и сразу вход в чат.
                objController.sendMessage("/reguser " + nickNameField.getText() + " " + loginField.getText() + " " + passField.getText());
                dispose();  // закрываем оконо
            }
        });

        // чек бокс для регистарции
        checkBox = new JCheckBox();
        checkBox.setText("Регистрация.");
        checkBox.setBounds(90, 70, 120, 15);
        checkBox.addActionListener(event -> {
            if (checkBox.isSelected()) {
                labelNickName.setVisible(true);
                nickNameField.setVisible(true);
            } else {
                labelNickName.setVisible(false);
                nickNameField.setVisible(false);
            }
        });

        //метка логина
        labelLogin = new JLabel("login");
        labelLogin.setBounds(10, 5, 70, 15);

        //метка пароля
        labelPass = new JLabel("pass");
        labelPass.setBounds(10, 50, 70, 15);

        //метка ник-нейма
        labelNickName = new JLabel("nick-Name");
        labelNickName.setBounds(10, 95, 70, 15);
        labelNickName.setVisible(false);

        //Добавляем все элементы управления панелью регистарции
        add(labelLogin);
        add(labelPass);
        add(labelNickName);
        add(loginField);
        add(passField);
        add(okButton);
        add(checkBox);
        add(nickNameField);
        setLayout(null);
        setLocationRelativeTo(null);
        this.setVisible(true);
    }

}

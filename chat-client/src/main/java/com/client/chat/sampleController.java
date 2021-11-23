package com.client.chat;
/**
 * Домашнее задание Шевеленко Андрея к 6 лекции Java 2
 */
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import javax.swing.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class sampleController{
    private final String SERVER_ADDR = "192.168.1.205";
    private final int SERVER_PORT = 8189;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String strFromServer;
    private String userName = "User";
    @FXML
    TextField textField;
    @FXML
    TextArea textArea;
    @FXML
    public void initialize(){
        try {
            openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    public void clikButton_1(){
        textArea.appendText("[User]: "+textField.getText()+"\n");
        sendMessage();
        textField.clear();
        textField.requestFocus();
    }
    @FXML
    public void openConnection() throws IOException {
        socket = new Socket(SERVER_ADDR, SERVER_PORT);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        new Thread(() -> {
            try {
                while (true) {
                    if(textArea.getText().toLowerCase().contains("/end")){
                        sendMessage();
                        Platform.exit();
                    }
                    strFromServer = in.readUTF();
                    textArea.appendText(strFromServer);
                    textArea.appendText("\n");
                }
            } catch (Exception e) {
                Platform.exit();
            }
        }).start();
    }
    @FXML
    public void sendMessage() {
        if (!textField.getText().trim().isEmpty()) {
            try {
                out.writeUTF("["+userName+"]: "+textField.getText());
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Ошибка отправки сообщения");
            }
        }
    }
    @FXML
    public void closeConnection() {
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

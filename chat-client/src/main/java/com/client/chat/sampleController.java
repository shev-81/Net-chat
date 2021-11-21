package com.client.chat;
/**
 * Домашнее задание Шевеленко Андрея к 4 лекции Java 2
 */
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import javax.swing.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class sampleController{
    private final String SERVER_ADDR = "localhost";
    private final int SERVER_PORT = 8189;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    public String strFromServer;
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
    public void clikButton_1(ActionEvent actionEvent){
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
                    if(textArea.getText().equalsIgnoreCase("/end")){
                        sendMessage();
                        closeConnection();
                        break;
                    }
                    strFromServer = in.readUTF();
                    textArea.appendText(strFromServer);
                    textArea.appendText("\n");
                }
            } catch (Exception e) {
                closeConnection();
            }
        }).start();
    }
    @FXML
    public void sendMessage() {
        if (!textField.getText().trim().isEmpty()) {
            try {
                out.writeUTF(textField.getText());
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
        Platform.exit();
    }
}

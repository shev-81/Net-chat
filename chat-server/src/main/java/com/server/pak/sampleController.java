package com.server.pak;
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
import java.net.ServerSocket;
import java.net.Socket;

public class sampleController{
    private final int SERVER_PORT = 8189;
    private ServerSocket serverSocket;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
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
        textArea.appendText("[Server]: "+textField.getText()+"\n");
        sendMessage();
        textField.clear();
        textField.requestFocus();
    }
    @FXML
    public void openConnection() throws IOException {
        socket=null;
        serverSocket = new ServerSocket(SERVER_PORT);
        System.out.println("Server Online...waiting connect user!");
        // Ожидание подключения
        socket = serverSocket.accept();
        System.out.println("User connected!");
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        new Thread(()->{
            try{
                while (true) {
                    String str = in.readUTF();
                    if (textArea.getText().toLowerCase().contains("/end")) {    // если пришло сообщение о закрытии закрываем подключение
                        System.out.println("[Server]: Server is closed!");
                        Platform.exit();
                    }
                    textArea.appendText(str+"\n");
                    System.out.println(str);
                }
            }catch (IOException e){}
        }).start();
    }
    @FXML
    public void sendMessage() {
        if (!textField.getText().trim().isEmpty()) {
            try {
                out.writeUTF("[Server]: "+textField.getText());
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
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

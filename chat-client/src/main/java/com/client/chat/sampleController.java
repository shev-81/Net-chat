package com.client.chat;
/**
 * Домашнее задание Шевеленко Андрея к 8 лекции Java 2
 */
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import javax.swing.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class sampleController implements Initializable {
    private final String SERVER_ADDR = "192.168.1.205";  //192.168.1.205";
    private final int SERVER_PORT = 8189;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String strFromServer;
    private String strFromClient;
    private boolean autorized = false;
    private Thread noTimerThr = null;

    public boolean isAutorized() {
        return autorized;
    }
    public void setAutorized(boolean autorized) {
        this.autorized = autorized;
    }
    @FXML
    TextField textField;
    @FXML
    TextArea textArea;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            noTimerThr = new Thread(()->noAutorizedTimer());
            noTimerThr.start();
            openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void noAutorizedTimer(){
        try {
            Thread.sleep(30000);
            if(!isAutorized()) {
                textArea.appendText("Вы отключены.");
                Thread.sleep(3000);
                sendMessage("/end");
                closeConnection();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    @FXML
    public void clikButton_1(){
        if (!textField.getText().trim().isEmpty())
            strFromClient=textField.getText();
        sendMessage(strFromClient);
        textField.clear();
        textField.requestFocus();
    }
    @FXML
    public synchronized void autorizQuestion(){
        new Thread (()->{
            try {
                Thread.sleep(1000);
                sendMessage("/auth "+JOptionPane.showInputDialog ("Введите логин и пароль через пробел."));
            } catch (Exception e) {
                System.out.println("Не введены вовремя логин и пароль ");
            }
            }).start();
    }
    @FXML
    public void openConnection() throws IOException {
        socket = new Socket(SERVER_ADDR, SERVER_PORT);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        autorizQuestion();
        new Thread(() -> {
            try {
                while (true) {
                    if(!noTimerThr.isAlive())
                        if(!isAutorized()) break;
                    strFromServer = in.readUTF();
                    if(strFromServer.contains("/end")){
                        closeConnection();
                        Platform.exit();
                    }
                    if(strFromServer.contains("/authok")){                  // при успешной авторизации сервер со служебной строкой присылает строку список всех авторизованных клиентов
                        String [] parts = strFromServer.split("\\s+"); // разбиваем ее на части в массив
                        setAutorized(true);
                        textArea.appendText("Вы авторизованны.\n");
                        for (int i=1; i<parts.length;i++)                   //выводим в текстовое поле список имен пропуская первый элемент со служебной командой
                            textArea.appendText(parts[i]+"\n");
                        continue;
                    }
                    if(strFromServer.contains("/authno")){
                        textArea.appendText("Попробуйте еще раз.\n");
                        autorizQuestion();
                        continue;
                    }
                    textArea.appendText(strFromServer);
                    textArea.appendText("\n");
                }
            } catch (Exception e) {
                Platform.exit();
            }
        }).start();
    }
    @FXML
    public void sendMessage(String str) {
        try {
            out.writeUTF(str);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Ошибка отправки сообщения");
        }
    }

    @FXML
    public void closeConnection() {
        try {
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

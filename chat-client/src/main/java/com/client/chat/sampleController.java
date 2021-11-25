package com.client.chat;
/**
 * Домашнее задание Шевеленко Андрея к 8 лекции Java 2
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
    private final String SERVER_ADDR = "192.168.1.205";  //192.168.1.205";
    private final int SERVER_PORT = 8189;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String strFromServer;
    private String strFromClient;
    private boolean autorized = false;
    private Thread noTimerThr = null;
    private int timerCount=60;

    public void resetTimerCount() {
        this.timerCount = 15;
    }
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
    @FXML
    public void initialize(){
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
            Thread.sleep(20000);
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
    public void openConnection() throws IOException {
        socket = new Socket(SERVER_ADDR, SERVER_PORT);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        new Thread(() -> {
            try {
                while (true) {
                    if(!noTimerThr.isAlive())
                        if(!isAutorized()) break;
                    if(textArea.getText().toLowerCase().contains("/end")){
                        strFromClient = textArea.getText();
                        sendMessage(strFromClient);
                        Platform.exit();
                    }
                    strFromServer = in.readUTF();
                    if(strFromServer.contains("/authok")){
                        setAutorized(true);
                        textArea.appendText("Вы авторизованны.\n");
                        continue;
                    }
                    if(strFromServer.contains("/autno")){
                        textArea.appendText("Попробуйте еще раз.\n");
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
            e.printStackTrace();
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

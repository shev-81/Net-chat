package com.client.chat;
/**
 * Домашнее задание Шевеленко Андрея к 8 лекции Java 2
 */
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javax.swing.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class sampleController implements Initializable {
    private final String SERVER_ADDR = "localhost";  //192.168.1.205";
    private final int SERVER_PORT = 8189;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String strFromServer;
    private String strFromClient;
    private boolean autorized = false;
    private Thread noTimerThr = null;
    public LoginFrame loginFrame;
    public boolean isAutorized() {
        return autorized;
    }
    public void setAutorized(boolean autorized) {
        this.autorized = autorized;
    }
    @FXML
    ListView<String> listFx;
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
            autorizQuestion();
            readMsg();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    public void clikButton_1(){
        if (!textField.getText().trim().isEmpty()) {
            strFromClient = textField.getText();
            sendMessage(strFromClient);
            textField.clear();
            textField.requestFocus();
        }
    }
    @FXML
    public synchronized void autorizQuestion(){
        new Thread (()->{
            try {
                Thread.sleep(1000);
                loginFrame = new LoginFrame(this);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Не введены вовремя логин и пароль ");
            }
        }).start();
    }
    @FXML
    public void readMsg(){
        new Thread(() -> {
            ObservableList<String> listUserModel = null;
            ArrayList <String> arrUsers = new ArrayList<>();
            try {
                while (true) {
                    if(!noTimerThr.isAlive())
                        if(!isAutorized()) break;
                    strFromServer = in.readUTF();
                    if(strFromServer.startsWith("/end")){
                        closeConnection();
                        Platform.exit();
                    }
                    if(strFromServer.startsWith("/authok")){
                        String [] parts = strFromServer.split("\\s+");
                        for (int i=1; i<parts.length;i++)                 //создаем и наполняем список пользователей
                            arrUsers.add(parts[i]);
                        listUserModel = FXCollections.observableArrayList(arrUsers);
                        listFx.setItems(listUserModel);                   // Устанавливаем список зарегистрированных пользователей для своего клиента
                        setAutorized(true);
                        textArea.appendText("Вы авторизованны.\n");
                        continue;
                    }
                    if(strFromServer.startsWith("/conected")){             // при соединении нового пользователя обновляем список активных пользователей
                        String [] parts = strFromServer.split("\\s+");
                        ObservableList<String> finalListUserModel = listUserModel;
                        Platform.runLater(() -> finalListUserModel.add(parts[1]));
                        continue;
                    }
                    if(strFromServer.startsWith("/disconected")){          // при Отсоединении пользователя обновляем список активных пользователей
                        String [] parts = strFromServer.split("\\s+");
                        ObservableList<String> finalListUserModel = listUserModel;
                        Platform.runLater(() -> finalListUserModel.remove(parts[1]));
                        continue;
                    }
                    if(strFromServer.startsWith("/authno")){
                        textArea.appendText("Попробуйте еще раз.\n");
                        autorizQuestion();
                        continue;
                    }
                    textArea.appendText(strFromServer+"\n");
                }
            } catch (Exception e) {
                Platform.exit();
            }
        }).start();
    }
    @FXML
    public void openConnection() throws IOException {
        socket = new Socket(SERVER_ADDR, SERVER_PORT);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
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
    // посылка личного сообщения по клику на ник пользователя в правой области
    public void moseClickOnListItem(MouseEvent mouseEvent) {
        String nameUser = listFx.getSelectionModel().getSelectedItem();
        new Thread (()->{
            String msg = JOptionPane.showInputDialog ("Сообщение для: "+nameUser);
            if (msg !=null) {
                sendMessage("/w " + nameUser + " " + msg);
            }else{
                JOptionPane.showMessageDialog(null, "Ошибка отправки сообщения");
            }
        }).start();
    }
    @FXML
    private void noAutorizedTimer(){
        try {
            Thread.sleep(60000);
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
}

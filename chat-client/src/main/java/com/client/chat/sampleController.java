package com.client.chat;
/**
 * Домашнее задание Шевеленко Андрея к 4 лекции Java 3
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
import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.ResourceBundle;

import static javafx.scene.input.MouseButton.PRIMARY;

public class sampleController implements Initializable {
    private final String SERVER_ADDR = "localhost";  //192.168.1.205";
    private final int SERVER_PORT = 8189;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String strFromServer;
    private String strFromClient;
    private String myName;
    private static final int TIME_COUNT = 120000; // таймер в 2 минуты
    private ObservableList<String> listUserModel;
    private ArrayList<String> arrUsers;
    public LoginFrame loginFrame;

    @FXML
    ListView<String> listFx;
    @FXML
    TextField textField;
    @FXML
    TextArea textArea;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            openConnection();
            autorizQuestion();
            loadAllMsg();
            readMsg();
        } catch (SocketTimeoutException e) {
            System.out.println("Пользователь был отключен из за бездействия!");
            loginFrame.dispose();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void clikButton_1() {
        if (!textField.getText().trim().isEmpty()) {
            strFromClient = textField.getText();
            sendMessage(strFromClient);
            textField.clear();
            textField.requestFocus();
        }
    }

    @FXML
    public void showLoginForm() {
        // запускаем приложение авторизации пользователя в отдельном потоке
        new Thread(() -> {
            try {
                loginFrame = new LoginFrame(this);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Не введены вовремя логин и пароль ");
            }
        }).start();
    }

    @FXML
    public synchronized void autorizQuestion() {
        showLoginForm();
        // запускаем в основном потоке приложения слушатель сообщения о подтверждении авторизации с сервера
        try {
            listUserModel = null;
            arrUsers = new ArrayList<>();
            while (true) {
                strFromServer = in.readUTF();
                if (strFromServer.startsWith("/authok")) {
                    String[] parts = strFromServer.split("\\s+");
                    for (int i = 1; i < parts.length; i++)                  //создаем и наполняем список пользователей
                        arrUsers.add(parts[i]);
                    listUserModel = FXCollections.observableArrayList(arrUsers);
                    listFx.setItems(listUserModel);                         // Устанавливаем список зарегистрированных пользователей для своего клиента
                    socket.setSoTimeout(0);
                    continue;
                }
                if (strFromServer.startsWith("/uname")) {
                    String[] parts = strFromServer.split("\\s+");
                    myName = parts[1];
                    Platform.runLater(() -> Main.getpStage().setTitle("Net-chat:  " + myName));
                    break;
                }
                if (strFromServer.startsWith("/authno")) {
                    socket.setSoTimeout(TIME_COUNT);       // сбрасываем ожидание ввода данных для сокета
                    JOptionPane.showMessageDialog(null, "Авторизация не пройдена");
                    // снова выводим на экран форму ввода логина и пароля
                    showLoginForm();
                    continue;
                }
            }
        } catch (SocketTimeoutException e) {
            System.out.println("Пользователь был отключен из за бездействия.");
            loginFrame.dispose();
            Platform.exit();
        } catch (Exception e) {
            e.printStackTrace();
            Platform.exit();
        }
    }

    @FXML
    public void readMsg() {
        new Thread(() -> {
            try {
                while (true) {
                    strFromServer = in.readUTF();
                    if (strFromServer.startsWith("/end")) {
                        closeConnection();
                        Platform.exit();
                    }
                    if (strFromServer.startsWith("/conected")) {             // при соединении нового пользователя обновляем список активных пользователей
                        String[] parts = strFromServer.split("\\s+");
                        ObservableList<String> finalListUserModel = listUserModel;
                        Platform.runLater(() -> finalListUserModel.add(parts[1]));
                        continue;
                    }
                    if (strFromServer.startsWith("/disconected")) {          // при Отсоединении пользователя обновляем список активных пользователей
                        String[] parts = strFromServer.split("\\s+");
                        ObservableList<String> finalListUserModel = listUserModel;
                        Platform.runLater(() -> finalListUserModel.remove(parts[1]));
                        continue;
                    }
                    textArea.appendText(strFromServer + "\n");
                    saveMsgToFile(strFromServer);
                }
            } catch (Exception e) {
                Platform.exit();
            }
        }).start();
    }

    @FXML
    public void saveMsgToFile(String msg) {
        if (!msg.contains(":")) return;
        try (BufferedWriter in = new BufferedWriter(new FileWriter("chat-client/chathistory/" + myName + "_msg.txt", true))) {
            in.write(msg);
            in.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void loadAllMsg() {
        int i;
        String str;
        ArrayList<String> loadMsg = new ArrayList<>();
        File file = new File("chat-client/chathistory/" + myName + "_msg.txt");
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            while ((str = reader.readLine()) != null) {
                loadMsg.add(str);
            }
            i = loadMsg.size() - 10;
            i = (i <= 0) ?  0 : loadMsg.size() - 10;
            for ( ; i < loadMsg.size(); i++) {
                textArea.appendText(loadMsg.get(i) + "\n");
            }
            textArea.appendText(LocalDate.now().toString() + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void openConnection() throws IOException {
        socket = new Socket(SERVER_ADDR, SERVER_PORT);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        socket.setSoTimeout(TIME_COUNT);
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
        if (mouseEvent.getButton() == PRIMARY) {
            new Thread(() -> {
                String msg = JOptionPane.showInputDialog("Message for: " + nameUser);
                try {
                    if (!msg.trim().isEmpty()) {
                        sendMessage("/w " + nameUser + " " + msg);
                    } else {
                        JOptionPane.showMessageDialog(null, "Nothing send");
                    }
                } catch (NullPointerException e) {
                    JOptionPane.showMessageDialog(null, "Error send message");
                }
            }).start();
        } else {
            // если левый клик то даем окно с запросом смены ника пользователя
            new Thread(() -> {
                String msg = JOptionPane.showInputDialog("Change name: ");
                try {
                    if (!msg.trim().isEmpty()) {  // если сообщение не пустое и не свой ник то проверяем его  с никами в сети
                        for (String nameUserInList : arrUsers) {
                            if (msg.trim().equals(nameUserInList.trim())) {
                                JOptionPane.showMessageDialog(null, "Error change name");
                                return;
                            }
                        }
                        arrUsers.set(arrUsers.indexOf(myName), msg);
                        myName = msg;
                        sendMessage("/changename " + msg);
                    } else {
                        JOptionPane.showMessageDialog(null, "Enter name.");
                    }
                } catch (NullPointerException e) {
                    JOptionPane.showMessageDialog(null, "Error change name");
                }
            }).start();
        }
    }
}

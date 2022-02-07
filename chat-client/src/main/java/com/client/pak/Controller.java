package com.client.pak;

import com.client.pak.message.Bubble;
import com.client.pak.message.CellRenderer;
import com.client.pak.message.MessgePane;
import com.client.pak.message.UserCell;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import java.io.*;
import java.net.URL;
import java.util.*;

public class Controller implements Initializable {
    private Connection connection;
    private String strFromClient;
    private String myName;
    private ObservableList<UserCell> listUserModel;
    private List<UserCell> userList;
    private Map<String, GridPane> messgePanes;
    private String useNowPane;

    @FXML
    GridPane regPane;
    @FXML
    TextField regLogin;
    @FXML
    PasswordField regPassword;
    @FXML
    PasswordField regPasswordRep;
    @FXML
    TextField regName;
    @FXML
    Label regMessage;

    @FXML
    ScrollPane scrollPane;

    @FXML
    TextField authLogin;
    @FXML
    PasswordField authPassword;
    @FXML
    ListView<UserCell> listFx;
    @FXML
    TextField textField;
    @FXML
    HBox chatPane;
    @FXML
    GridPane chat;

    @FXML
    GridPane authPane;
    @FXML
    Label authMessage;

    @FXML
    GridPane setPane;
    @FXML
    TextField setName;
    @FXML
    Label setMessage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        changeStageToAuth();
        scrollPane.setFitToWidth(true);
        messgePanes = new HashMap();
        messgePanes.put("Общий чат", chat);
        useNowPane = "Общий чат";
    }

    @FXML
    public void SendButton() {
        if (!textField.getText().trim().isEmpty()) {
            strFromClient = textField.getText();
            Bubble chatMessage = new Bubble(myName, strFromClient, "");
            messgePanes.get(useNowPane).setHalignment(chatMessage, HPos.RIGHT);
            messgePanes.get(useNowPane).setValignment(chatMessage, VPos.BOTTOM);
            Platform.runLater(() -> {
                messgePanes.get(useNowPane).addRow(messgePanes.get(useNowPane).getRowCount(), chatMessage);
                scrollDown();
            });
            if(useNowPane.equals("Общий чат")){                             // если чат общий то посылаем обычное сообщение
                connection.sendMessage(strFromClient);
            }else{                                                          // если сообщение персональное то: /personal кому отКого и самоСообщение
                connection.sendMessage("/personal " + useNowPane + " " + myName + " " + strFromClient);
            }
            textField.clear();
            textField.requestFocus();
        }
    }

    public void sendDisconnect(MouseEvent mouseEvent) throws IOException {
        connection.sendMessage("/end");
        connection.closeConnection();
        connection = null;
        myName = " ";
        changeStageToAuth();
    }

    public void changeStageToSet(MouseEvent mouseEvent) {
        Platform.runLater(() -> {
            setName.clear();
            setMessage.setVisible(false);
        });
        authPane.setVisible(false);
        regPane.setVisible(false);
        chatPane.setVisible(false);
        setPane.setVisible(true);
    }

    public void sendStatus() throws IOException {
//        if (!status.getText().strip().isEmpty()) {
//            Message message = new Message();
//            message.setMessageType(MessageType.LIST);
//            message.setText(status.getText());
//            connection.send(message);
//            status.clear();
//        }
        System.out.println("sendStatus");
    }

    public void enterChat(ActionEvent event) {
        if (connection == null) {
            connection = new Connection(this);
            new Thread(connection).start();
        }
        if (authLogin.getText().isEmpty() || authPassword.getText().isEmpty()) {
            authMessage.setText("Enter login and password");
            authMessage.setVisible(true);
        } else {
            connection.sendMessage("/auth " + authLogin.getText() + " " + authPassword.getText());
        }
    }

    public void changeStageToAuth() {
        Platform.runLater(() -> {
            authLogin.clear();
            authPassword.clear();
        });
        authPane.setVisible(true);
        authMessage.setVisible(false);
        regPane.setVisible(false);
        chatPane.setVisible(false);
        setPane.setVisible(false);
    }

    public void changeStageToChat() {
        chatPane.setVisible(true);
        authPane.setVisible(false);
        regPane.setVisible(false);
        setPane.setVisible(false);
    }

    public void changeStageToReg() {
        Platform.runLater(() -> {
            regLogin.clear();
            regPassword.clear();
            regPasswordRep.clear();
            regName.clear();
        });
        regPane.setVisible(true);
        regMessage.setVisible(false);
        authPane.setVisible(false);
        chatPane.setVisible(false);
        setPane.setVisible(false);
    }

    public void loadListUsers(String[] parts) {
        listUserModel = null;
        userList = new ArrayList<>();
        userList.add(new UserCell("Общий чат", "посетителей"));
        for (int i = 1; i < parts.length; i++) {                            //создаем и наполняем список пользователей
            if(parts[i].equals(myName)){                                    //если мое имя совпадает с именем в списке то пропускаем его
                continue;
            }
            userList.add(new UserCell(parts[i], "On line"));          // добавляем пользователей в список он-лайн пользователей
            messgePanes.put(parts[i], new MessgePane(parts[i]));            // добавляем панель личных сообщений для юзера
        }
        listUserModel = FXCollections.observableArrayList(userList);
        Platform.runLater(() -> {
            listFx.setItems(listUserModel);                       // Устанавливаем список зарегистрированных пользователей для своего клиента
            listFx.setCellFactory(new CellRenderer());
        });
    }

    public void register() {
        if (connection == null) {
            connection = new Connection(this);
            new Thread(connection).start();
        }
        if (regLogin.getText().isEmpty() || regPassword.getText().isEmpty() ||
                regPasswordRep.getText().isEmpty() || regName.getText().isEmpty()) {
            regMessage.setTextFill(Color.RED);
            regMessage.setText("Enter login, password and name");
            regMessage.setVisible(true);
        } else if (!regPassword.getText().equals(regPasswordRep.getText())) {
            regMessage.setTextFill(Color.RED);
            regMessage.setText("Passwords do not match");
            regMessage.setVisible(true);
        } else {
            connection.sendMessage("/reguser " + regName.getText() + " " + regLogin.getText() + " " + regPassword.getText());
        }
    }

    public void scrollDown() {
        final Timeline timeline = new Timeline();
        final KeyValue kv = new KeyValue(scrollPane.vvalueProperty(), 1.0);
        final KeyFrame kf = new KeyFrame(Duration.millis(500), kv);
        timeline.getKeyFrames().add(kf);
        timeline.play();
    }

    public void moseClickOnListItem(MouseEvent mouseEvent) {
        String nameUser = listFx.getSelectionModel().getSelectedItem().getName();
        scrollPane.setContent(messgePanes.get(nameUser));
        useNowPane = nameUser;
    }

    public void saveAccChanges(ActionEvent event) throws IOException {
        if (!setName.getText().isEmpty()) {
            new Thread(() -> {
                try {
                    if (!setName.getText().trim().isEmpty() && !setName.getText().trim().equals(myName)) {  // если сообщение не пустое и не свой ник то проверяем его с никами в сети
                        for (UserCell userInList : userList) {
                            String nameUserInList = userInList.getName();
                            if (setName.getText().trim().equals(nameUserInList.trim())) {
                                Platform.runLater(() -> {
                                    setMessage.setTextFill(Color.RED);
                                    setMessage.setText("Error change name.");
                                    setMessage.setVisible(true);
                                });
                                return;
                            }
                        }
                        Platform.runLater(() -> Main.getpStage().setTitle("Net-chat:  " + setName.getText()));
                        connection.sendMessage("/changename " + setName.getText() + " "+ myName );
                        myName = setName.getText();
                        Platform.runLater(() -> {
                            setMessage.setTextFill(Color.GREEN);
                            setMessage.setText("Name change accepted.");
                            setMessage.setVisible(true);
                        });

                    } else {
                        Platform.runLater(() -> {
                            setMessage.setTextFill(Color.RED);
                            setMessage.setText("Enter new name.");
                            setMessage.setVisible(true);
                        });
                    }
                } catch (NullPointerException e) {
                    Platform.runLater(() -> {
                        setMessage.setTextFill(Color.RED);
                        setMessage.setText("Can't change name.");
                        setMessage.setVisible(true);
                    });
                }
            }).start();
        }
    }

    public void loadAllMsg() {
        int i;
        String str;
        ArrayList<String> loadMsg = new ArrayList<>();
        File file = new File("chat-client/chathistory/" + getMyName() + "_msg.txt");
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            while ((str = reader.readLine()) != null) {
                loadMsg.add(str);
            }
            i = loadMsg.size() - 10;
            i = (i <= 0) ? 0 : loadMsg.size() - 10;
            for (; i < loadMsg.size(); i++) {
                String[] parts = loadMsg.get(i).split("\\s+");
                StringBuilder sb = new StringBuilder();
                for (int j = 1; j < parts.length; j++) {
                    sb.append(parts[j]).append(" ");
                }
                Bubble chatMessage = new Bubble(parts[0], sb.toString().trim(), "");
                GridPane.setValignment(chatMessage, VPos.BOTTOM);
                if (!parts[0].equals(getMyName())) {
                    GridPane.setHalignment(chatMessage, HPos.LEFT);
                } else {
                    GridPane.setHalignment(chatMessage, HPos.RIGHT);
                }
                Platform.runLater(() -> chat.addRow(chat.getRowCount(), chatMessage));
            }
            Platform.runLater(() -> scrollDown());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveMsgToFile(String msg) {
        try (BufferedWriter in = new BufferedWriter(new FileWriter("chat-client/chathistory/" + getMyName() + "_msg.txt", true))) {
            in.write(msg);
            in.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void wrongUser() {
        Platform.runLater(() -> authMessage.setText("Wrong login or password"));
        authMessage.setVisible(true);
    }

    public Connection getConnection() {
        return connection;
    }

    public Map<String, GridPane> getMessgePanes() {
        return messgePanes;
    }

    public String getMyName() {
        return myName;
    }

    public void addUserInListFx(String userName) {
        Platform.runLater(() -> {
            userList.add(new UserCell(userName, "On line"));
            ObservableList<UserCell> users = FXCollections.observableArrayList(userList);
            listFx.setItems(users);
            listFx.setCellFactory(new CellRenderer());
        });
    }

    public void removeUsers(String userName) {
        for (int i = 0; i < userList.size(); i++) {
            UserCell userCell = userList.get(i);
            if (userCell.getName().equals(userName)) {
                userList.remove(i);
            }
        }
        Platform.runLater(() -> {
            ObservableList<UserCell> users = FXCollections.observableArrayList(userList);
            listFx.setItems(users);
            listFx.setCellFactory(new CellRenderer());
        });
    }

    public void setMyName(String myName) {
        this.myName = myName;
    }
}

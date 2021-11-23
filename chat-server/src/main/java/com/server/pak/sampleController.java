package com.server.pak;
/**
 * Домашнее задание Шевеленко Андрея к 7 лекции Java 2
 */
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class sampleController{
    @FXML
    ServerApp server;
    @FXML
    TextField textField;
    @FXML
    TextArea textArea;
    @FXML
    public void initialize(){
        server = new ServerApp();
    }
    @FXML
    public void clikButton_1(){                         // по клику кнопки посылаем всем пользователям сообщение
        textArea.appendText("[Server]: "+textField.getText()+"\n");
        for(ClientHandler client: server.getClients())
            client.sendMessage();
        textField.clear();
        textField.requestFocus();
    }
}

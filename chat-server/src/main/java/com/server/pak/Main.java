package com.server.pak;
/**
 * Домашнее задание Шевеленко Андрея к 6 лекции Java 2
 */

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    private FXMLLoader loader;
    private sampleController controller;
    ServerApp server;
    public void start(Stage primaryStage) throws Exception{
        loader = new FXMLLoader(getClass().getResource("/sample.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Сервер");
        primaryStage.setScene(new Scene(root, 300, 500));
        primaryStage.show();
    }
    public void stop(){
        controller = loader.getController();   // Обращаемся к классу контроллера и забираем ссылку на сервер
        server=controller.server;
        for(ClientHandler client: server.getClients()){     // закрываем для всех клиентов сервера соединения.
            client.closeConnection();
        }

    }


    public static void main(String[] args) {
        launch(args);
    }
}
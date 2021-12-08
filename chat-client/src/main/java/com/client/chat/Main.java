package com.client.chat;
/**
 * Домашнее задание Шевеленко Андрея к 8 лекции Java 2
 */

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    private FXMLLoader loader;
    private sampleController controller;
    public static Stage pStage;

    public void start(Stage primaryStage) throws Exception {
        pStage = primaryStage;
        loader = new FXMLLoader(getClass().getResource("/sample.fxml"));
        controller = loader.getController();
        Parent root = loader.load();
        primaryStage.setScene(new Scene(root, 400, 500));
        primaryStage.setTitle("Net-chat:");
        primaryStage.show();
    }

    public static Stage getpStage() {
        return pStage;
    }

    public void stop() {
        controller = loader.getController();
        controller.sendMessage("/end");
        controller.closeConnection();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
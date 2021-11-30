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
    public void start(Stage primaryStage) throws Exception{
        loader = new FXMLLoader(getClass().getResource("/sample.fxml"));
        controller = loader.getController();
        Parent root = loader.load();
        primaryStage.setTitle("Net-chat");
        primaryStage.setScene(new Scene(root, 400, 500));
        primaryStage.show();
    }
    public void stop(){
        controller = loader.getController();
        controller.sendMessage("/end");
        controller.closeConnection();
    }
    public static void main(String[] args) {
        launch(args);
    }
}
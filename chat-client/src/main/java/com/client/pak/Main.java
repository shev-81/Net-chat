package com.client.pak;

import message.Message;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    private FXMLLoader loader;
    private Controller controller;
    public static Stage pStage;

    public void start(Stage primaryStage) throws Exception {
        pStage = primaryStage;
        loader = new FXMLLoader(getClass().getResource("/sample.fxml"));
        controller = loader.getController();
        Parent root = loader.load();
        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("Net-chat:");
        primaryStage.show();
    }

    public static Stage getpStage() {
        return pStage;
    }

    public void stop() {
        try{
            controller = loader.getController();
            controller.getConnection().sendMessage(new Message(Message.MessageType.END));
            controller.getConnection().closeConnection();
        }catch (NullPointerException e){}
    }

    public static void main(String[] args) {
        launch(args);
    }
}
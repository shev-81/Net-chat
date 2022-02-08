package com.client.pak;

import javafx.application.Platform;
import java.io.*;
import java.net.Socket;
import message.*;

public class Connection implements Runnable {

    private final String SERVER_ADDR = "localhost";  //192.168.1.205";
    private final int SERVER_PORT = 8189;
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    public static final int TIME_COUNT = 120000; // таймер в 2 минуты
    private Controller controller;
    public ReaderMessagesClient readerMessages;
    private Message message;

    public Connection(Controller controller) {
        this.controller = controller;
        openConnection();
    }

    @Override
    public void run() {
        this.readerMessages = new ReaderMessagesClient(controller, this);
        autorizQuestion();
        Platform.runLater(() -> controller.loadAllMsg());
        readMsg();
    }

    public void openConnection() {
        try {
            socket = new Socket(SERVER_ADDR, SERVER_PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            socket.setSoTimeout(TIME_COUNT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void autorizQuestion() {
        try {
            boolean chek = true;
            while (chek) {
                message = (Message) in.readObject();
                chek = readerMessages.read(message);
            }
        } catch (Exception e) {
            controller.changeStageToAuth();
        }
    }

    public void readMsg() {
        try {
            boolean chek = true;
            while (chek) {
                message = (Message) in.readObject();
                chek = readerMessages.read(message);
            }
        } catch (Exception e) {
            controller.changeStageToAuth();
        }
    }

    public void sendMessage(Message message) {
        try {
            out.writeObject(message);
            out.reset();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Socket getSocket() {
        return socket;
    }

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

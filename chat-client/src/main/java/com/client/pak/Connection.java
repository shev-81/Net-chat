package com.client.pak;

import com.client.pak.message.MessageType;
import com.client.pak.message.ReaderMessages;
import javafx.application.Platform;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class Connection implements Runnable {
    private final String SERVER_ADDR = "localhost";  //192.168.1.205";
    private final int SERVER_PORT = 8189;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String strFromServer;
    public static final int TIME_COUNT = 120000; // таймер в 2 минуты
    private Controller controller;
    private ReaderMessages readerMessages;

    public Connection(Controller controller) {
        this.controller = controller;
        openConnection();
    }

    @Override
    public void run() {
        this.readerMessages = new ReaderMessages(controller, this);
        autorizQuestion();
        Platform.runLater(() -> controller.loadAllMsg());
        readMsg();
    }

    public void openConnection() {
        try {
            socket = new Socket(SERVER_ADDR, SERVER_PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            socket.setSoTimeout(TIME_COUNT);
        } catch (SocketTimeoutException e) {
            System.out.println("Пользователь был отключен из за бездействия!");
            Platform.exit();
        } catch (SocketException e) {
            e.printStackTrace();
            Platform.exit();
        } catch (IOException e) {
            e.printStackTrace();
            Platform.exit();
        }
    }

    public void autorizQuestion() {
        // запускаем в основном потоке приложения слушатель сообщения о подтверждении авторизации с сервера
        try {
            while (true) {
                strFromServer = in.readUTF();
                if (strFromServer.startsWith("/authok")) {
                    readerMessages.read(strFromServer, MessageType.AUTHOK);
                    break;
                }
                if (strFromServer.startsWith("/uname")) {
                    readerMessages.read(strFromServer, MessageType.UNAME);
                    continue;
                }
                if (strFromServer.startsWith("/authno")) {
                    readerMessages.read(strFromServer, MessageType.AUTHNO);
                    continue;
                }
            }
        } catch (SocketTimeoutException e) {
            System.out.println("Пользователь был отключен из за бездействия.");
            Platform.exit();
        } catch (Exception e) {
            e.printStackTrace();
            Platform.exit();
        }
    }

    public void readMsg() {
        try {
            while (true) {
                strFromServer = in.readUTF();
                String[] parts = strFromServer.split("\\s+");
                if (strFromServer.startsWith("/conected")) {             // при соединении нового пользователя обновляем список активных пользователей
                    readerMessages.read(strFromServer, MessageType.CONECTED);
                    continue;
                }
                if (strFromServer.startsWith("/disconected")) {          // при Отсоединении пользователя обновляем список активных пользователей
                    readerMessages.read(strFromServer, MessageType.DISCONECTED);
                    continue;
                }
                if (strFromServer.startsWith("/changename")) {
                    readerMessages.read(strFromServer, MessageType.CHANGENAME);
                    continue;
                }
                if (strFromServer.startsWith("/personal")) {             //personal от кого и само сообщение
                    readerMessages.read(strFromServer, MessageType.PERSONAL);
                    continue;
                }
                if (!parts[0].equals(controller.getMyName())) {
                    readerMessages.read(strFromServer, MessageType.UMESSAGE);
                }
                controller.saveMsgToFile(strFromServer);
            }
        } catch (SocketException e) {
            e.toString();
            System.out.println("socket close ");
            controller.changeStageToAuth();
        } catch (Exception e) {
            e.printStackTrace();
            controller.changeStageToAuth();
            Platform.exit();
        }
    }

    public void sendMessage(String str) {
        try {
            out.writeUTF(str);
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

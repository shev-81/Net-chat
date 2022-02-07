package com.server.pak;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.sql.SQLException;

public class ClientHandler {
    private static final Logger LOGGER = LogManager.getLogger(ClientHandler.class);
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String name;
    private String nickName;
    private ServerApp serverApp;
    private ReaderMessagesServer readerMessages;

    public ClientHandler(ServerApp serverApp, Socket socket) throws IOException {
        try {
            this.serverApp = serverApp;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            this.name = "";
            this.readerMessages = new ReaderMessagesServer(serverApp);
            autentification();
            readMessages();
        } catch (IOException | SQLException e) {
            e.printStackTrace();
            LOGGER.info("Ошибка при создании слушателя клиента... ");
            closeConnection();
        }
    }

    public void autentification() throws IOException, SocketException, SQLException {
        while (true) {
            String str = in.readUTF();
            if (str.startsWith("/reguser")) {    // если пришел запрос о регистрации
                if(readerMessages.read(str, MessageType.REGUSER, this)){
                    return;
                }
            }
            if (str.startsWith("/auth")) {    // если пришел запрос о проверки регистрации
                if(readerMessages.read(str, MessageType.AUTH, this)){
                    return;
                }
            }
        }
    }

    public void readMessages() throws IOException, SQLException {
        while (true) {
            String str = in.readUTF();
            LOGGER.info("от " + name + ": " + str);
            if (str.toLowerCase().startsWith("/personal")) {      // personal кому от кого и само сообщение
                readerMessages.read(str, MessageType.PERSONAL, this);
                continue;
            }
            if (str.toLowerCase().startsWith("/changename")) {   // смена имени на новое
                readerMessages.read(str, MessageType.CHANGENAME, this);
                continue;
            }
            if (str.toLowerCase().startsWith("/end")) {     // если пришло сообщение о закрытии закрываем подключение
                readerMessages.read(str, MessageType.END, this);
                break;
            }
            serverApp.sendAll(name + " " + str);
        }
        closeConnection();
    }

    public void sendMessage(String string) {
        try {
            out.writeUTF(string);
        } catch (SocketException e) {
            LOGGER.info("Пользователь разорвал соединение.");
        } catch (IOException e) {
            LOGGER.throwing(Level.WARN, e);
            e.printStackTrace();
        }
    }

    public void sendPrivateMessage(String str) {    // /personal кому от кого и само сообщение
        String[] parts = str.split("\\s+");
        StringBuilder msg = new StringBuilder();    // создаем строку сообщения не учитывая служебные команды
        for (int i = 3; i < parts.length; i++) {    // собираем оставшуюся строку в сообщение
            msg.append(parts[i]).append(" ");
        }
        if (serverApp.isNickBusy(parts[1])) {           // если имя занято то клиент есть и посылаем ему сообщение
            serverApp.getClient(parts[1]).sendMessage("/personal " + parts[2] + " " + msg);
        } else {
            LOGGER.info("[Server]: " + parts[1] + " not in network");
        }
    }

    public void closeConnection() {
        try {
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            LOGGER.throwing(Level.WARN, e);
            e.printStackTrace();
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getName() {
        return name;
    }
}

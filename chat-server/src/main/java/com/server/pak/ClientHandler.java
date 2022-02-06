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

    public ClientHandler(ServerApp serverApp, Socket socket) throws IOException {
        try {
            this.serverApp = serverApp;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            this.name = "";
            autentification();
            readMessages();
        } catch (IOException | SQLException e) {
            LOGGER.info("Ошибка при создании слушателя клиента... ");
            closeConnection();
        }
    }

    public void autentification() throws IOException, SocketException {
        while (true) {
            String str = in.readUTF();
            if (str.toLowerCase().contains("/end")) {     // если пришло сообщение о закрытии закрываем подключение
                LOGGER.info("[Server]: Unknow User disconnected!");
                throw new SocketException("потльзователь не подтвердил авторизацию.");
            }
            if (str.startsWith("/reguser")) {    // если пришел запрос о регистрации
                System.out.println(str);
                String[] parts = str.split("\\s+");
                if (serverApp.getAuthService().registerNewUser(parts[1], parts[2], parts[3])) {
                    name = parts[1];
                    serverApp.sendAll("/conected " + name);
                    serverApp.subscribe(this);
                    sendMessage("/authok " + serverApp.getClientsList());
                    sendMessage("/uname " + name);
                    return;
                } else {
                    LOGGER.info("[Server]: регистрация нового пользователя не прошла");
                    sendMessage("/authno");
                }
            }
            if (str.startsWith("/auth")) {    // если пришел запрос о проверки регистрации
                String[] parts = str.split("\\s+");
                try {
                    nickName = serverApp.getAuthService().getNickByLoginPass(parts[1], parts[2]);
                } catch (Exception e) {
                    nickName = null;
                }
                if (nickName != null) {
                    if (!serverApp.isNickBusy(nickName)) {
                        name = nickName;
                        serverApp.sendAll("/conected " + name);
                        serverApp.subscribe(this);
                        sendMessage("/uname " + name);
                        sendMessage("/authok " + serverApp.getClientsList());
                        LOGGER.info("[Server]: " + name + " авторизовался.");
                        return;
                    } else {
                        sendMessage("/authno");
                    }
                } else {
                    sendMessage("/authno");
                }
            }
        }
    }

    public void readMessages() throws IOException, SQLException {
        while (true) {
            String str = in.readUTF();
            LOGGER.info("от " + name + ": " + str);
            if (str.toLowerCase().startsWith("/personal")) {      // personal кому от кого и само сообщение
                sendPrivateMessage(str);
                continue;
            }
            if (str.toLowerCase().startsWith("/changename")) {   // смена имени на новое
                LOGGER.info("[Server]: "+ str + " запрос на смену имени");
                String[] parts = str.split("\\s+");
                boolean rezult = serverApp.getAuthService().updateNickName(parts[1], parts[2]);
                if(rezult) {
                    serverApp.sendAll("/changename " + parts[1] + " " + parts[2]);
                    LOGGER.info("[Server]: "+ str + " запрос на смену имени УДОВЛЕТВОРЕН");
                }else{
                    LOGGER.warn("[Server]: "+ str + " запрос на смену имени НЕ УДОВЛЕТВОРЕН");
                }
                name = parts[1];
                continue;
            }

            if (str.toLowerCase().startsWith("/end")) {     // если пришло сообщение о закрытии закрываем подключение
                serverApp.unSubscribe(this);
                //serverApp.sendAll(name + " отключился.");
                serverApp.sendAll("/disconected " + name);
                LOGGER.info("[Server]: " + name + " disconnected!");
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
    public String getName() {
        return name;
    }
}

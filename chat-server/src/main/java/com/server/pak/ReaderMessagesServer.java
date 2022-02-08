package com.server.pak;

import message.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.sql.SQLException;

public class ReaderMessagesServer {

    private static final Logger LOGGER = LogManager.getLogger(ReaderMessagesServer.class);
    private ServerApp server;

    public ReaderMessagesServer(ServerApp server) {
        this.server = server;
    }

    public boolean read(Message message, ClientHandler clientHandler) throws SQLException {
        Message.MessageType type = message.getType();
        switch (type) {
            case AUTH: return auth(message, clientHandler);
            case REGUSER: return regUser(message, clientHandler);
            case END: end(clientHandler); break;
            case CHANGENAME: changeName(message, clientHandler); break;
            case PERSONAL: personal(message, clientHandler); break;
            case UMESSAGE: uMessage (message); break;
            case STATUS: status(message); break;
        }
        return true;
    }

    public boolean auth(Message message, ClientHandler clientHandler){
        String nickName;
        try {
            nickName = server.getAuthService().getNickByLoginPass(message.getLogin(), message.getPass());
        } catch (Exception e) {
            nickName = null;
        }
        if (nickName != null) {
            if (!server.isNickBusy(nickName)) {
                clientHandler.setName(nickName);
                server.subscribe(clientHandler);
                message = new Message(Message.MessageType.AUTHOK);
                message.setNameU(nickName);
                message.setUsersList(server.getClientsList());
                clientHandler.sendMessage(message);
                message = new Message(Message.MessageType.CONECTED);
                message.setNameU(nickName);
                server.sendAll(message);
                LOGGER.info("[Server]: " + nickName + " авторизовался.");
                return false;
            } else {
                clientHandler.sendMessage(new Message(Message.MessageType.AUTHNO));
                return true;
            }
        } else {
            clientHandler.sendMessage(new Message(Message.MessageType.AUTHNO));
            return true;
        }
    }

    public boolean regUser(Message message, ClientHandler clientHandler){
        if (server.getAuthService().registerNewUser(message.getNameU(), message.getLogin(), message.getPass())) {
            clientHandler.setName(message.getNameU());
            message.setType(Message.MessageType.CONECTED);
            server.sendAll(message);
            server.subscribe(clientHandler);
            message.setType(Message.MessageType.AUTHOK);
            message.setUsersList(server.getClientsList());
            clientHandler.sendMessage(message);
            return false;
        } else {
            LOGGER.info("[Server]: регистрация нового пользователя не прошла");
            message.setType(Message.MessageType.AUTHNO);
            clientHandler.sendMessage(message);
            return true;
        }
    }

    public void end(ClientHandler clientHandler){
        server.unSubscribe(clientHandler);
        Message message = new Message(Message.MessageType.DISCONECTED);
        message.setNameU(clientHandler.getName());
        server.sendAll(message);
        LOGGER.info("[Server]: " + clientHandler.getName() + " disconnected!");
    }

    public void changeName(Message message, ClientHandler clientHandler) throws SQLException {
        LOGGER.info("[Server]: " + message.getNameU() + " запросил на смену имени на " + message.getToNameU());
        boolean rezult = server.getAuthService().updateNickName(message.getToNameU(), message.getNameU());    //   String newName,  String oldName
        if (rezult) {
            clientHandler.setName(message.getToNameU());
            server.sendAll(message);
            LOGGER.info("[Server]: Запрос на смену имени с " + message.getNameU() + " на " + message.getToNameU() + " УДОВЛЕТВОРЕН");
        } else {
            LOGGER.info("[Server]: Запрос на смену имени с " + message.getNameU() + " на " + message.getToNameU() + " НЕ УДОВЛЕТВОРЕН");
        }
    }

    public void personal(Message message, ClientHandler clientHandler){
        clientHandler.sendPrivateMessage(message);
    }

    public void uMessage(Message message){
        server.sendAll(message);
    }

    public void status (Message message){
        server.sendAll(message);
    }
}

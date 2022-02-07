package com.server.pak;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.sql.SQLException;

public class ReaderMessagesServer {

    private static final Logger LOGGER = LogManager.getLogger(ReaderMessagesServer.class);
    private ServerApp server;

    public ReaderMessagesServer(ServerApp server) {
        this.server = server;
    }

    public boolean read(String strFromServer, MessageType messageType, ClientHandler clientHandler) throws SQLException {
        String[] parts = strFromServer.split("\\s+");
        switch (messageType) {
            case AUTH:
                String nickName;
                try {
                    nickName = server.getAuthService().getNickByLoginPass(parts[1], parts[2]);
                    clientHandler.setNickName(nickName);
                } catch (Exception e) {
                    nickName = null;
                }
                if (nickName != null) {
                    if (!server.isNickBusy(nickName)) {
                        clientHandler.setName(nickName);
                        server.sendAll("/conected " + nickName);
                        server.subscribe(clientHandler);
                        clientHandler.sendMessage("/uname " + nickName);
                        clientHandler.sendMessage("/authok " + server.getClientsList());
                        LOGGER.info("[Server]: " + nickName + " авторизовался.");
                        return true;
                    } else {
                        clientHandler.sendMessage("/authno");
                        return false;
                    }
                } else {
                    clientHandler.sendMessage("/authno");
                    return false;
                }
            case END:
                server.unSubscribe(clientHandler);
                server.sendAll("/disconected " + clientHandler.getName());
                LOGGER.info("[Server]: " + clientHandler.getName() + " disconnected!");
                break;
            case REGUSER:
                if (server.getAuthService().registerNewUser(parts[1], parts[2], parts[3])) {
                    clientHandler.setName(parts[1]);
                    server.sendAll("/conected " + parts[1]);
                    server.subscribe(clientHandler);
                    clientHandler.sendMessage("/authok " + server.getClientsList());
                    clientHandler.sendMessage("/uname " + parts[1]);
                    return true;
                } else {
                    LOGGER.info("[Server]: регистрация нового пользователя не прошла");
                    clientHandler.sendMessage("/authno");
                    return false;
                }
            case CHANGENAME:
                LOGGER.info("[Server]: "+ strFromServer + " запрос на смену имени");
                boolean rezult = server.getAuthService().updateNickName(parts[1], parts[2]);
                if(rezult) {
                    server.sendAll("/changename " + parts[1] + " " + parts[2]);
                    LOGGER.info("[Server]: "+ strFromServer + " запрос на смену имени УДОВЛЕТВОРЕН");
                    clientHandler.setName(parts[1]);
                    return true;
                }else{
                    LOGGER.warn("[Server]: "+ strFromServer + " запрос на смену имени НЕ УДОВЛЕТВОРЕН");
                    return false;
                }
            case PERSONAL:
                clientHandler.sendPrivateMessage(strFromServer);
                break;
        }
        return true;
    }
}

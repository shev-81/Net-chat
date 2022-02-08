package com.server.pak;

import message.Message;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.sql.SQLException;

public class ClientHandler {
    private static final Logger LOGGER = LogManager.getLogger(ClientHandler.class);
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private String name;
    private ServerApp serverApp;
    private ReaderMessagesServer readerMessages;

    public ClientHandler(ServerApp serverApp, Socket socket) throws IOException {
        try {
            this.serverApp = serverApp;
            this.socket = socket;
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.in = new ObjectInputStream(socket.getInputStream());
            this.name = "";
            this.readerMessages = new ReaderMessagesServer(serverApp);
            autentification();
            readMessages();
        } catch (IOException | SQLException | ClassNotFoundException e) {
            LOGGER.info("[Server]: Соединение c клиентом разорванно.");
            closeConnection();
        }
    }

    public void autentification() throws IOException, ClassNotFoundException, SQLException {
        boolean chek = true;
        while (chek) {
            Message message = (Message) in.readObject();
            chek = readerMessages.read(message, this);
        }
    }

    public void readMessages() throws IOException, SQLException, ClassNotFoundException {
        boolean chek = true;
        while (chek) {
            Message message = (Message) in.readObject();
            chek = readerMessages.read(message, this);
        }
        closeConnection();
    }

    public void sendMessage(Message message) {
        try {
            out.writeObject(message);
            out.reset();
        } catch (IOException e) {
            LOGGER.throwing(Level.WARN, e);
        }
    }

    public void sendPrivateMessage(Message message) {
        if (serverApp.isNickBusy(message.getToNameU())) {
            serverApp.getClient(message.getToNameU()).sendMessage(message);
        } else {
            LOGGER.info("[Server]: " + message.getToNameU() + " not in network");
        }
    }

    public void closeConnection() {
        try {
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            LOGGER.throwing(Level.WARN, e);
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

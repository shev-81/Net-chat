package com.server.pak;

import message.Message;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerApp {
    private static final Logger LOGGER = LogManager.getLogger(ServerApp.class); // Trace < Debug < Info < Warn < Error < Fatal
    private ArrayList<ClientHandler> clients;
    private Socket socket = null;
    private AuthService authService;

    ServerApp() {
        this.clients = new ArrayList<>();
        this.authService = new AuthServiceBD();
        ExecutorService service = Executors.newCachedThreadPool();
        try (ServerSocket serverSocket = new ServerSocket(8189)) {
            while (true) {
                LOGGER.info("Server wait connected User.");
                socket = serverSocket.accept();
                LOGGER.info("User connected.");
                service.execute(() -> {
                    try {
                        new ClientHandler(this, socket);
                    } catch (IOException e) {
                        LOGGER.throwing(Level.FATAL, e);
                    }
                });
            }
        } catch (IOException e) {
            LOGGER.throwing(Level.FATAL, e);
        } finally {
            LOGGER.info("Server is offline.");
            authService.stop();
            service.shutdown();
        }
    }

    public String[] getClientsList() {
        StringBuilder clientsList = new StringBuilder();
        for (ClientHandler client : clients) {
            clientsList.append(client.getName() + " ");
        }
        String[] parts = clientsList.toString().trim().split("\\s+");
        return parts;
    }

    public AuthService getAuthService() {
        return authService;
    }

    public ClientHandler getClient(String name) {
        for (ClientHandler client : clients) {
            if (client.getName().equals(name))
                return client;
        }
        return null;
    }

    public synchronized void sendAll(Message message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public boolean isNickBusy(String nickName) {
        for (ClientHandler client : clients) {
            if (client.getName().equals(nickName)) {
                return true;
            }
        }
        return false;
    }

    public synchronized void subscribe(ClientHandler o) {
        clients.add(o);
    }

    public synchronized void unSubscribe(ClientHandler o) {
        clients.remove(o);
    }
}

package com.server.pak;
/**
 * Домашнее задание Шевеленко Андрея к 3 лекции Java 3
 */
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerApp {
    private static final Logger LOGGER = LogManager.getLogger(Main.class); // Trace < Debug < Info < Warn < Error < Fatal
    private ArrayList<ClientHandler> clients;
    private Socket socket=null;
    private AuthService authService;

    public AuthService getAuthService() {
        return authService;
    }
    ServerApp(){
        clients = new ArrayList<>();                // инициализируем список
        authService = new AuthServiceBD();          // инициализируем список возможжных User/ov на сервере
        ExecutorService service = Executors.newCachedThreadPool();     //newFixedThreadPool(10);
        try(ServerSocket serverSocket = new ServerSocket(8189)){
            while(true){
                LOGGER.info("Server wait connected User.");
                socket = serverSocket.accept();
                LOGGER.info("User connected.");
                service.execute(()-> {
                    try {
                        new ClientHandler(this, socket);
                    } catch (SocketException e) {
                        LOGGER.throwing(Level.WARN,e);
                        e.toString();
                    } catch (IOException e) {
                        LOGGER.throwing(Level.FATAL,e);
                        e.printStackTrace();
                    }
                });
            }
        }catch (IOException e){
            LOGGER.throwing(Level.FATAL,e);
        }finally {
            LOGGER.info("Server is offline.");
            authService.stop();
            service.shutdown();
        }
    }
    public String getClientsList() {
        StringBuilder clientsList = new StringBuilder();
        for(ClientHandler client: clients){
            clientsList.append(client.getName()+" ");
        }
        return clientsList.toString();
    }
    public ClientHandler getClient(String name) {
        for(ClientHandler client: clients){
            if(client.getName().equals(name)) return client;
        }
        return null;
    }
    public synchronized void sendAll(String str){
        for(ClientHandler client: clients){
            client.sendMessage(str);
        }
    }
    public boolean isNickBusy(String nickName){
        for(ClientHandler client: clients){
            if(client.getName().equals(nickName)){
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

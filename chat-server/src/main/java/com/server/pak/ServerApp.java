package com.server.pak;
/**
 * Домашнее задание Шевеленко Андрея к 8 лекции Java 2
 */
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ServerApp {
    private ArrayList<ClientHandler> clients;
    private Socket socket=null;
    private AuthService authService;
    public AuthService getAuthService() {
        return authService;
    }
    ServerApp(){
        clients = new ArrayList<>();            // инициализируем список
        authService = new AuthServiceClass();   // инициализируем список возможжных User/ov на сервере
        try(ServerSocket serverSocket = new ServerSocket(8189)){
            while(true){
                System.out.println("Server wait connected User.");
                socket = serverSocket.accept();
                System.out.println("User connected.");
                new ClientHandler(this, socket);
            }
        }catch (IOException e){
            System.out.println("Ошибка на сервере.");
        }
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
    }public synchronized void unSubscribe(ClientHandler o) {
        clients.remove(o);
    }
}

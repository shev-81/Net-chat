package com.server.pak;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ServerApp {
    private ArrayList<ClientHandler> clients;
    public ArrayList<ClientHandler> getClients() {
        return clients;
    }
    Socket socket=null;
    ServerApp(){
        clients = new ArrayList<>();
        try(ServerSocket serverSocket = new ServerSocket(8189)){
            while(true){
                System.out.println("Server wait connected User.");
                socket = serverSocket.accept();
                System.out.println("User connected.");
                subscribe(new ClientHandler(this, socket));
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    public synchronized  void sendAll(String str){
        for(ClientHandler client: clients){
            client.sendMessage(str);
        }
    }
    public synchronized void subscribe(ClientHandler o) {
        clients.add(o);
    }

}

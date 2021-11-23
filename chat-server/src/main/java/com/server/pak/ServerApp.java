package com.server.pak;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServerApp {
    private List<ClientHandler> clients;

    public List<ClientHandler> getClients() {
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
                new ClientHandler(this,socket);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}

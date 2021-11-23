package com.server.pak;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String name;
    ServerApp serverApp;


    public ClientHandler(ServerApp serverApp, Socket socket) throws IOException {
        this.serverApp =serverApp;
        this.socket = socket;
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());
        this.name="User";
        new Thread(()->{
            readMessages();
        }).start();
    }
    public void readMessages(){
        while (true){
            try {
                String str = in.readUTF();
                if (str.toLowerCase().contains("/end")) {    // если пришло сообщение о закрытии закрываем подключение
                    System.out.println("[Server]: Server is closed!");
                    serverApp.sendAll("user disconnected");
                    //sendMessage("user disconnected");
                    closeConnection();
                    break;
                }
                serverApp.sendAll(str);
                //sendMessage(str);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void sendMessage(String string){
        try {
            out.writeUTF("Echo "+string);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void closeConnection() {
        try {
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

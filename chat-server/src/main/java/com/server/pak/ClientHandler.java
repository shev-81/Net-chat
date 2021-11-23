package com.server.pak;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private ServerApp server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String str;

    public ClientHandler(ServerApp server, Socket socket) throws IOException {
        this.server = server;
        this.socket = socket;
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        server.getClients().add(this);                          // Добавляем нового клиента к списку клиаентов на сервере
        new Thread(()->{
            readMessages();
        });
    }
    public void readMessages(){
        while (true){
            try {
                str = in.readUTF();
                if (str.equals("/end")) {    // если пришло сообщение о закрытии закрываем подключение
                    System.out.println("[Server]: Server is closed!");
                    break;
                }
                sendMessage();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                closeConnection();
            }
        }
    }
    public void sendMessage(){
        try {
            out.writeUTF("Echo "+str);
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

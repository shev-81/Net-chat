package com.server.pak;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerApp {
    public static void main(String[] args) {
        Socket socket=null;
        try(ServerSocket serverSocket = new ServerSocket(8189)){
            System.out.println("Server Online...waiting connect user!");
            // Ожидание подключения
            socket = serverSocket.accept();
            System.out.println("User connected!");
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            while (true) {
                String str = in.readUTF();
                if (str.equals("/end")) {    // если пришло сообщение о закрытии закрываем подключение
                    System.out.println("[Server]: Server is closed!");
                    break;
                }
                out.writeUTF("[Server]: " + str);
                System.out.println("[User]: "+str);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}

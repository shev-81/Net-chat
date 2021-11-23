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

    public String getName() {
        return name;
    }

    public ClientHandler(ServerApp serverApp, Socket socket) throws IOException {
        try {
            this.serverApp = serverApp;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            this.name = "";
            new Thread(() -> {
                try {
                    autentification();
                    readMessages();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    closeConnection();
                }
            }).start();
        } catch(IOException e) {
            throw new RuntimeException ("Ошибка при создании слушателя клиента... ");
        }
    }
    public void autentification() throws IOException{
        while (true){
            sendMessage("Введите логин или пароль. \n /auth login pass");
            String str = in.readUTF();
            if (str.startsWith("/auth")) {    // если пришло сообщение о регистрации
                String [] parts = str.split("\\s");
                String nickName = serverApp.getAuthService().getNickByLoginPass(parts[1],parts[2]);
                if(nickName!=null){
                    if(!serverApp.isNickBusy(nickName)){
                        sendMessage("/authok" +nickName);
                        name=nickName;
                        serverApp.sendAll(name+" присоединился.");
                        serverApp.subscribe(this);
                        return;
                    }else{sendMessage("Учетная запись используется");}
                }else {sendMessage("Не верный логин или пароль.");}
            }
        }
    }
    public void readMessages() throws IOException{
        while (true){
            String str = in.readUTF();
            System.out.println("от" +name+ ": "+ str);
            if (str.toLowerCase().contains("/end")) {     // если пришло сообщение о закрытии закрываем подключение
                System.out.println("[Server]: "+name+" disconnected!");
                serverApp.sendAll(name+" disconnected");
                return;
            }
            serverApp.sendAll(name+": "+str);
        }
    }
    public void sendMessage(String string){
        try {
            out.writeUTF(string);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void closeConnection() {
        serverApp.sendAll(name+": disconected.");
        serverApp.unSubscribe(this);
        try {
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

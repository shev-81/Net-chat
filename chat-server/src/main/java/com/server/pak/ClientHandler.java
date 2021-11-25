package com.server.pak;
/**
 * Домашнее задание Шевеленко Андрея к 8 лекции Java 2
 */
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

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
                }catch (SocketException e) {
                    e.toString();
                }
                catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    closeConnection();
                }
            }).start();
        } catch(IOException e) {
            throw new RuntimeException ("Ошибка при создании слушателя клиента... ");
        }
    }
    public void autentification() throws IOException,SocketException{
        while (true){
            sendMessage("Введите логин или пароль. \n/auth login pass");
            String str = in.readUTF();
            if (str.toLowerCase().contains("/end")) {     // если пришло сообщение о закрытии закрываем подключение
                System.out.println("[Server]: Unknow User disconnected!");
                throw new SocketException("потльзователь не подтвердил авторизацию.");
            }
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
                    }else{
                        sendMessage("Учетная запись используется");
                        sendMessage("/authno");
                    }
                }else {
                    sendMessage("Не верный логин или пароль.");
                    sendMessage("/authno");
                }
            }
        }
    }
    public void readMessages() throws IOException{
        while (true){
            String str = in.readUTF();
            System.out.println("от " +name+ ": "+ str);
            if (str.toLowerCase().contains("/w")){
                sendPrivateMessage(str);
                continue;
            }
            if (str.toLowerCase().contains("/end")) {     // если пришло сообщение о закрытии закрываем подключение
                serverApp.sendAll("\n"+name+": disconected.");
                serverApp.unSubscribe(this);
                System.out.println("[Server]: "+name+" disconnected!");
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
    public void sendPrivateMessage(String str){
        String [] parts = str.split("\\s");
        if(serverApp.isNickBusy(parts[1])){
            this.sendMessage(name+": "+parts[2]);
            serverApp.getClient(parts[1]).sendMessage(name+": "+parts[2]);
        }else{
            sendMessage("пользователя: "+parts[1]+" нет в сети.");
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

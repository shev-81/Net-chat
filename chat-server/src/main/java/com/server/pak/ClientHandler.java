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
    private String nickName;
    private String usersList;
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
            String str = in.readUTF();
            if (str.toLowerCase().contains("/end")) {     // если пришло сообщение о закрытии закрываем подключение
                System.out.println("[Server]: Unknow User disconnected!");
                throw new SocketException("потльзователь не подтвердил авторизацию.");
            }
            if (str.startsWith("/auth")) {    // если пришел запрос о проверки регистрации
                String [] parts = str.split("\\s+");
                try {
                    nickName = serverApp.getAuthService().getNickByLoginPass(parts[1], parts[2]);
                }catch (Exception e){
                    nickName=null;
                }
                if(nickName!=null){
                    if(!serverApp.isNickBusy(nickName)){
                        name=nickName;
                        serverApp.sendAll(name+" присоединился.");
                        serverApp.sendAll("/conected "+name);
                        serverApp.subscribe(this);
                        sendMessage("/authok "+serverApp.getClientsList());
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
            System.out.println("от " +name+ ": "+str);
            if (str.toLowerCase().startsWith("/w")){   //  "/w nickName msg....."
                sendPrivateMessage(str);
                continue;
            }
            if (str.toLowerCase().startsWith("/end")) {     // если пришло сообщение о закрытии закрываем подключение
                serverApp.unSubscribe(this);
                serverApp.sendAll(name+" отключился.");
                serverApp.sendAll("/disconected "+name);
                System.out.println("[Server]: "+name+" disconnected!");
                return;
            }
            serverApp.sendAll(name+": "+str);
        }
    }
    public void sendMessage(String string){
        try {
            out.writeUTF(string);
        }catch (SocketException e) {
            System.out.println("Пользователь разорвал соединение.");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void sendPrivateMessage(String str){
        String [] parts = str.split("\\s+");
        StringBuilder msg = new StringBuilder();    // создаем строку сообщения ен учитывая служебные команды
        for (int i=2; i<parts.length;i++){          // собираем оставшуюся строку в сообщение
            msg.append(parts[i]).append(" ");
        }
        if(serverApp.isNickBusy(parts[1])){         // если имя занято то клиент есть и посылаем ему сообщение
            this.sendMessage(name+": "+ msg);  // дубль сообщения себе в чат
            serverApp.getClient(parts[1]).sendMessage(name+" шепчет: "+ msg);  // и в приват выбранному имени
        }else{
            sendMessage(parts[1]+" нет в сети.");
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

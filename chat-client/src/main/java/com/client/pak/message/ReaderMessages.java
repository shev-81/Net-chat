package com.client.pak;

import com.client.pak.message.MessageType;
import com.client.pak.message.MessgePane;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.scene.layout.GridPane;
import java.net.SocketException;
import static com.client.pak.Connection.TIME_COUNT;

public class ReaderMessages {

    private Controller controller;
    private Connection connection;
    private Bubble chatMessage;
    private StringBuilder sb;


    public ReaderMessages(Controller controller, Connection connection) {
        this.controller = controller;
        this.connection = connection;
    }

    public void read(String strFromServer, MessageType messageType) throws SocketException {
        String[] parts = strFromServer.split("\\s+");
        switch (messageType){
            case AUTHOK:
                controller.loadListUsers(parts);
                connection.getSocket().setSoTimeout(0);
                controller.changeStageToChat();
                break;
            case UNAME:
                controller.setMyName(parts[1]);
                Platform.runLater(() -> Main.getpStage().setTitle("Net-chat:  " + controller.getMyName()));
                break;
            case AUTHNO:
                connection.getSocket().setSoTimeout(TIME_COUNT);
                controller.wrongUser();
                break;
            case CONECTED:
                if (parts[1].equals(controller.getMyName())) {
                    return;
                }
                Platform.runLater(() -> controller.addUserInListFx(parts[1]));
                chatMessage = new Bubble(parts[1] + " присоединяется к чату.");
                GridPane.setHalignment(chatMessage, HPos.CENTER);
                Platform.runLater(() -> {
                    controller.getMessgePanes().put(parts[1], new MessgePane(parts[1]));
                    controller.chat.addRow(controller.getMessgePanes().get("Общий чат").getRowCount(), chatMessage);
                    controller.scrollDown();
                });
                break;
            case DISCONECTED:
                Platform.runLater(() -> controller.removeUsers(parts[1]));
                chatMessage = new Bubble(parts[1] + " покидает чат.");
                GridPane.setHalignment(chatMessage, HPos.CENTER);
                Platform.runLater(() -> {
                    controller.chat.addRow(controller.getMessgePanes().get("Общий чат").getRowCount(), chatMessage);
                    controller.scrollDown();
                });
                break;
            case CHANGENAME:
                chatMessage = new Bubble(parts[2] + " сменил имя на - " + parts[1]);
                GridPane.setHalignment(chatMessage, HPos.CENTER);
                Platform.runLater(() -> {
                    controller.removeUsers(parts[2]);
                    controller.addUserInListFx(parts[1]);
                    controller.chat.addRow(controller.getMessgePanes().get("Общий чат").getRowCount(), chatMessage);
                    controller.scrollDown();
                });
                break;
            case PERSONAL:
                sb = new StringBuilder();
                for (int i = 2; i < parts.length; i++) {
                    sb.append(parts[i]).append(" ");
                }
                chatMessage = new Bubble(parts[1], sb.toString(), "");
                GridPane.setHalignment(chatMessage, HPos.LEFT);
                GridPane usePaneChat = controller.getMessgePanes().get(parts[1]);
                Platform.runLater(() -> {
                    usePaneChat.addRow(usePaneChat.getRowCount(), chatMessage);
                    controller.scrollDown();
                });
                break;
            case UMESSAGE:
                sb = new StringBuilder();
                for (int i = 1; i < parts.length; i++) {
                    sb.append(parts[i]).append(" ");
                }
                Bubble chatMessage = new Bubble(parts[0], sb.toString(), "");
                GridPane.setHalignment(chatMessage, HPos.LEFT);
                Platform.runLater(() -> controller.chat.addRow(controller.getMessgePanes().get("Общий чат").getRowCount(), chatMessage));
                Platform.runLater(() -> controller.scrollDown());
                break;
        }
    }
}

package message;

import com.client.pak.Connection;
import com.client.pak.Controller;
import com.client.pak.Main;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.scene.layout.GridPane;
import java.net.SocketException;
import static com.client.pak.Connection.TIME_COUNT;

public class ReaderMessagesClient {

    private Controller controller;
    private Connection connection;
    private Bubble chatMessage;
    private Message.MessageType type;

    public ReaderMessagesClient(Controller controller, Connection connection) {
        this.controller = controller;
        this.connection = connection;
    }

    public boolean read(Message message) throws SocketException {
        type = message.getType();
        switch (type) {
            case AUTHOK: return aouthOk (message);
            case AUTHNO: aouthNo(); break;
            case CONECTED: connected(message); break;
            case DISCONECTED: disconnected(message); break;
            case CHANGENAME: changeName(message); break;
            case PERSONAL: personal(message); break;
            case UMESSAGE: uMessage(message); break;
            case STATUS: status(message); break;
        }
        return true;
    }

    public boolean aouthOk (Message message) throws SocketException {
        controller.setMyName(message.getNameU());
        Platform.runLater(() -> Main.getpStage().setTitle("Net-chat:  " + controller.getMyName()));
        controller.loadListUsers(message.getUsersList());
        connection.getSocket().setSoTimeout(0);
        controller.changeStageToChat();
        return false;
    }

    public void aouthNo () throws SocketException {
        connection.getSocket().setSoTimeout(TIME_COUNT);
        controller.wrongUser();
    }

    public void connected (Message message){
        if (message.getNameU().equals(controller.getMyName())) {
            return;
        }
        Platform.runLater(() -> controller.addUserInListFx(message.getNameU()));
        chatMessage = new Bubble(message.getNameU() + " присоединяется к чату.");
        GridPane.setHalignment(chatMessage, HPos.CENTER);
        Platform.runLater(() -> {
            controller.getMessagePanes().put(message.getNameU(), new MessgePane(message.getNameU()));
            controller.getMessagePanes().get("Общий чат").addRow(controller.getMessagePanes().get("Общий чат").getRowCount(), chatMessage);
            controller.scrollDown();
        });
    }

    public void disconnected (Message message){
        Platform.runLater(() -> controller.removeUsers(message.getNameU()));
        chatMessage = new Bubble(message.getNameU() + " покидает чат.");
        GridPane.setHalignment(chatMessage, HPos.CENTER);
        Platform.runLater(() -> {
            controller.getMessagePanes().get("Общий чат").addRow(controller.getMessagePanes().get("Общий чат").getRowCount(), chatMessage);
            controller.scrollDown();
        });
    }

    public void changeName (Message message){
        if(!message.getNameU().equals(controller.getMyName())){
            chatMessage = new Bubble(message.getNameU() + " сменил имя на - " + message.getToNameU());
            GridPane.setHalignment(chatMessage, HPos.CENTER);
            Platform.runLater(() -> {
                controller.removeUsers(message.getNameU());
                controller.getMessagePanes().remove(message.getNameU());
                controller.addUserInListFx(message.getToNameU());
                controller.getMessagePanes().put(message.getToNameU(), new MessgePane(message.getToNameU()));
                controller.getMessagePanes().get("Общий чат").addRow(controller.getMessagePanes().get("Общий чат").getRowCount(), chatMessage);
                controller.scrollDown();
            });
        }else{
            Platform.runLater(() -> {
                controller.getMessagePanes().remove(message.getNameU());
                controller.getMessagePanes().put(message.getToNameU(), new MessgePane(message.getToNameU()));
                controller.setMyName(message.getToNameU());
            });
        }
    }

    public void personal (Message message){
        chatMessage = new Bubble(message.getNameU(), message.getText(), "");
        GridPane.setHalignment(chatMessage, HPos.LEFT);
        GridPane usePaneChat = controller.getMessagePanes().get(message.getNameU());
        Platform.runLater(() -> {
            usePaneChat.addRow(usePaneChat.getRowCount(), chatMessage);
            controller.scrollDown();
        });
    }

    public void uMessage (Message message){
        if(message.getNameU().equals(controller.getMyName())){
            return;
        }
        Bubble chatMessage = new Bubble(message.getNameU(), message.getText(), "");
        GridPane.setHalignment(chatMessage, HPos.LEFT);
        Platform.runLater(() -> {
            controller.getMessagePanes().get("Общий чат").addRow(controller.getMessagePanes().get("Общий чат").getRowCount(), chatMessage);
            controller.scrollDown();
            controller.saveMsgToFile(message.getNameU()+" "+message.getText());
        });
    }

    public void status (Message message){
        if(message.getNameU().equals(controller.getMyName())){
            Platform.runLater(() -> {
            controller.getStatus().setText(message.getText());
            });
        }else{
            controller.upDateUserList(message);
        }
    }
}

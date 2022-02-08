package message;

import java.io.Serializable;
import java.util.Date;

public class Message implements Serializable {
    private static final long serialVersionUID = 9176873029745254542L;
    public enum MessageType {
        AUTHOK, AUTHNO, CONECTED, DISCONECTED, CHANGENAME, PERSONAL, UMESSAGE,
        AUTH, END, REGUSER, STATUS
    }

    private Date date;
    private String nameU;
    private String toNameU;
    private String text;
    private String login;
    private String pass;
    private MessageType type;
    private String [] usersList;

    public Message() {
        this.date = new Date();
    }

    public Message(MessageType type) {
        this.date = new Date();
        this.type = type;
    }

    public void setNameU(String nameU) {
        this.nameU = nameU;
    }

    public void setToNameU(String toNameU) {
        this.toNameU = toNameU;             // используется при смене имени и при посылке личного сообщения
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public void setUsersList(String [] usersList) {
        this.usersList = usersList;
    }

    public Date getDate() {
        return date;
    }

    public String getNameU() {
        return nameU;
    }

    public String getToNameU() {
        return toNameU;
    }

    public String getText() {
        return text;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String getLogin() {
        return login;
    }

    public String getPass() {
        return pass;
    }

    public MessageType getType() {
        return type;
    }

    public String [] getUsersList() {
        return usersList;
    }
}

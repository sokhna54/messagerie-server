package g2.messagerieclient.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Packet implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Type {
        LOGIN, REGISTER, LOGOUT,
        SEND_MESSAGE, GET_HISTORY, GET_USERS, MARK_READ,
        LOGIN_OK, LOGIN_FAIL, REGISTER_OK, REGISTER_FAIL,
        MESSAGE_RECEIVED, MESSAGE_HISTORY, USER_LIST,
        ERROR, STATUS_UPDATE, LOGOUT_OK
    }

    private Type type;
    private String fromUsername;
    private String toUsername;
    private String content;
    private String role;
    private LocalDateTime timestamp;
    private Object data;

    public Packet() {}

    public Packet(Type type) {
        this.type = type;
        this.timestamp = LocalDateTime.now();
    }

    public static Packet login(String username, String password) {
        Packet p = new Packet(Type.LOGIN);
        p.fromUsername = username;
        p.content = password;
        return p;
    }

    public static Packet register(String username, String password, String role) {
        Packet p = new Packet(Type.REGISTER);
        p.fromUsername = username;
        p.content = password;
        p.role = role;
        return p;
    }

    public static Packet sendMessage(String from, String to, String message) {
        Packet p = new Packet(Type.SEND_MESSAGE);
        p.fromUsername = from;
        p.toUsername = to;
        p.content = message;
        return p;
    }

    public static Packet getHistory(String from, String to) {
        Packet p = new Packet(Type.GET_HISTORY);
        p.fromUsername = from;
        p.toUsername = to;
        return p;
    }

    public static Packet getUsers(String username) {
        Packet p = new Packet(Type.GET_USERS);
        p.fromUsername = username;
        return p;
    }

    public static Packet markRead(String from, String to) {
        Packet p = new Packet(Type.MARK_READ);
        p.fromUsername = from;
        p.toUsername = to;
        return p;
    }

    public static Packet error(String message) {
        Packet p = new Packet(Type.ERROR);
        p.content = message;
        return p;
    }

    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }
    public String getFromUsername() { return fromUsername; }
    public void setFromUsername(String fromUsername) { this.fromUsername = fromUsername; }
    public String getToUsername() { return toUsername; }
    public void setToUsername(String toUsername) { this.toUsername = toUsername; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }

    @Override
    public String toString() {
        return "Packet{type=" + type + ", from=" + fromUsername + ", to=" + toUsername + "}";
    }
}
package g2.messagerieserver.model;

import lombok.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class Packet implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Type {
        // Client → Serveur
        LOGIN, REGISTER, LOGOUT,
        SEND_MESSAGE, GET_HISTORY, GET_USERS, MARK_READ,
        // Serveur → Client
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

    public Packet(Type type) {
        this.type = type;
        this.timestamp = LocalDateTime.now();
    }
// Factory methods pour créer des paquets spécifiques
    public static Packet login(String username, String password) {
        Packet p = new Packet(Type.LOGIN);
        p.fromUsername = username;
        p.content = password;
        return p;
    }
// Factory method pour l'inscription
    public static Packet register(String username, String password, String role) {
        Packet p = new Packet(Type.REGISTER);
        p.fromUsername = username;
        p.content = password;
        p.role = role;
        return p;
    }
// Factory method pour envoyer un message
    public static Packet sendMessage(String from, String to, String message) {
        Packet p = new Packet(Type.SEND_MESSAGE);
        p.fromUsername = from;
        p.toUsername = to;
        p.content = message;
        return p;
    }
// Factory method pour demander l'historique des messages entre deux utilisateurs
    public static Packet getHistory(String from, String to) {
        Packet p = new Packet(Type.GET_HISTORY);
        p.fromUsername = from;
        p.toUsername = to;
        return p;
    }
// Factory method pour demander la liste des utilisateurs connectés
    public static Packet getUsers(String username) {
        Packet p = new Packet(Type.GET_USERS);
        p.fromUsername = username;
        return p;
    }
// Factory method pour marquer les messages comme lus
    public static Packet markRead(String from, String to) {
        Packet p = new Packet(Type.MARK_READ);
        p.fromUsername = from;
        p.toUsername = to;
        return p;
    }
// Factory method pour les réponses de succès
    public static Packet error(String message) {
        Packet p = new Packet(Type.ERROR);
        p.content = message;
        return p;
    }
//
    @Override
    public String toString() {
        return "Packet{type=" + type + ", from=" + fromUsername + ", to=" + toUsername + "}";
    }
}
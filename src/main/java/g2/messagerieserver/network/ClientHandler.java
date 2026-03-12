package g2.messagerieserver.network;

import g2.messagerieserver.model.Message;
import g2.messagerieserver.model.Packet;
import g2.messagerieserver.model.User;
import g2.messagerieserver.service.MessageService;
import g2.messagerieserver.service.UserService;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final Server server;
    private final UserService userService;
    private final MessageService messageService;

    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String username;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
        this.userService = new UserService();
        this.messageService = new MessageService();
    }

    @Override
    public void run() {
        log("Nouveau client connecté depuis " + socket.getInetAddress());
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in  = new ObjectInputStream(socket.getInputStream());

            Packet packet;
            while ((packet = (Packet) in.readObject()) != null) {
                handlePacket(packet);
            }
        } catch (IOException e) {
            log("Connexion perdue avec " + (username != null ? username : "inconnu"));
        } catch (Exception e) {
            log("Erreur inattendue : " + e.getMessage());
        } finally {
            disconnect();
        }
    }
// Traite les différents types de paquets reçus du client et appelle les méthodes correspondantes pour chaque action (login, register, send message, etc.)
    private void handlePacket(Packet packet) throws IOException {
        log("Reçu : " + packet);
        switch (packet.getType()) {
            case LOGIN        -> handleLogin(packet);
            case REGISTER     -> handleRegister(packet);
            case LOGOUT       -> handleLogout();
            case SEND_MESSAGE -> handleSendMessage(packet);
            case GET_HISTORY  -> handleGetHistory(packet);
            case GET_USERS    -> handleGetUsers(packet);
            case MARK_READ    -> handleMarkRead(packet);
            default -> send(Packet.error("Type de paquet non reconnu."));
        }
    }
//gere la connexion d un utilisateur, vérifie que le nom n est pas déjà connecté, tente de se connecter en base, et notifie tous les autres utilisateurs du changement de statut
    private void handleLogin(Packet packet) throws IOException {

        if (server.isConnected(packet.getFromUsername())) {
            send(Packet.error("Cet utilisateur est déjà connecté. "));
            return;
        }
        try {
            User user = userService.login(packet.getFromUsername(), packet.getContent());
            this.username = user.getUsername();
            server.register(username, this);
            log("Utilisateur connecté : " + username + " [" + user.getRole() + "]");

            Packet ok = new Packet(Packet.Type.LOGIN_OK);
            ok.setFromUsername(username);
            ok.setRole(user.getRole().name());
            ok.setContent("Bienvenue " + username + " !");
            send(ok);

            broadcastStatusUpdate(username, "ONLINE");
            deliverPendingMessages();

        } catch (Exception e) {
            Packet fail = new Packet(Packet.Type.LOGIN_FAIL);
            fail.setContent(e.getMessage());
            send(fail);
        }
    }
//gere l inscription d un nouvel utilisateur
    private void handleRegister(Packet packet) throws IOException {
        try {
            User user = userService.register(
                    packet.getFromUsername(),
                    packet.getContent(),
                    packet.getRole()
            );
            Packet ok = new Packet(Packet.Type.REGISTER_OK);
            ok.setContent("Compte créé avec succès pour " + user.getUsername());
            send(ok);
            log("Inscription réussie : " + user.getUsername() + " [" + user.getRole() + "]");
        } catch (Exception e) {
            Packet fail = new Packet(Packet.Type.REGISTER_FAIL);
            fail.setContent(e.getMessage());
            send(fail);
        }
    }
    //gere la deconnexion d un utilisateur,notifie tous les autres utilisateurs du changement de statut, et ferme la connexion

    private void handleLogout() throws IOException {
        log("Déconnexion demandée par " + username);
        send(new Packet(Packet.Type.LOGOUT_OK));
        disconnect();
    }
//gere l envoi des messages entre deux utilisateurs, vérifie que les deux existent, crée le message en base, et notifie le destinataire si il est en ligne
    private void handleSendMessage(Packet packet) throws IOException {
        if (username == null) { send(Packet.error("Non authentifié.")); return; }

        try {
            User sender = userService.findByUsername(username)
                    .orElseThrow(() -> new Exception("Expéditeur introuvable."));
            User receiver = userService.findByUsername(packet.getToUsername())
                    .orElseThrow(() -> new Exception("Destinataire introuvable. (RG5)"));

            Message msg = messageService.send(sender, receiver, packet.getContent());
            log("Message de " + username + " → " + receiver.getUsername());

            ClientHandler targetHandler = server.getHandler(receiver.getUsername());
            if (targetHandler != null) {
                Packet notification = new Packet(Packet.Type.MESSAGE_RECEIVED);
                notification.setFromUsername(username);
                notification.setToUsername(receiver.getUsername());
                notification.setContent(packet.getContent());
                notification.setTimestamp(msg.getDateEnvoi());
                targetHandler.send(notification);
                messageService.markAsReceived(msg.getId());
            }

        } catch (Exception e) {
            send(Packet.error(e.getMessage()));
        }
    }
//retourne l'historique des messages entre deux utilisateurs !
    private void handleGetHistory(Packet packet) throws IOException {
        if (username == null) { send(Packet.error("Non authentifié.")); return; }

        List<Message> history = messageService.getConversation(username, packet.getToUsername());
        List<String[]> serializable = history.stream()
                .map(m -> new String[]{
                        m.getSender().getUsername(),
                        m.getReceiver().getUsername(),
                        m.getContenu(),
                        m.getDateEnvoi().format(FMT),
                        m.getStatut().name()
                })
                .collect(Collectors.toList());

        Packet response = new Packet(Packet.Type.MESSAGE_HISTORY);
        response.setData(serializable);
        send(response);
    }

//retourne la liste des utilisateurs connectés, avec leur statut et rôle. Les organisateurs voient tous les utilisateurs, les autres ne voient que les utilisateurs en ligne (RG13).
    private void handleGetUsers(Packet packet) throws IOException {
        if (username == null) { send(Packet.error("Non authentifié.")); return; }

        User requester = userService.findByUsername(username).orElse(null);
        List<User> users;

        // RG13 : ORGANISATEUR voit tous, les autres voient seulement ONLINE
        if (requester != null && requester.getRole() == User.Role.ORGANISATEUR) {
            users = userService.getAllUsers();
        } else {
            users = userService.getOnlineUsers();
        }

        List<String[]> data = users.stream()
                .map(u -> new String[]{ u.getUsername(), u.getRole().name(), u.getStatus().name() })
                .collect(Collectors.toList());

        Packet response = new Packet(Packet.Type.USER_LIST);
        response.setData(data);
        send(response);
    }
// Marque tous les messages d'une conversation comme lus lorsque le client le demande (RG7).
    private void handleMarkRead(Packet packet) {
        if (username == null) return;
        List<Message> conversation = messageService.getConversation(packet.getToUsername(), username);
        for (Message msg : conversation) {
            if (msg.getReceiver().getUsername().equals(username)
                    && msg.getStatut() != Message.Statut.LU) {
                messageService.markAsRead(msg.getId());
            }
        }
        log("Messages marqués LU par " + username + " avec " + packet.getToUsername());
    }

    private void deliverPendingMessages() {
        List<Message> pending = messageService.getPendingMessages(username);
        for (Message msg : pending) {
            try {
                Packet notification = new Packet(Packet.Type.MESSAGE_RECEIVED);
                notification.setFromUsername(msg.getSender().getUsername());
                notification.setToUsername(username);
                notification.setContent(msg.getContenu());
                notification.setTimestamp(msg.getDateEnvoi());
                send(notification);
                messageService.markAsReceived(msg.getId());
            } catch (IOException e) {
                log("Erreur livraison message en attente : " + e.getMessage());
            }
        }
        if (!pending.isEmpty())
            log(pending.size() + " message(s) en attente livré(s) à " + username);
    }
// Notifie tous les autres utilisateurs du changement de statut d'un utilisateur (ONLINE/OFFLINE) via un paquet de type STATUS_UPDATE.
    private void broadcastStatusUpdate(String user, String status) {
        Packet update = new Packet(Packet.Type.STATUS_UPDATE);
        update.setFromUsername(user);
        update.setContent(status);
        server.broadcast(update, user);
    }

    public void send(Packet packet) throws IOException {
        out.writeObject(packet);
        out.flush();
        out.reset();
    }

    private void disconnect() {
        if (username != null) {
            try { userService.logout(username); } catch (Exception ignored) {}
            server.unregister(username);
            broadcastStatusUpdate(username, "OFFLINE");
            log("Utilisateur déconnecté : " + username);
        }
        try { socket.close(); } catch (IOException ignored) {}
    }

    public String getUsername() { return username; }

    private void log(String msg) {
        System.out.printf("[%s] %s%n", LocalDateTime.now().format(FMT), msg);
    }
}
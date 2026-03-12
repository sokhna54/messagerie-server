package g2.messagerieclient.controller;
import g2.messagerieserver.model.Packet;
import g2.messagerieclient.network.ServerConnection;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;

import java.util.List;

public class ChatController {

    @FXML private ListView<String> userListView;
    @FXML private VBox messagesBox;
    @FXML private TextField messageField;
    @FXML private Label currentUserLabel;
    @FXML private Label selectedUserLabel;
    @FXML private ScrollPane scrollPane;

    private String currentUsername;
    private String currentRole;
    private String selectedUser;
    private ServerConnection connection;

    public void init(String username, String role, ServerConnection connection) {
        this.currentUsername = username;
        this.currentRole = role;
        this.connection = connection;
        currentUserLabel.setText( username + " (" + role + ")");
        connection.setListener(this::handlePacket);
        loadUsers();
    }
    private void loadUsers() {
        try {
            connection.send(Packet.getUsers(currentUsername));
        } catch (Exception e) {
            System.err.println("Erreur chargement utilisateurs : " + e.getMessage());
        }
    }

    @FXML
    public void handleRefresh() {
        loadUsers();
    }

    @FXML

    public void handleSend() {
        String content = messageField.getText().trim();
        if (content.isEmpty() || selectedUser == null) return;

        try {
            connection.send(Packet.sendMessage(currentUsername, selectedUser, content));
            addMessage(content, true, "ENVOYE"); // ← ajout ENVOYE
            messageField.clear();
        } catch (Exception e) {
            System.err.println("Erreur envoi message : " + e.getMessage());
        }
    }

    private void openConversation(String user) {
        this.selectedUser = user;
        selectedUserLabel.setText("Conversation avec : " + user);
        messagesBox.getChildren().clear();

        try {
            connection.send(Packet.getHistory(currentUsername, user));
            connection.send(Packet.markRead(currentUsername, user));
        } catch (Exception e) {
            System.err.println("Erreur ouverture conversation : " + e.getMessage());
        }
    }

    private void handlePacket(Packet packet) {
        switch (packet.getType()) {
            case USER_LIST -> updateUserList(packet);
            case MESSAGE_HISTORY -> loadHistory(packet);
            case MESSAGE_RECEIVED -> receiveMessage(packet);
            case STATUS_UPDATE -> loadUsers();
            default -> {}
        }
    }

    @SuppressWarnings("unchecked")
    private void updateUserList(Packet packet) {
        userListView.getItems().clear();
        List<String[]> users = (List<String[]>) packet.getData();
        if (users == null) return;

        for (String[] u : users) {
            String username = u[0];
            String status = u[2];
            if (!username.equals(currentUsername)) {
                String display = username + " | " + (status.equals("ONLINE") ? " En ligne" : "⚫ Hors ligne");
                userListView.getItems().add(display);
            }
        }

        userListView.setOnMouseClicked(e -> {
            String selected = userListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                String username = selected.split(" \\| ")[0].trim();
                openConversation(username);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void loadHistory(Packet packet) {
        messagesBox.getChildren().clear();
        List<String[]> history = (List<String[]>) packet.getData();
        if (history == null) return;

        for (String[] msg : history) {
            String from = msg[0];
            String content = msg[2];
            String statut = msg[4]; // ← récupère le statut
            addMessage(content, from.equals(currentUsername), statut); // ← ajout statut
        }
    }

    private void receiveMessage(Packet packet) {
        String from = packet.getFromUsername();
        if (from.equals(currentUsername)) return;

        if (selectedUser != null && from.equals(selectedUser)) {
            addMessage(packet.getContent(), false, null); // ← ajout null
        }
    }

    private void addMessage(String content, boolean isMine, String statut) {
        HBox container = new HBox();
        container.setPadding(new Insets(5, 10, 5, 10));
        container.setAlignment(isMine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        String tick = "";
        if (isMine) {
            if (statut != null && (statut.equals("RECU") || statut.equals("LU"))) {
                tick = " ✓✓"; // destinataire a reçu
            } else {
                tick = " ✓";  // envoyé mais pas encore reçu
            }
        }

        Label bubble = new Label(content + tick);
        bubble.setWrapText(true);
        bubble.setMaxWidth(300);
        bubble.setPadding(new Insets(8, 12, 8, 12));
        bubble.setStyle(isMine
                ? "-fx-background-color: #7C3AED; -fx-text-fill: white; -fx-background-radius: 15;"
                : "-fx-background-color: #E5E7EB; -fx-text-fill: black; -fx-background-radius: 15;");

        container.getChildren().add(bubble);
        messagesBox.getChildren().add(container);
        scrollPane.setVvalue(1.0);
    }
    @FXML
    public void handleLogout() {
        try {
            connection.send(new Packet(Packet.Type.LOGOUT));
            connection.disconnect();
        } catch (Exception ignored) {}

        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/g2/messagerieclient/login.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());
            javafx.stage.Stage stage = (javafx.stage.Stage) messageField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("G2 Messagerie");
        } catch (Exception e) {
            System.err.println("Erreur retour login : " + e.getMessage());
        }
    }
}
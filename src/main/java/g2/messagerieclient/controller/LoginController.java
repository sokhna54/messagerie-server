package g2.messagerieclient.controller;

import g2.messagerieclient.network.ServerConnection;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import g2.messagerieserver.model.Packet;
public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField usernameFieldReg;
    @FXML private PasswordField passwordFieldReg;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Label messageLabel;
    @FXML private TabPane tabPane;

    private final ServerConnection connection = new ServerConnection();

    @FXML
    public void initialize() {
        roleComboBox.getItems().addAll("ORGANISATEUR", "MEMBRE", "BENEVOLE");
        roleComboBox.setValue("MEMBRE");

        try {
            connection.connect(packet -> handlePacket(packet));
        } catch (Exception e) {
            messageLabel.setText("Impossible de se connecter au serveur !");
            messageLabel.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    public void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Veuillez remplir tous les champs.");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        try {
            connection.send(Packet.login(username, password));
        } catch (Exception e) {
            messageLabel.setText("Erreur de connexion au serveur.");
            messageLabel.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    public void handleRegister() {
        String username = usernameFieldReg.getText().trim();
        String password = passwordFieldReg.getText().trim();
        String role = roleComboBox.getValue();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Veuillez remplir tous les champs.");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        try {
            connection.send(Packet.register(username, password, role));
        } catch (Exception e) {
            messageLabel.setText("Erreur de connexion au serveur.");
            messageLabel.setStyle("-fx-text-fill: red;");
        }
    }

    private void handlePacket(Packet packet) {
        switch (packet.getType()) {
            case LOGIN_OK -> openChat(packet.getFromUsername(), packet.getRole());
            case LOGIN_FAIL -> {
                messageLabel.setText(packet.getContent());
                messageLabel.setStyle("-fx-text-fill: red;");
            }
            case REGISTER_OK -> {
                messageLabel.setText(packet.getContent());
                messageLabel.setStyle("-fx-text-fill: green;");
            }
            case REGISTER_FAIL -> {
                messageLabel.setText(packet.getContent());
                messageLabel.setStyle("-fx-text-fill: red;");
            }
            default -> {}
        }
    }

    private void openChat(String username, String role) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/g2/messagerieclient/chat.fxml"));
            Scene scene = new Scene(loader.load());
            ChatController chatController = loader.getController();
            chatController.init(username, role, connection);

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("G2 Messagerie — " + username);
            stage.setResizable(true);
        } catch (Exception e) {
            messageLabel.setText("Erreur ouverture chat : " + e.getMessage());
        }
    }
}
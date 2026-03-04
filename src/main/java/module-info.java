module g2.messagerieserver {
    requires javafx.controls;
    requires javafx.fxml;


    opens g2.messagerieserver to javafx.fxml;
    exports g2.messagerieserver;
}
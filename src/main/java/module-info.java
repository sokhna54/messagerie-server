module g2.messagerieserver {
    requires javafx.controls;
    requires javafx.fxml;
    requires jakarta.persistence;
    requires org.hibernate.orm.core;
    requires java.naming;
    requires java.sql;
    requires jbcrypt;
    requires static lombok;

    exports g2.messagerieclient to javafx.graphics;
    opens g2.messagerieclient to javafx.fxml;
    opens g2.messagerieclient.controller to javafx.fxml;
}
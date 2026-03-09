package g2.messagerieserver.config;

import g2.messagerieserver.model.Message;
import g2.messagerieserver.model.User;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {
    private static final SessionFactory SESSION_FACTORY;

    static {
        try {
            SESSION_FACTORY = new Configuration()
                    .configure("hibernate.cfg.xml")
                    .buildSessionFactory();
            System.out.println("[HIBERNATE] SessionFactory initialisée avec succès.");
        } catch (Exception e) {
            System.err.println("[HIBERNATE] Erreur lors de l'initialisation : " + e.getMessage());
            throw new ExceptionInInitializerError(e);
        }
    }

    public static SessionFactory getSessionFactory() {
        return SESSION_FACTORY;
    }

    public static void shutdown() {
        if (SESSION_FACTORY != null && !SESSION_FACTORY.isClosed()) {
            SESSION_FACTORY.close();
            System.out.println("[HIBERNATE] SessionFactory fermée.");
        }
    }
}
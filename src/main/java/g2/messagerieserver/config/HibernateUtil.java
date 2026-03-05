package g2.messagerieserver.config;

import g2.messagerieserver.model.Message;
import g2.messagerieserver.model.User;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {

    private static SessionFactory sessionFactory;

    static {
        try {
            sessionFactory = new Configuration()
                    .configure("hibernate.cfg.xml")
                    .addAnnotatedClass(User.class)
                    .addAnnotatedClass(Message.class)
                    .buildSessionFactory();
            System.out.println("[HIBERNATE] SessionFactory initialisée avec succès.");
        } catch (Exception e) {
            System.err.println("[HIBERNATE] Erreur : " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void shutdown() {
        if (sessionFactory != null) sessionFactory.close();
    }
}
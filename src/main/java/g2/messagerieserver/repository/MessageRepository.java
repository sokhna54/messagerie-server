package g2.messagerieserver.repository;



import g2.messagerieserver.config.HibernateUtil;
import g2.messagerieserver.model.Message;
import g2.messagerieserver.model.User;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

public class MessageRepository {

    public Message save(Message message) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(message);
            tx.commit();
            return message;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    /**
     * Récupère l'historique entre deux utilisateurs, trié par date (RG8).
     */
    public List<Message> findConversation(String username1, String username2) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Message> query = session.createQuery(
                    "FROM Message m WHERE " +
                            "(m.sender.username = :u1 AND m.receiver.username = :u2) OR " +
                            "(m.sender.username = :u2 AND m.receiver.username = :u1) " +
                            "ORDER BY m.dateEnvoi ASC", Message.class);
            query.setParameter("u1", username1);
            query.setParameter("u2", username2);
            return query.list();
        }
    }

    /**
     * Messages en attente (ENVOYE) pour un utilisateur qui se reconnecte (RG6).
     */
    public List<Message> findPendingMessages(String receiverUsername) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "FROM Message m WHERE m.receiver.username = :receiver AND m.statut = :statut ORDER BY m.dateEnvoi ASC",
                            Message.class)
                    .setParameter("receiver", receiverUsername)
                    .setParameter("statut", Message.Statut.ENVOYE)
                    .list();
        }
    }

    public void updateStatut(Long messageId, Message.Statut statut) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.createMutationQuery(
                            "UPDATE Message m SET m.statut = :statut WHERE m.id = :id")
                    .setParameter("statut", statut)
                    .setParameter("id", messageId)
                    .executeUpdate();
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }
}


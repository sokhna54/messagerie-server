package g2.messagerieserver.repository;

import g2.messagerieserver.config.HibernateUtil;
import g2.messagerieserver.model.User;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;
import java.util.Optional;

public class UserRepository {

    public Optional<User> findByUsername(String username) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            User user = session.createQuery(
                            "FROM User u WHERE u.username = :username", User.class)
                    .setParameter("username", username)
                    .uniqueResult();
            return Optional.ofNullable(user);
        }
    }

    public boolean existsByUsername(String username) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery(
                            "SELECT COUNT(u) FROM User u WHERE u.username = :username", Long.class)
                    .setParameter("username", username)
                    .uniqueResult();
            return count != null && count > 0;
        }
    }

    public User save(User user) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(user);
            tx.commit();
            return user;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    public List<User> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM User u ORDER BY u.username", User.class).list();
        }
    }

    public List<User> findOnlineUsers() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "FROM User u WHERE u.status = :status ORDER BY u.username", User.class)
                    .setParameter("status", User.Status.ONLINE)
                    .list();
        }
    }
}
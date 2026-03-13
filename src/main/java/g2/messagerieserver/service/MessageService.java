package g2.messagerieserver.service;

import g2.messagerieserver.model.Message;
import g2.messagerieserver.model.User;
import g2.messagerieserver.repository.MessageRepository;

import java.util.List;

public class MessageService {

    private final MessageRepository messageRepository = new MessageRepository();

    public Message send(User sender, User receiver, String contenu) throws Exception {
        if (contenu == null || contenu.isBlank())
            throw new Exception("Le message ne peut pas être vide. ");
        if (contenu.length() > 1000)
            throw new Exception("Le message ne doit pas dépasser 1000 caractères. ");

        Message message = new Message(sender, receiver, contenu);
        return messageRepository.save(message);
    }
    //  Récupérer l'historique entre deux utilisateurs, trié par date

    public List<Message> getConversation(String username1, String username2) {
        return messageRepository.findConversation(username1, username2);
    }

    public List<Message> getPendingMessages(String username) {
        return messageRepository.findPendingMessages(username);
    }

    public void markAsReceived(Long messageId) {
        messageRepository.updateStatut(messageId, Message.Statut.RECU);
    }

    public void markAsRead(Long messageId) {
        messageRepository.updateStatut(messageId, Message.Statut.LU);
    }
}

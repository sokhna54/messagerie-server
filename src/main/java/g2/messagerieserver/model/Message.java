package g2.messagerieserver.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Getter
@Setter
@NoArgsConstructor
public class Message {

    public enum Statut { ENVOYE, RECU, LU }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Column(nullable = false, length = 1000)
    private String contenu;

    @Column(nullable = false)
    private LocalDateTime dateEnvoi = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Statut statut = Statut.ENVOYE;

    public Message(User sender, User receiver, String contenu) {
        if (contenu == null || contenu.isBlank())
            throw new IllegalArgumentException("Le contenu ne peut pas être vide.");
        if (contenu.length() > 1000)
            throw new IllegalArgumentException("Le contenu ne doit pas dépasser 1000 caractères.");
        this.sender = sender;
        this.receiver = receiver;
        this.contenu = contenu;
        this.dateEnvoi = LocalDateTime.now();
        this.statut = Statut.ENVOYE;
    }
}

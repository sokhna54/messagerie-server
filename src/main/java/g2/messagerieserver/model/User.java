package g2.messagerieserver.model;

import jakarta.persistence.*;
import lombok.*;
import org.mindrot.jbcrypt.BCrypt;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    public enum Role { ORGANISATEUR, MEMBRE, BENEVOLE }
    public enum Status { ONLINE, OFFLINE }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
    @SequenceGenerator(name = "user_seq", sequenceName = "users_id_seq", allocationSize = 1)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.OFFLINE;

    @Column(name = "dateCreation")
    private LocalDateTime dateCreation = LocalDateTime.now();

    public User(String username, String rawPassword, Role role) {
        this.username = username;
        this.password = BCrypt.hashpw(rawPassword, BCrypt.gensalt());
        this.role = role;
        this.status = Status.OFFLINE;
        this.dateCreation = LocalDateTime.now();
    }

    public boolean checkPassword(String rawPassword) {
        return BCrypt.checkpw(rawPassword, this.password);
    }
}
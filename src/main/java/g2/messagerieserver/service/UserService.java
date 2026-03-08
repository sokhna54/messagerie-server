package g2.messagerieserver.service;

import g2.messagerieserver.model.User;
import g2.messagerieserver.repository.UserRepository;

import java.util.List;
import java.util.Optional;

public class UserService {

    private final UserRepository userRepository = new UserRepository();

    public User register(String username, String rawPassword, String roleName) throws Exception {
        if (username == null || username.isBlank())
            throw new Exception("Le nom d'utilisateur est requis.");
        if (rawPassword == null || rawPassword.isBlank())
            throw new Exception("Le mot de passe est requis.");
        if (userRepository.existsByUsername(username))
            throw new Exception("Le nom d'utilisateur '" + username + "' est déjà pris.");

        User.Role role;
        try {
            role = User.Role.valueOf(roleName);
        } catch (IllegalArgumentException e) {
            throw new Exception("Rôle invalide : " + roleName);
        }

        User user = new User(username, rawPassword, role);
        return userRepository.save(user);
    }

    public User login(String username, String rawPassword) throws Exception {
        Optional<User> opt = userRepository.findByUsername(username);
        if (opt.isEmpty()) throw new Exception("Utilisateur introuvable.");

        User user = opt.get();
        if (!user.checkPassword(rawPassword)) throw new Exception("Mot de passe incorrect.");

        user.setStatus(User.Status.ONLINE);
        return userRepository.update(user);
    }

    public void logout(String username) {
        userRepository.updateStatus(username, User.Status.OFFLINE);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getOnlineUsers() {
        return userRepository.findOnlineUsers();
    }
}
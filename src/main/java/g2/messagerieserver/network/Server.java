package g2.messagerieserver.network;

import g2.messagerieserver.config.HibernateUtil;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Serveur principal : accepte les connexions et gère les clients
public class Server {

    private static final int PORT = 9090;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Map thread-safe des clients connectés
    private final Map<String, ClientHandler> connectedClients = new ConcurrentHashMap<>();
    // Pool de threads pour chaque client
    private final ExecutorService threadPool = Executors.newCachedThreadPool();

    // Démarre le serveur et accepte les connexions
    public void start() {
        HibernateUtil.getSessionFactory();
        log("Base de données connectée.");
        log("Serveur SoZeyChat démarré sur le port " + PORT);
        log("En attente de connexions...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                log("Nouvelle connexion entrante : " + clientSocket.getInetAddress());
                ClientHandler handler = new ClientHandler(clientSocket, this);
                threadPool.execute(handler);
            }
        } catch (IOException e) {
            log("Erreur serveur : " + e.getMessage());
        } finally {
            HibernateUtil.shutdown();
            threadPool.shutdown();
        }
    }

    // Enregistre un client comme connecté
    public void register(String username, ClientHandler handler) {
        connectedClients.put(username, handler);
        log("Clients connectés : " + connectedClients.keySet());
    }

    // Enlève un client de la liste des connectés
    public void unregister(String username) {
        connectedClients.remove(username);
        log("Client déconnecté : " + username + " | Clients restants : " + connectedClients.keySet());
    }

    // Vérifie si un utilisateur est connecté
    public boolean isConnected(String username) {
        return connectedClients.containsKey(username);
    }

    // Récupère le handler d'un client connecté
    public ClientHandler getHandler(String username) {
        return connectedClients.get(username);
    }

    // Envoie un paquet à tous les clients sauf un (pour les notifications de présence)
    public void broadcast(Object packet, String excludeUsername) {
        connectedClients.forEach((username, handler) -> {
            if (!username.equals(excludeUsername)) {
                try {
                    handler.send((g2.messagerieserver.model.Packet) packet);
                } catch (IOException e) {
                    log("Erreur broadcast vers " + username);
                }
            }
        });
    }

    // Affiche un message de log avec timestamp
    private void log(String msg) {
        System.out.printf("[SERVER %s] %s%n", LocalDateTime.now().format(FMT), msg);
    }

    // Point d'entrée du serveur
    public static void main(String[] args) {
        new Server().start();
    }
}
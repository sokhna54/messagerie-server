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

public class Server {

    private static final int PORT = 9090;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Map<String, ClientHandler> connectedClients = new ConcurrentHashMap<>();
    private final ExecutorService threadPool = Executors.newCachedThreadPool();

    public void start() {
        log("Base de données connectée.");
        log("Serveur G2 démarré sur le port " + PORT);
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

    public void register(String username, ClientHandler handler) {
        connectedClients.put(username, handler);
        log("Clients connectés : " + connectedClients.keySet());
    }

    public void unregister(String username) {
        connectedClients.remove(username);
        log("Client déconnecté : " + username + " | Clients restants : " + connectedClients.keySet());
    }

    public boolean isConnected(String username) {
        return connectedClients.containsKey(username);
    }

    public ClientHandler getHandler(String username) {
        return connectedClients.get(username);
    }

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

    private void log(String msg) {
        System.out.printf("[SERVER %s] %s%n", LocalDateTime.now().format(FMT), msg);
    }

    public static void main(String[] args) {
        new Server().start();
    }
}
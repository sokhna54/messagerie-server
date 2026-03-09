package g2.messagerieclient.network;

import g2.messagerieserver.model.Packet;

import java.io.*;
import java.net.Socket;

public class ServerConnection {

    private static final String HOST = "localhost";
    private static final int PORT = 9090;

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Thread listenerThread;
    private PacketListener listener;

    public interface PacketListener {
        void onPacketReceived(Packet packet);
    }

    public void connect(PacketListener listener) throws IOException {
        this.listener = listener;
        socket = new Socket(HOST, PORT);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
        startListening();
    }

    private void startListening() {
        listenerThread = new Thread(() -> {
            try {
                Packet packet;
                while ((packet = (Packet) in.readObject()) != null) {
                    final Packet p = packet;
                    javafx.application.Platform.runLater(() -> listener.onPacketReceived(p));
                }
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Connexion perdue : " + e.getMessage());
            }
        });
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    public void send(Packet packet) throws IOException {
        out.writeObject(packet);
        out.flush();
        out.reset();
    }

    public void disconnect() {
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {}
    }

    public boolean isConnected() {
        return socket != null && !socket.isClosed();
    }
}
package server;

import shared.InputAction;
import shared.WorldSnapshot;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private final Socket socket;
    private final GameWorld world;
    private final DataInputStream in;
    private final DataOutputStream out;
    private volatile boolean disconnected = false;
    private final String clientAddress; // Храним адрес

    public ClientHandler(Socket socket, GameWorld world) throws IOException {
        this.socket = socket;
        this.world = world;
        this.clientAddress = socket.getInetAddress().toString();
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());

        world.addPlayer(this);
    }

    public String getAddress() {
        return clientAddress;
    }

    public boolean isDisconnected() {
        return disconnected;
    }

    public void readLoop() {
        try {
            while (!disconnected) {
                int code = in.readInt();
                InputAction action = InputAction.values()[code];
                world.applyInput(this, action);
            }
        } catch (IOException e) {
            close();
        }
    }

    public synchronized void sendSnapshot(WorldSnapshot snapshot) {
        if (disconnected) return;
        try {
            out.writeInt(snapshot.width());
            out.writeInt(snapshot.height());

            // Отправить символы и цвета
            for (int y = 0; y < snapshot.height(); y++) {
                for (int x = 0; x < snapshot.width(); x++) {
                    out.writeChar(snapshot.tiles()[y][x]);
                    out.writeInt(snapshot.colors()[y][x]);
                }
            }

            out.writeInt(snapshot.playerPos().x());
            out.writeInt(snapshot.playerPos().y());
            out.flush();
        } catch (IOException e) {
            close();
        }
    }

    private void close() {
        if (disconnected) return;
        disconnected = true;
        System.out.println("Client disconnected: " + clientAddress);

        world.removePlayer(this);
        try {
            socket.close();
        } catch (IOException ignored) {}
    }
}

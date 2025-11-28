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

    public ClientHandler(Socket socket, GameWorld world) throws IOException {
        this.socket = socket;
        this.world = world;
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());

        // Сразу добавляем игрока в мир при создании сессии
        world.addPlayer(this);
    }

    public boolean isDisconnected() {
        return disconnected;
    }

    public void readLoop() {
        try {
            while (!disconnected) {
                int code = in.readInt();
                InputAction action = InputAction.values()[code];

                // Теперь передаем "себя" (this) чтобы мир знал, кто двигается
                world.applyInput(this, action);
            }
        } catch (IOException e) {
            System.out.println("Client disconnected");
            close();
        }
    }

    public synchronized void sendSnapshot(WorldSnapshot snapshot) {
        if (disconnected) return;
        try {
            out.writeInt(snapshot.width());
            out.writeInt(snapshot.height());
            for (int y = 0; y < snapshot.height(); y++) {
                for (int x = 0; x < snapshot.width(); x++) {
                    out.writeChar(snapshot.tiles()[y][x]);
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
        world.removePlayer(this); // Удаляем из мира при дисконнекте
        try {
            socket.close();
        } catch (IOException ignored) {}
    }
}

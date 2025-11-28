package server;

import shared.InputAction;
import shared.WorldSnapshot;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private final GameWorld world;
    private final DataInputStream in;
    private final DataOutputStream out;

    public ClientHandler(Socket socket, GameWorld world) throws IOException {
        this.world = world;
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());
    }

    public void readLoop() {
        try {
            while (true) {
                int code = in.readInt();
                InputAction action = InputAction.values()[code];
                world.applyInput(action);
            }
        } catch (IOException e) {
            // клиент отвалился — пока просто выходим
        }
    }

    public synchronized void sendSnapshot(WorldSnapshot snapshot) {
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
            // ошибка записи — можно завершать игру
        }
    }
}


package client;

import shared.InputAction;
import shared.Position;
import shared.WorldSnapshot;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientConnection {
    private final Socket socket;
    private final DataInputStream inputStream;
    private final DataOutputStream outputStream;
    private final ClientGameState state;

    public ClientConnection(String host, int port, ClientGameState state) throws IOException {
        this.state = state;
        this.socket = new Socket(host, port);
        this.inputStream = new DataInputStream(socket.getInputStream());
        this.outputStream = new DataOutputStream(socket.getOutputStream());
    }

    public void start() {
        new Thread(this::readLoop, "client-read").start();
    }

    private void readLoop() {
        try {
            while (true) {
                int w = inputStream.readInt();
                int h = inputStream.readInt();
                char[][] tiles = new char[h][w];
                for (int y = 0; y < h; y++) {
                    for (int x = 0; x < w; x++) {
                        tiles[y][x] = inputStream.readChar();
                    }
                }
                int px = inputStream.readInt();
                int py = inputStream.readInt();
                state.snapshot = new WorldSnapshot(w, h, tiles, new Position(px, py));
            }
        } catch (IOException e) {
            // сервер отвалился
        }
    }

    public synchronized void sendAction(InputAction action) {
        try {
            outputStream.writeInt(action.ordinal());
            outputStream.flush();
        } catch (IOException e) {
            // соединение упало
        }
    }
}


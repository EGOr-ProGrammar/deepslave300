package client;

import shared.InputAction;
import shared.WorldSnapshot;

import java.io.*;
import java.net.Socket;

public class ClientConnection {
    private final Socket socket;
    private final ObjectInputStream inputStream;  // ← изменено
    private final DataOutputStream outputStream;
    private final ClientGameState state;

    public ClientConnection(String host, int port, ClientGameState state) throws IOException {
        this.state = state;
        this.socket = new Socket(host, port);

        // Нужно ObjectInputStream создать после того, как сервер отправил заголовок
        this.inputStream = new ObjectInputStream(socket.getInputStream());
        this.outputStream = new DataOutputStream(socket.getOutputStream());
    }

    public void start() {
        new Thread(this::readLoop, "client-read").start();
    }

    private void readLoop() {
        try {
            while (true) {
                WorldSnapshot snapshot = (WorldSnapshot) inputStream.readObject();
                state.snapshot = snapshot;  // ← получаем ВСЕ поля
            }
        } catch (Exception e) {
            System.err.println("Connection lost: " + e.getMessage());
        }
    }

    public synchronized void sendAction(InputAction action) {
        try {
            outputStream.writeInt(action.ordinal());
            outputStream.flush();
        } catch (IOException e) {
            System.err.println("Failed to send action: " + e.getMessage());
        }
    }
}

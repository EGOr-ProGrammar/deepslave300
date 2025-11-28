package client;

import client.read.InputReader;
import shared.WorldSnapshot;

public class ClientMain {
    public static void main(String[] args) throws Exception {
        ClientGameState state = new ClientGameState();
        ClientConnection connection = new ClientConnection("localhost", 4000, state);
        connection.start();
        InputReader.start(connection);

        // TODO: сделать что-то с Exception
        while (true) {
            WorldSnapshot snapshot = state.snapshot;
            if (snapshot != null) {
                AnsiRenderer.render(snapshot);
            }
            Thread.sleep(100); // 10 FPS рендера
        }
    }
}

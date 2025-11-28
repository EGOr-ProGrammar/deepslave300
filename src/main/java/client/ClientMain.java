package client;

import client.keyboard_input.InputManager;
import shared.WorldSnapshot;

public class ClientMain {
    public static void main(String[] args) throws Exception {
        AnsiRenderer.init();

        try {
            ClientGameState state = new ClientGameState();
            ClientConnection connection = new ClientConnection("localhost", 4000, state);
            connection.start();

            InputManager inputManager = new InputManager(connection);
            inputManager.start();

            while (true) {
                WorldSnapshot snapshot = state.snapshot;
                if (snapshot != null) {
                    AnsiRenderer.render(snapshot);
                }
                Thread.sleep(50);
            }
        } finally {
            AnsiRenderer.cleanup();
        }
    }
}

package client.read;

import client.ClientConnection;
import shared.InputAction;

import java.io.IOException;

public class InputReader {

    public static void start(ClientConnection connection) {
        new Thread(() -> {
            try {
                while (true) {
                    int ch = InstantKeyReader.readKey();
                    if (ch == -1) break;

                    InputAction action = switch (ch) {
                        case 'w', 'W' -> InputAction.MOVE_UP;
                        case 's', 'S' -> InputAction.MOVE_DOWN;
                        case 'a', 'A' -> InputAction.MOVE_LEFT;
                        case 'd', 'D' -> InputAction.MOVE_RIGHT;
                        // стрелки как ESC [ A/B/C/D
                        case 27 -> {
                            int bracket = System.in.read();
                            int code = System.in.read();
                            yield switch (code) {
                                case 'A' -> InputAction.MOVE_UP;
                                case 'B' -> InputAction.MOVE_DOWN;
                                case 'C' -> InputAction.MOVE_RIGHT;
                                case 'D' -> InputAction.MOVE_LEFT;
                                default -> null;
                            };
                        }
                        default -> null;
                    };

                    if (action != null) {
                        connection.sendAction(action);
                    }
                }
            } catch (IOException ignored) {
            } finally {
                try {
                    InstantKeyReader.disableRawMode();
                } catch (IOException ignored) {}
            }
        }, "input").start();
    }
}

package client.keyboard_input;

import client.ClientConnection;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.NonBlockingReader;

import java.io.IOException;

public class InputManager {
    private final ClientConnection connection;
    private final Terminal terminal;
    private InputState currentState;
    private volatile boolean running = true;

    public InputManager(ClientConnection connection) throws IOException {
        this.connection = connection;

        this.terminal = TerminalBuilder.builder()
                .system(true)
                .jna(true)
                .build();

        // Ввод не ждет Enter
        this.terminal.enterRawMode();
    }

    public void setState(InputState newState) {
        this.currentState = newState;
    }

    public void start() {
        new Thread(this::runLoop, "input-loop").start();
    }

    private void runLoop() {
        setState(new GameplayState());

        NonBlockingReader reader = terminal.reader();
        try {
            while (running) {
                // 3. Читаем символ мгновенно. Блокируется, пока игрок не нажмет кнопку.
                int key = reader.read();

                if (key == -1) break; // EOF
                if (currentState != null) {
                    currentState.handleInput(key, connection, this);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                // Вернуть терминал в обычный режим при выходе
                terminal.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        running = false;
    }
}

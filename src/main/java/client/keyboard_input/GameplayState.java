package client.keyboard_input;

import client.AnsiRenderer;
import client.ClientConnection;
import shared.InputAction;

public class GameplayState implements InputState {

    @Override
    public void handleInput(int key, ClientConnection connection, InputManager context) {
        InputAction action = null;
        char c = (char) key;

        // Ctrl+L (код 12) - принудительная перерисовка
        if (key == 12) {
            AnsiRenderer.forceRedraw();
            return;
        }

        switch (Character.toLowerCase(c)) {
            case 'w': action = InputAction.MOVE_UP; break;
            case 's': action = InputAction.MOVE_DOWN; break;
            case 'a': action = InputAction.MOVE_LEFT; break;
            case 'd': action = InputAction.MOVE_RIGHT; break;
            case 'q':
                System.exit(0);
                break;
            // TODO: добавить case инвентаря
            // case 'i': context.setState(new InventoryState()); break;
        }

        if (action != null) {
            connection.sendAction(action);
        }
    }
}

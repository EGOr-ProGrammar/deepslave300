package client.keyboard_input;

import client.ClientConnection;

/**
 * Состояние ввода для одной или нескольких клавиш.
 */
public interface InputState {
    /**
     * Обрабатывает нажатие одной клавиши.
     * @param key код клавиши из terminal.reader().read()
     * @param connection соединение для отправки команд
     * @param context контекст для смены состояния (например, открыть инвентарь)
     */
    void handleInput(int key, ClientConnection connection, InputManager context);
}

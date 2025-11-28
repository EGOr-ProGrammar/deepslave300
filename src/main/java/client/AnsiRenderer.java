package client;

import shared.WorldSnapshot;

public class AnsiRenderer {
    private static final String CLEAR_SCREEN = "\033[H\033[2J"; // Очистка всего экрана
    private static final String RESET = "\033[0m";

    public static void render(WorldSnapshot snapshot) {
        StringBuilder sb = new StringBuilder();

        // Перемещаем курсор в начало
        // Вместо полной очистки для плавности
        sb.append("\033[H");

        for (int y = 0; y < snapshot.height(); y++) {
            for (int x = 0; x < snapshot.width(); x++) {
                char symbol = snapshot.tiles()[y][x];
                int colorCode = snapshot.colors()[y][x];

                // Формировка ANSI последовательность цвета: \033[XXm
                sb.append("\033[").append(colorCode).append("m");
                sb.append(symbol);
                sb.append(RESET); // Сброс цвета после каждого символа
            }
            sb.append("\n");
        }

        sb.append("Pos: ").append(snapshot.playerPos().x())
                .append(":").append(snapshot.playerPos().y())
                .append("   "); // Затирка старого текста

        System.out.print(sb.toString());
    }
}

package client;

import shared.WorldSnapshot;

public class AnsiRenderer {
    // ANSI коды управления экраном
    private static final String ALT_BUFFER_ON = "\033[?1049h";  // Включить альтернативный буфер
    private static final String ALT_BUFFER_OFF = "\033[?1049l"; // Выключить
    private static final String HIDE_CURSOR = "\033[?25l";      // Скрыть мигающий курсор
    private static final String SHOW_CURSOR = "\033[?25h";      // Показать курсор
    private static final String CLEAR_SCREEN = "\033[2J";       // Очистить экран
    private static final String MOVE_TO_START = "\033[H";       // Курсор в 0,0
    private static final String RESET = "\033[0m";

    public static void init() {
        System.out.print(ALT_BUFFER_ON);
        System.out.print(HIDE_CURSOR);
        System.out.print(CLEAR_SCREEN);
        System.out.flush();
    }

    public static void cleanup() {
        System.out.print(ALT_BUFFER_OFF);
        System.out.print(SHOW_CURSOR);
        System.out.flush();
    }

    public static void render(WorldSnapshot snapshot) {
        StringBuilder sb = new StringBuilder();

        // Всегда начинать рендер с верхнего левого угла
        sb.append(MOVE_TO_START);

        for (int y = 0; y < snapshot.height(); y++) {
            for (int x = 0; x < snapshot.width(); x++) {
                char symbol = snapshot.tiles()[y][x];
                // Проверка на null/пустой массив цветов для совместимости, если сервер не прислал цвета
                int colorCode = (snapshot.colors() != null) ? snapshot.colors()[y][x] : 37;

                sb.append("\033[").append(colorCode).append("m");
                sb.append(symbol);
                sb.append(RESET);
            }
            sb.append("\n");
        }

        // Инфо-строка
        sb.append("Pos: ").append(snapshot.playerPos().x())
                .append(":").append(snapshot.playerPos().y())
                .append("   ");

        System.out.print(sb.toString());
    }
}

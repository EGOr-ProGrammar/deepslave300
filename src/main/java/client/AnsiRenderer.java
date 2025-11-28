package client;

import shared.WorldSnapshot;

import java.util.Arrays;

public class AnsiRenderer {
    // ANSI коды управления
    private static final String ALT_BUFFER_ON = "\033[?1049h";
    private static final String ALT_BUFFER_OFF = "\033[?1049l";
    private static final String HIDE_CURSOR = "\033[?25l";
    private static final String SHOW_CURSOR = "\033[?25h";
    private static final String CLEAR_SCREEN = "\033[2J";
    private static final String RESET = "\033[0m";

    // Буфер предыдущего кадра для оптимизации (diff rendering)
    private static char[][] lastTiles = null;
    private static int[][] lastColors = null;
    private static int lastWidth = -1;
    private static int lastHeight = -1;
    private static String lastInfoString = "";

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
        if (snapshot == null) return;

        int w = snapshot.width();
        int h = snapshot.height();
        char[][] currentTiles = snapshot.tiles();
        int[][] currentColors = snapshot.colors();

        boolean fullRedraw = false;
        if (w != lastWidth || h != lastHeight) {
            lastWidth = w;
            lastHeight = h;
            lastTiles = new char[h][w];
            lastColors = new int[h][w];
            for (char[] row : lastTiles) Arrays.fill(row, '\0');

            System.out.print(CLEAR_SCREEN);
            fullRedraw = true;
        }

        StringBuilder sb = new StringBuilder();

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                char newChar = currentTiles[y][x];
                int newColor = (currentColors != null) ? currentColors[y][x] : 37;

                // Оптимизация: рисуем только изменения
                if (fullRedraw || newChar != lastTiles[y][x] || newColor != lastColors[y][x]) {
                    sb.append("\033[").append(y + 1).append(";").append(x + 1).append("H");

                    // TODO: вынести цвет для игрока в конфиг, создать класс для окраски
                    sb.append("\033[").append(newColor).append("m");
                    sb.append(newChar);

                    lastTiles[y][x] = newChar;
                    lastColors[y][x] = newColor;
                }
            }
        }
        sb.append(RESET);

        String infoString = "Pos: " + snapshot.playerPos().x() + ":" + snapshot.playerPos().y();
        if (!infoString.equals(lastInfoString) || fullRedraw) {
            sb.append("\033[").append(h + 1).append(";1H");
            sb.append("\033[K");
            sb.append(infoString);
            lastInfoString = infoString;
        }

        if (!sb.isEmpty()) {
            System.out.print(sb);
            System.out.flush();
        }
    }

    public static void forceRedraw() {
        // Это заставит render() подумать, что размер изменился, и перерисовать всё с нуля
        lastWidth = -1;
    }
}

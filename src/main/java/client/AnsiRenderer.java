package client;

import shared.WorldSnapshot;
import shared.NpcSnapshot;
import shared.LootSnapshot;
import shared.Position;

import java.util.Arrays;

public class AnsiRenderer {

    // ANSI коды управления
    private static final String ESC = "\u001B";
    private static final String ALT_BUFFER_ON  = ESC + "[?1049h";
    private static final String ALT_BUFFER_OFF = ESC + "[?1049l";
    private static final String HIDE_CURSOR    = ESC + "[?25l";
    private static final String SHOW_CURSOR    = ESC + "[?25h";
    private static final String CLEAR_SCREEN   = ESC + "[2J";
    private static final String RESET          = ESC + "[0m";

    // Буфер предыдущего кадра для оптимизации (diff rendering)
    private static char[][] lastTiles  = null;
    private static int[][]  lastColors = null;
    private static int lastWidth  = -1;
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
        if (w != lastWidth || h != lastHeight || lastTiles == null || lastColors == null) {
            lastWidth = w;
            lastHeight = h;
            lastTiles = new char[h][w];
            lastColors = new int[h][w];
            for (char[] row : lastTiles) {
                Arrays.fill(row, '\0');
            }
            for (int[] row : lastColors) {
                Arrays.fill(row, -1);
            }
            System.out.print(CLEAR_SCREEN);
            fullRedraw = true;
        }

        // Рабочие буферы для наложения мобов и лута
        char[][] renderTiles = new char[h][w];
        int[][] renderColors = new int[h][w];
        for (int y = 0; y < h; y++) {
            System.arraycopy(currentTiles[y], 0, renderTiles[y], 0, w);
            System.arraycopy(currentColors[y], 0, renderColors[y], 0, w);
        }

        // Накладываем лут
        if (snapshot.loot() != null) {
            for (LootSnapshot loot : snapshot.loot()) {
                int lx = loot.x();
                int ly = loot.y();
                if (lx >= 0 && lx < w && ly >= 0 && ly < h) {
                    char current = renderTiles[ly][lx];
                    // Не перезаписываем игроков и мобов
                    if (current == '.' || current == ' ' ) {
                        renderTiles[ly][lx] = loot.symbol();
                        renderColors[ly][lx] = loot.getColorCode();
                    }
                }
            }
        }

        // Накладываем мобов (приоритет выше лута, но ниже игроков)
        if (snapshot.npcs() != null) {
            for (NpcSnapshot npc : snapshot.npcs()) {
                int nx = npc.x();
                int ny = npc.y();
                if (nx >= 0 && nx < w && ny >= 0 && ny < h) {
                    char current = renderTiles[ny][nx];
                    // Не перезаписываем игроков
                    if (current != '@' && current != 'P' && current != '\\') {
                        renderTiles[ny][nx] = npc.symbol();
                        renderColors[ny][nx] = npc.getColorCode();
                    }
                }
            }
        }

        // Рендер с diff‑оптимизацией
        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                char newChar = renderTiles[y][x];
                int newColor = renderColors[y][x];

                if (fullRedraw || newChar != lastTiles[y][x] || newColor != lastColors[y][x]) {
                    sb.append(ESC).append("[")
                            .append(y + 1).append(";").append(x + 1).append("H");
                    sb.append(ESC).append("[")
                            .append(newColor).append("m");
                    sb.append(newChar);

                    lastTiles[y][x] = newChar;
                    lastColors[y][x] = newColor;
                }
            }
        }
        sb.append(RESET);

        // HUD с позицией, HP и золотом
        String infoString = buildHUD(snapshot);
        if (!infoString.equals(lastInfoString) || fullRedraw) {
            int hudRow = h + 1;
            sb.append(ESC).append("[")
                    .append(hudRow).append(";1H");
            sb.append(ESC).append("[K"); // очистить строку
            sb.append(RESET);
            sb.append(infoString);
            lastInfoString = infoString;
        }

        System.out.print(sb);
        System.out.flush();
    }

    private static String buildHUD(WorldSnapshot snapshot) {
        Position p = snapshot.playerPos();
        int x = p != null ? p.x() : -1;
        int y = p != null ? p.y() : -1;
        int hp = snapshot.playerHp();
        int maxHp = snapshot.playerMaxHp();
        int gold = snapshot.playerGold();

        return String.format("Pos %d,%d  HP %d/%d  Gold %d", x, y, hp, maxHp, gold);
    }

    public static void forceRedraw() {
        lastWidth = -1;
        lastHeight = -1;
        lastTiles = null;
        lastColors = null;
        lastInfoString = "";
    }
}

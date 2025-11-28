package client;

import shared.WorldSnapshot;

public class AnsiRenderer {
    public static void clear() {
        System.out.print("\u001b[2J");      // очистить экран
        System.out.print("\u001b[H");       // курсор в 0,0
    }

    public static void render(WorldSnapshot snapshot) {
        clear();
        char[][] tiles = snapshot.tiles();
        for (int y = 0; y < snapshot.height(); y++) {
            StringBuilder line = new StringBuilder();
            for (int x = 0; x < snapshot.width(); x++) {
                if (x == snapshot.playerPos().x() && y == snapshot.playerPos().y()) {
                    // TODO: вынести цвет для игрока в конфиг, создать класс для окраски
                    line.append("\u001b[32m@\u001b[0m");
                } else {
                    line.append(tiles[y][x]);
                }
            }
            System.out.println(line);
        }
    }
}


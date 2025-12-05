package server.generate_map;

import server.model.TileType;
import shared.Position;

import java.util.Random;

public class DungeonMap {
    // Размеры карты без стен
    public static final int WIDTH = 120;
    public static final int HEIGHT = 90;
    private final TileType[][] tiles;
    private final long seed;

    public DungeonMap(int idX, int idY, long seed) {
        this.seed = seed;
        this.tiles = new TileType[HEIGHT][WIDTH];
        generate();
    }

    private void generate() {
        // Используем seed для детерминированной генерации
        Random rand = new Random(seed);

        // Шум
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                tiles[y][x] = (rand.nextDouble() < 0.45) ? TileType.WALL : TileType.FLOOR;
            }
        }

        // Сглаживание
        for (int i = 0; i < 5; i++) smoothMap();

        // Гарантированные стены по периметру
        for (int y = 0; y < HEIGHT; y++) {
            tiles[y][0] = TileType.WALL;
            tiles[y][WIDTH - 1] = TileType.WALL;
        }
        for (int x = 0; x < WIDTH; x++) {
            tiles[0][x] = TileType.WALL;
            tiles[HEIGHT - 1][x] = TileType.WALL;
        }

        // Ворота
        createGates();
    }

    private void createGates() {
        int cx = WIDTH / 2;
        int cy = HEIGHT / 2;

        // Безопасное создание проходов
        for (int i = -2; i <= 2; i++) {
            if (cx + i >= 0 && cx + i < WIDTH) tiles[0][cx + i] = TileType.FLOOR; // Север
            if (cx + i >= 0 && cx + i < WIDTH) tiles[HEIGHT - 1][cx + i] = TileType.FLOOR; // Юг
            if (cy + i >= 0 && cy + i < HEIGHT) tiles[cy + i][0] = TileType.FLOOR; // Запад
            if (cy + i >= 0 && cy + i < HEIGHT) tiles[cy + i][WIDTH - 1] = TileType.FLOOR; // Восток
        }
    }

    private void smoothMap() {
        TileType[][] nextTiles = new TileType[HEIGHT][WIDTH];
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                int walls = countWalls(x, y);
                if (walls > 4) nextTiles[y][x] = TileType.WALL;
                else if (walls < 4) nextTiles[y][x] = TileType.FLOOR;
                else nextTiles[y][x] = tiles[y][x];
            }
        }
        for (int i = 0; i < HEIGHT; i++) System.arraycopy(nextTiles[i], 0, tiles[i], 0, WIDTH);
    }

    private int countWalls(int x, int y) {
        int count = 0;
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (dx == 0 && dy == 0) continue;
                int nx = x + dx, ny = y + dy;
                if (nx < 0 || nx >= WIDTH || ny < 0 || ny >= HEIGHT || tiles[ny][nx] == TileType.WALL) {
                    count++;
                }
            }
        }
        return count;
    }

    public TileType getTile(int x, int y) {
        if (x < 0 || x >= WIDTH || y < 0 || y >= HEIGHT) return TileType.WALL;
        return tiles[y][x];
    }

    public Position getRandomFloorPosition() {
        Random rand = new Random();
        for (int i = 0; i < 1000; i++) { // Защита от бесконечного цикла
            int x = rand.nextInt(WIDTH - 2) + 1;
            int y = rand.nextInt(HEIGHT - 2) + 1;
            if (tiles[y][x] == TileType.FLOOR) return new Position(x, y);
        }
        return new Position(WIDTH/2, HEIGHT/2); // Fallback
    }

    public long getSeed() {
        return seed;
    }
}

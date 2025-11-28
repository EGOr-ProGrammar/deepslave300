package server.generate_map;

import server.model.TileType;
import shared.Position;

import java.util.Random;

public class DungeonMap {
    public static final int WIDTH = 120;
    public static final int HEIGHT = 90;
    private final TileType[][] tiles;
    private final int mapIdX, mapIdY; // Координаты этой карты в сетке уровня

    public DungeonMap(int idX, int idY) {
        this.mapIdX = idX;
        this.mapIdY = idY;
        this.tiles = new TileType[HEIGHT][WIDTH];
        generate();
    }

    // Генерация "пещеры" (Cellular Automata)
    private void generate() {
        Random rand = new Random();
        // 1. Шум
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                if (rand.nextDouble() < 0.45) {
                    tiles[y][x] = TileType.WALL;
                } else {
                    tiles[y][x] = TileType.FLOOR;
                }
            }
        }

        // 2. Сглаживание (4-5 итераций)
        for (int i = 0; i < 5; i++) {
            smoothMap();
        }

        // 3. Гарантируем стены по краям (но оставляем проходы!)
        for (int y = 0; y < HEIGHT; y++) {
            tiles[y][0] = TileType.WALL;
            tiles[y][WIDTH - 1] = TileType.WALL;
        }
        for (int x = 0; x < WIDTH; x++) {
            tiles[0][x] = TileType.WALL;
            tiles[HEIGHT - 1][x] = TileType.WALL;
        }

        // 4. Создаем "Ворота" (проходы к соседям)
        // Проходы всегда по центру стен, чтобы стыковаться
        createGates();
    }

    private void smoothMap() {
        TileType[][] nextTiles = new TileType[HEIGHT][WIDTH];
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                int wallCount = countWallNeighbors(x, y);
                if (wallCount > 4) nextTiles[y][x] = TileType.WALL;
                else if (wallCount < 4) nextTiles[y][x] = TileType.FLOOR;
                else nextTiles[y][x] = tiles[y][x];
            }
        }
        for (int y = 0; y < HEIGHT; y++) {
            System.arraycopy(nextTiles[y], 0, tiles[y], 0, WIDTH);
        }
    }

    private int countWallNeighbors(int gridX, int gridY) {
        int count = 0;
        for (int neighborY = gridY - 1; neighborY <= gridY + 1; neighborY++) {
            for (int neighborX = gridX - 1; neighborX <= gridX + 1; neighborX++) {
                if (neighborX >= 0 && neighborX < WIDTH && neighborY >= 0 && neighborY < HEIGHT) {
                    if (neighborX != gridX || neighborY != gridY) {
                        if (tiles[neighborY][neighborX] == TileType.WALL) {
                            count++;
                        }
                    }
                } else {
                    count++; // Границы считаем стенами
                }
            }
        }
        return count;
    }

    private void createGates() {
        // Пробиваем дырки по центрам сторон (шириной 4 тайла)
        // СЕВЕР (y=0)
        for (int x = WIDTH / 2 - 2; x <= WIDTH / 2 + 2; x++) tiles[0][x] = TileType.FLOOR;
        // ЮГ (y=HEIGHT-1)
        for (int x = WIDTH / 2 - 2; x <= WIDTH / 2 + 2; x++) tiles[HEIGHT - 1][x] = TileType.FLOOR;
        // ЗАПАД (x=0)
        for (int y = HEIGHT / 2 - 2; y <= HEIGHT / 2 + 2; y++) tiles[y][0] = TileType.FLOOR;
        // ВОСТОК (x=WIDTH-1)
        for (int y = HEIGHT / 2 - 2; y <= HEIGHT / 2 + 2; y++) tiles[y][WIDTH - 1] = TileType.FLOOR;
    }

    public TileType getTile(int x, int y) {
        if (x < 0 || x >= WIDTH || y < 0 || y >= HEIGHT) return TileType.WALL;
        return tiles[y][x];
    }

    public void setTile(int x, int y, TileType type) {
        if (x >= 0 && x < WIDTH && y >= 0 && y < HEIGHT) {
            tiles[y][x] = type;
        }
    }

    public Position getRandomFloorPosition() {
        Random rand = new Random();
        int x, y;
        do {
            x = rand.nextInt(WIDTH - 2) + 1;
            y = rand.nextInt(HEIGHT - 2) + 1;
        } while (tiles[y][x] != TileType.FLOOR);
        return new Position(x, y);
    }
}

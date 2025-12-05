package server.generate_map;

import server.model.TileType;
import shared.Position;

import java.util.Random;

public class DungeonMap {
    public static final int WIDTH = 80;
    public static final int HEIGHT = 30;

    private final TileType[][] tiles;
    private final long seed;

    // Новые поля для хранения информации о соседях
    private final boolean hasNorth;
    private final boolean hasSouth;
    private final boolean hasWest;
    private final boolean hasEast;

    public DungeonMap(int idX, int idY, long seed, boolean hasNorth, boolean hasSouth, boolean hasWest, boolean hasEast) {
        this.seed = seed;
        this.hasNorth = hasNorth;
        this.hasSouth = hasSouth;
        this.hasWest = hasWest;
        this.hasEast = hasEast;
        this.tiles = new TileType[HEIGHT][WIDTH];
        generate();
    }

    private void generate() {
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

        createGates();
        createGateCorridors();
    }

    private void createGates() {
        int cx = WIDTH / 2;  // 40
        int cy = HEIGHT / 2; // 15

        // Проходы шириной 5 клеток только там, где есть соседи
        for (int i = -2; i <= 2; i++) {
            // Север (верх, y=0)
            if (hasNorth && cx + i >= 0 && cx + i < WIDTH) {
                tiles[0][cx + i] = TileType.FLOOR;
            }

            // Юг (низ, y=HEIGHT-1)
            if (hasSouth && cx + i >= 0 && cx + i < WIDTH) {
                tiles[HEIGHT - 1][cx + i] = TileType.FLOOR;
            }

            // Запад (лево, x=0)
            if (hasWest && cy + i >= 0 && cy + i < HEIGHT) {
                tiles[cy + i][0] = TileType.FLOOR;
            }

            // Восток (право, x=WIDTH-1)
            if (hasEast && cy + i >= 0 && cy + i < HEIGHT) {
                tiles[cy + i][WIDTH - 1] = TileType.FLOOR;
            }
        }
    }

    private void createGateCorridors() {
        int cx = WIDTH / 2;
        int cy = HEIGHT / 2;
        int corridorDepth = 5; // Глубина коридора от прохода

        // Север (вход сверху, y=0, коридор идёт вниз)
        if (hasNorth) {
            for (int i = -2; i <= 2; i++) {
                int gateX = cx + i;
                if (gateX >= 0 && gateX < WIDTH) {
                    for (int y = 0; y < corridorDepth && y < HEIGHT; y++) {
                        tiles[y][gateX] = TileType.FLOOR;
                    }
                }
            }
        }

        // Юг (вход снизу, y=HEIGHT-1, коридор идёт вверх)
        if (hasSouth) {
            for (int i = -2; i <= 2; i++) {
                int gateX = cx + i;
                if (gateX >= 0 && gateX < WIDTH) {
                    for (int y = HEIGHT - 1; y >= HEIGHT - corridorDepth && y >= 0; y--) {
                        tiles[y][gateX] = TileType.FLOOR;
                    }
                }
            }
        }

        // Запад (вход слева, x=0, коридор идёт вправо)
        if (hasWest) {
            for (int i = -2; i <= 2; i++) {
                int gateY = cy + i;
                if (gateY >= 0 && gateY < HEIGHT) {
                    for (int x = 0; x < corridorDepth && x < WIDTH; x++) {
                        tiles[gateY][x] = TileType.FLOOR;
                    }
                }
            }
        }

        // Восток (вход справа, x=WIDTH-1, коридор идёт влево)
        if (hasEast) {
            for (int i = -2; i <= 2; i++) {
                int gateY = cy + i;
                if (gateY >= 0 && gateY < HEIGHT) {
                    for (int x = WIDTH - 1; x >= WIDTH - corridorDepth && x >= 0; x--) {
                        tiles[gateY][x] = TileType.FLOOR;
                    }
                }
            }
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
        for (int i = 0; i < 1000; i++) {
            int x = rand.nextInt(WIDTH - 2) + 1;
            int y = rand.nextInt(HEIGHT - 2) + 1;
            if (tiles[y][x] == TileType.FLOOR) return new Position(x, y);
        }
        return new Position(WIDTH/2, HEIGHT/2);
    }

    public long getSeed() {
        return seed;
    }
}

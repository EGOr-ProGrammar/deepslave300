package server;

import shared.InputAction;
import shared.Position;
import shared.WorldSnapshot;

public class GameWorld {
    private final int width = 40;
    private final int height = 20;
    private final TileType[][] tiles = new TileType[height][width];
    private Position playerPos = new Position(2, 2);

    public GameWorld() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                boolean border = x == 0 || y == 0 || x == width - 1 || y == height - 1;
                tiles[y][x] = border ? TileType.WALL : TileType.FLOOR;
            }
        }
    }

    public synchronized void applyInput(InputAction action) {
        int nx = playerPos.x();
        int ny = playerPos.y();

        switch (action) {
            case MOVE_UP -> ny--;
            case MOVE_DOWN -> ny++;
            case MOVE_LEFT -> nx--;
            case MOVE_RIGHT -> nx++;
        }

        if (nx >= 0 && nx < width && ny >= 0 && ny < height &&
                tiles[ny][nx] == TileType.FLOOR) {
            playerPos = new Position(nx, ny);
        }
    }

    public synchronized WorldSnapshot snapshot() {
        char[][] chars = new char[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                chars[y][x] = tiles[y][x].symbol;
            }
        }
        return new WorldSnapshot(width, height, chars, playerPos);
    }
}


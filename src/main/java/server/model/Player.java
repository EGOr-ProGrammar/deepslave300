package server.model;

import shared.Position;
import java.util.Random;

public class Player {
    private Position position;
    private final int colorCode;

    private int currentLevel = 1;
    // Координата X карты в сетке уровня
    private int currentMapGridX = 0;
    // Координата Y карты в сетке уровня
    private int currentMapGridY = 0;

    private static final int[] ALLOWED_COLORS = {
            31, 33, 34, 35, 36, 91, 93, 94, 95, 96
    };

    public Player(Position startPos) {
        this.position = startPos;
        this.colorCode = ALLOWED_COLORS[new Random().nextInt(ALLOWED_COLORS.length)];
    }

    public int getCurrentLevel() { return currentLevel; }
    public int getMapX() { return currentMapGridX; }
    public int getMapY() { return currentMapGridY; }

    public void setLevel(int level) { this.currentLevel = level; }
    public void setMapGrid(int x, int y) { this.currentMapGridX = x; this.currentMapGridY = y; }

    public Position getPosition() { return position; }
    public void setPosition(Position position) { this.position = position; }
    public int getColorCode() { return colorCode; }
}

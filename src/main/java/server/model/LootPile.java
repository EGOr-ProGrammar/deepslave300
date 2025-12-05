package server.model;

import shared.Position;

import java.util.UUID;

public class LootPile {
    private final UUID id;
    private Position position;
    private final int goldAmount;
    private int level;
    private int mapX, mapY;

    public LootPile(Position position, int goldAmount) {
        this.id = UUID.randomUUID();
        this.position = position;
        this.goldAmount = goldAmount;
    }

    public UUID getId() { return id; }
    public Position getPosition() { return position; }
    public int getGoldAmount() { return goldAmount; }
    public int getLevel() { return level; }
    public int getMapX() { return mapX; }
    public int getMapY() { return mapY; }

    public void setLevel(int level) { this.level = level; }
    public void setMapGrid(int x, int y) { this.mapX = x; this.mapY = y; }
}

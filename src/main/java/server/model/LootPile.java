package server.model;

import shared.Position;

public class LootPile {
    private final Position position;
    private final int goldAmount;
    private int level;
    private int mapX;
    private int mapY;

    public LootPile(Position position, int goldAmount) {
        this.position = position;
        this.goldAmount = goldAmount;
    }

    public boolean canBePickedBy(Player player) {
        return player.getCurrentLevel() == this.level &&
                player.getMapX() == this.mapX &&
                player.getMapY() == this.mapY &&
                player.getPosition().equals(this.position);
    }

    /**
     * Применяет эффект лута к игроку
     */
    public void applyTo(Player player) {
        player.addGold(this.goldAmount);
        System.out.println("Player picked up " + this.goldAmount + " gold");
    }

    public Position getPosition() { return position; }
    public int getGoldAmount() { return goldAmount; }
    public int getLevel() { return level; }
    public int getMapX() { return mapX; }
    public int getMapY() { return mapY; }

    public void setLevel(int level) { this.level = level; }
    public void setMapGrid(int x, int y) {
        this.mapX = x;
        this.mapY = y;
    }
}

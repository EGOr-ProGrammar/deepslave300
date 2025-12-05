package server.model;

import shared.Position;
import java.util.Random;

public class Player {
    private Position position;
    private final int colorCode;
    private int currentLevel = 1;
    private int hp = 20;
    private int maxHp = 20;
    private int attack = 4;
    private int gold = 0;

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

    public void takeDamage(int damage) {
        this.hp = Math.max(0, this.hp - damage);
    }

    public void addGold(int amount) {
        this.gold += amount;
    }

    public void heal(int amount) {
        this.hp = Math.min(this.maxHp, this.hp + amount);
    }

    public int getCurrentLevel() { return currentLevel; }
    public int getMapX() { return currentMapGridX; }
    public int getMapY() { return currentMapGridY; }
    public Position getPosition() { return position; }
    public void setPosition(Position position) { this.position = position; }
    public int getColorCode() { return colorCode; }
    public int getHp() { return hp; }
    public int getMaxHp() { return maxHp; }
    public int getAttack() { return attack; }
    public int getGold() { return gold; }

    public void setLevel(int level) { this.currentLevel = level; }
    public void setMapGrid(int x, int y) {
        this.currentMapGridX = x;
        this.currentMapGridY = y;
    }
    public void setHp(int hp) { this.hp = Math.max(0, Math.min(hp, maxHp)); }
    public void setMaxHp(int maxHp) { this.maxHp = maxHp; }
    public void setAttack(int attack) { this.attack = attack; }
    public void setGold(int gold) { this.gold = Math.max(0, gold); }
}

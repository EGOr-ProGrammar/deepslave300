package server.model;

/**
 * Тип ячейки игрового мира.
 */
public enum TileType {
    FLOOR('.'),
    WALL('#'),
    STAIRS_DOWN('>');

    public final char symbol;

    TileType(char symbol) {
        this.symbol = symbol;
    }
}

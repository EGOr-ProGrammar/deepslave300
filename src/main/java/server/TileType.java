package server;

/**
 * Тип ячейки игрового мира.
 */
public enum TileType {
    FLOOR('.'),
    WALL('#');

    public final char symbol;
    TileType(char symbol) { this.symbol = symbol; }
}

package shared;

/**
 * Содержит информацию об этаже уровня(мире)
 * @param width - ширина этажа по X
 * @param height - высота этажа по Y
 * @param tiles - тип ячейки этажа. Содержит ячейки типа TileType
 * @param playerPos - координаты (x,y) текущего положения игрока на этаже
 */
public record WorldSnapshot(
        int width,
        int height,
        char[][] tiles,
        Position playerPos
) {}
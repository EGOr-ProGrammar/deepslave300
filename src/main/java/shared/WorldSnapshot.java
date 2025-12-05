package shared;

import java.util.Map;

/**
 * Снимок состояния игрового мира для передачи клиенту.
 * Содержит всю информацию, необходимую для отрисовки текущего кадра.
 *
 * @param width - ширина уровня по X
 * @param height - высота уровня по Y
 * @param tiles - тип ячейки уровня (символы для отображения)
 * @param colors - ANSI коды цветов для каждой ячейки
 * @param playerPos - текущая позиция игрока
 * @param currentDepth - текущая глубина подземелья
 * @param timestamp - время создания снапшота (для синхронизации)
 * @param playersData - данные всех игроков на уровне (для мультиплеера)
 */
public record WorldSnapshot(
        int width,
        int height,
        char[][] tiles,
        int[][] colors,
        Position playerPos,
        int currentDepth,
        long timestamp,
        Map<String, PlayerData> playersData
) {
    /**
     * Упрощенный конструктор для обратной совместимости
     */
    public WorldSnapshot(int width, int height, char[][] tiles,
                         int[][] colors, Position playerPos) {
        this(width, height, tiles, colors, playerPos, 1,
                System.currentTimeMillis(), Map.of());
    }
}

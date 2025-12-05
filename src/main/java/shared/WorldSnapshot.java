package shared;

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

import java.io.Serializable;
import java.util.List;

public record WorldSnapshot(
        int width,
        int height,
        char[][] tiles,
        int[][] colors,
        Position playerPos,
        List<NpcSnapshot> npcs,       // ★ НОВОЕ
        List<LootSnapshot> loot,      // ★ НОВОЕ
        int playerHp,
        int playerMaxHp,
        int playerGold
) implements Serializable {

    public WorldSnapshot(int width, int height, char[][] tiles, int[][] colors, Position playerPos) {
        this(width, height, tiles, colors, playerPos,
                List.of(), List.of(), 20, 20, 0);
    }
}


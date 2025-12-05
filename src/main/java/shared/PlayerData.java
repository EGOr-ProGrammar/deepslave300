package shared;

import java.io.Serializable;

/**
 * Данные игрока для передачи в снапшоте мира.
 * Используется для отображения других игроков на карте.
 */
public record PlayerData(
        String id,
        String name,
        int x,
        int y,
        char symbol,
        int health,
        int maxHealth
) implements Serializable {

    /**
     * Базовый конструктор без здоровья
     */
    public PlayerData(String id, String name, int x, int y) {
        this(id, name, x, y, '@', 100, 100);
    }
}

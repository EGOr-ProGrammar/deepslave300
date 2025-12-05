package shared;

import java.io.Serializable;

/**
 * Снэпшот лута для передачи клиенту.
 */
public record LootSnapshot(
        int x,
        int y,
        int goldAmount,
        char symbol
) implements Serializable {

    public LootSnapshot(int x, int y, int goldAmount) {
        this(x, y, goldAmount, '$');
    }

    public int getColorCode() {
        return 93; // Ярко-желтый
    }
}

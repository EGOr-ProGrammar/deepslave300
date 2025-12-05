package shared;

import java.io.Serializable;

/**
 * Снэпшот NPC для передачи клиенту.
 */
public record NpcSnapshot(
        int x,
        int y,
        int hp,
        int maxHp,
        char symbol
) implements Serializable {

    public int getColorCode() {
        double hpPercent = (double) hp / maxHp;
        if (hpPercent > 0.7) return 91;  // Красный яркий (здоровый)
        if (hpPercent > 0.3) return 33;  // Желтый (раненый)
        return 31;                        // Красный темный (почти мертв)
    }
}

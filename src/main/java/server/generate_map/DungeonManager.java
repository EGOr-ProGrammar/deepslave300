package server.generate_map;

import java.util.HashMap;
import java.util.Map;

public class DungeonManager {
    private final Map<Integer, DungeonLevel> levels = new HashMap<>();
    private final long seed;

    public DungeonManager(long seed) {
        this.seed = seed;
        levels.put(1, new DungeonLevel(1, seed));
    }

    public DungeonLevel getLevel(int levelNum) {
        // Создать уровень, только когда кто-то на него заходит
        if (!levels.containsKey(levelNum)) {
            System.out.println("Generating Level " + levelNum + " with seed derivative...");
            // Каждый уровень получает свой производный seed
            long levelSeed = seed + (levelNum * 1000L);
            levels.put(levelNum, new DungeonLevel(levelNum, levelSeed));
        }
        return levels.get(levelNum);
    }

    public long getSeed() {
        return seed;
    }
}

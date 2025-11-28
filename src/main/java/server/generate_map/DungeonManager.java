package server.generate_map;

import java.util.HashMap;
import java.util.Map;

public class DungeonManager {
    private final Map<Integer, DungeonLevel> levels = new HashMap<>();

    public DungeonManager() {
        levels.put(1, new DungeonLevel(1));
    }

    public DungeonLevel getLevel(int levelNum) {
        // Создать уровень, только когда кто-то на него заходит
        if (!levels.containsKey(levelNum)) {
            System.out.println("Generating Level " + levelNum + "...");
            levels.put(levelNum, new DungeonLevel(levelNum));
        }
        return levels.get(levelNum);
    }
}

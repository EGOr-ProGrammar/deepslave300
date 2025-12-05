package server.generate_map;

public class DungeonLevel {
    private final int levelNumber; // 1..10
    private final DungeonMap[][] maps;
    private final int gridWidth;
    private final int gridHeight;
    private final long seed;

    public DungeonLevel(int levelNumber, long seed) {
        this.levelNumber = levelNumber;
        this.seed = seed;

        // Определить размер сетки (2x2, 3x3, 4x4)
        if (levelNumber == 10) {
            this.gridWidth = 1;
            this.gridHeight = 2; // Босс-уровень вытянутый
        } else if (levelNumber >= 7) {
            this.gridWidth = 4;
            this.gridHeight = 4;
        } else if (levelNumber >= 4) {
            this.gridWidth = 3;
            this.gridHeight = 3;
        } else {
            this.gridWidth = 2;
            this.gridHeight = 2;
        }

        this.maps = new DungeonMap[gridHeight][gridWidth];
        generateLevel();
    }

    private void generateLevel() {
        // Создаем карты с уникальными seed для каждой
        for (int y = 0; y < gridHeight; y++) {
            for (int x = 0; x < gridWidth; x++) {
                // Каждая карта получает уникальный seed на основе позиции
                long mapSeed = seed + (y * gridWidth + x);

                // Определяем наличие соседей
                boolean hasNorth = (y > 0);
                boolean hasSouth = (y < gridHeight - 1);
                boolean hasWest = (x > 0);
                boolean hasEast = (x < gridWidth - 1);

                maps[y][x] = new DungeonMap(x, y, mapSeed, hasNorth, hasSouth, hasWest, hasEast);
            }
        }

        // TODO: в будущем будет босс на 10 уровне
        if (levelNumber < 10) {
            // Если лестница должна быть сразу
            // spawnStairs();
        }
    }

    public DungeonMap getMap(int gridX, int gridY) {
        if (gridX < 0 || gridX >= gridWidth || gridY < 0 || gridY >= gridHeight) return null;
        return maps[gridY][gridX];
    }

    public int getGridWidth() { return gridWidth; }
    public int getGridHeight() { return gridHeight; }
    public long getSeed() { return seed; }
}

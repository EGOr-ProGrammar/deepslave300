package server.controller;

import server.generate_map.DungeonLevel;
import server.generate_map.DungeonManager;
import server.generate_map.DungeonMap;
import server.model.Player;
import server.model.TileType;
import shared.InputAction;
import shared.Position;
import shared.WorldSnapshot;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameWorld {
    private final DungeonManager dungeonManager;
    private final Map<String, Player> players = new ConcurrentHashMap<>();
    private final long seed;

    public GameWorld(long seed) {
        this.seed = seed;
        this.dungeonManager = new DungeonManager(seed);
        System.out.println("✓ Game world initialized with seed: " + seed);
    }

    public GameWorld() {
        this(System.currentTimeMillis());
    }

    /**
     * Обновление игрового мира (логика NPC, событий, таймеров)
     */
    public synchronized void tick() {
        // TODO Блок 3: обновление NPC и врагов
        // TODO: обработка событий мира
        // TODO: обновление таймеров и эффектов

        // Пока заглушка для будущей логики
         System.out.println("World tick executed");
    }

    public void addPlayer(ClientHandler client) {
        String playerId = client.getPlayerId(); // ★ Получаем ID

        DungeonLevel level1 = dungeonManager.getLevel(1);
        int startMapX = 0;
        int startMapY = 0;

        DungeonMap map = level1.getMap(startMapX, startMapY);
        Position spawnPos = map.getRandomFloorPosition();

        Player newPlayer = new Player(spawnPos);
        newPlayer.setMapGrid(startMapX, startMapY);

        players.put(playerId, newPlayer);
        System.out.println("✓ Player added: " + playerId + " at " + spawnPos);
    }

    public void removePlayer(ClientHandler client) {
        String playerId = client.getPlayerId();
        players.remove(playerId);
        System.out.println("✓ Player removed: " + playerId);
    }

    public synchronized void applyInput(ClientHandler client, InputAction action) {
        String playerId = client.getPlayerId();
        Player player = players.get(playerId);
        if (player == null) return;

        // Получить текущую карту игрока
        DungeonLevel level = dungeonManager.getLevel(player.getCurrentLevel());
        DungeonMap currentMap = level.getMap(player.getMapX(), player.getMapY());

        Position currentPos = player.getPosition();
        int nx = currentPos.x();
        int ny = currentPos.y();

        switch (action) {
            case MOVE_UP -> ny--;
            case MOVE_DOWN -> ny++;
            case MOVE_LEFT -> nx--;
            case MOVE_RIGHT -> nx++;
        }

        // Логика перехода между картами
        if (nx < 0) {
            // Идем влево
            if (trySwitchMap(player, level, -1, 0, DungeonMap.WIDTH - 2, ny)) return;
            nx = 0; // Если перехода нет, упереться в стену
        } else if (nx >= DungeonMap.WIDTH) {
            // Идем вправо
            if (trySwitchMap(player, level, 1, 0, 1, ny)) return;
            nx = DungeonMap.WIDTH - 1;
        } else if (ny < 0) {
            // Идем вверх
            if (trySwitchMap(player, level, 0, -1, nx, DungeonMap.HEIGHT - 2)) return;
            ny = 0;
        } else if (ny >= DungeonMap.HEIGHT) {
            // Идем вниз
            if (trySwitchMap(player, level, 0, 1, nx, 1)) return;
            ny = DungeonMap.HEIGHT - 1;
        }

        TileType tile = currentMap.getTile(nx, ny);
        if (tile == TileType.WALL) return;

        // Проверка коллизий с игроками
        for (Player other : players.values()) {
            if (other != player &&
                    other.getCurrentLevel() == player.getCurrentLevel() &&
                    other.getMapX() == player.getMapX() &&
                    other.getMapY() == player.getMapY() &&
                    other.getPosition().x() == nx &&
                    other.getPosition().y() == ny) {
                return;
            }
        }

        // Проверка спуска вниз
        if (tile == TileType.STAIRS_DOWN) {
            int nextLevel = player.getCurrentLevel() + 1;
            DungeonLevel newLevel = dungeonManager.getLevel(nextLevel);

            // Спаун на новом уровне
            DungeonMap newMap = newLevel.getMap(0, 0);
            Position newSpawn = newMap.getRandomFloorPosition();

            player.setLevel(nextLevel);
            player.setMapGrid(0, 0);
            player.setPosition(newSpawn);

            System.out.println("✓ Player " + playerId + " descended to level " + nextLevel);
            return;
        }

        player.setPosition(new Position(nx, ny));
    }

    private boolean trySwitchMap(Player player, DungeonLevel level, int dx, int dy, int newX, int newY) {
        int newGridX = player.getMapX() + dx;
        int newGridY = player.getMapY() + dy;

        // Существует ли соседняя карта
        if (level.getMap(newGridX, newGridY) != null) {
            player.setMapGrid(newGridX, newGridY);
            player.setPosition(new Position(newX, newY));
            return true;
        }
        return false;
    }

    public synchronized WorldSnapshot snapshot(ClientHandler forClient) {
        String myPlayerId = forClient.getPlayerId();
        Player myPlayer = players.get(myPlayerId);
        if (myPlayer == null) return null;

        // Текущая карта игрока
        DungeonLevel level = dungeonManager.getLevel(myPlayer.getCurrentLevel());
        DungeonMap map = level.getMap(myPlayer.getMapX(), myPlayer.getMapY());

        int w = DungeonMap.WIDTH;
        int h = DungeonMap.HEIGHT;
        char[][] chars = new char[h][w];
        int[][] colors = new int[h][w];

        // Рендер тайлов карты
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                TileType t = map.getTile(x, y);
                chars[y][x] = t.symbol;
                if (t == TileType.WALL) colors[y][x] = 90; // Серый
                else if (t == TileType.STAIRS_DOWN) colors[y][x] = 97; // Белый яркий
                else colors[y][x] = 37; // Пол
            }
        }

        // Рендер игроков карты
        for (Map.Entry<String, Player> entry : players.entrySet()) {
            String playerId = entry.getKey(); // ★ String вместо ClientHandler
            Player p = entry.getValue();

            if (p.getCurrentLevel() == myPlayer.getCurrentLevel() &&
                    p.getMapX() == myPlayer.getMapX() &&
                    p.getMapY() == myPlayer.getMapY()) {

                Position pos = p.getPosition();
                // ★ ИЗМЕНЕНО: сравнение строк
                if (playerId.equals(myPlayerId)) {
                    chars[pos.y()][pos.x()] = '@';
                    colors[pos.y()][pos.x()] = 92;
                } else {
                    chars[pos.y()][pos.x()] = 'P';
                    colors[pos.y()][pos.x()] = p.getColorCode();
                }
            }
        }

        return new WorldSnapshot(w, h, chars, colors, myPlayer.getPosition());
    }

    public int getPlayerCount() {
        return players.size();
    }

    public long getSeed() {
        return seed;
    }
}

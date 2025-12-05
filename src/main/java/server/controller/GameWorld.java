package server.controller;

import server.generate_map.DungeonLevel;
import server.generate_map.DungeonManager;
import server.generate_map.DungeonMap;
import server.model.EnemyNpc;
import server.model.LootPile;
import server.model.Player;
import server.model.TileType;
import shared.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class GameWorld {
    private final Map<UUID, EnemyNpc> enemies = new ConcurrentHashMap<>();
    private final List<LootPile> lootPiles = new ArrayList<>();
    private final DungeonManager dungeonManager;
    private final Map<String, Player> players = new ConcurrentHashMap<>();
    private final long seed;

    public GameWorld(long seed) {
        this.seed = seed;
        this.dungeonManager = new DungeonManager(seed);
        System.out.println("Game world initialized with seed: " + seed);
    }

    public GameWorld() {
        this(System.currentTimeMillis());
    }

    public synchronized void tick() {
        // ИИ мобов: преследование и атака
        for (EnemyNpc enemy : enemies.values()) {
            if (!enemy.isAlive()) continue;

            Player targetPlayer = null;
            int minDistance = Integer.MAX_VALUE;

            for (Player p : players.values()) {
                if (p.getCurrentLevel() == enemy.getCurrentLevel() &&
                        p.getMapX() == enemy.getMapX() &&
                        p.getMapY() == enemy.getMapY()) {

                    int dist = enemy.distanceTo(p.getPosition());
                    if (dist < minDistance && enemy.isInAggroRange(p.getPosition())) {
                        minDistance = dist;
                        targetPlayer = p;
                    }
                }
            }

            if (targetPlayer != null) {
                Position nextPos = enemy.getNextStepTowards(targetPlayer.getPosition());
                DungeonLevel level = dungeonManager.getLevel(enemy.getCurrentLevel());
                DungeonMap map = level.getMap(enemy.getMapX(), enemy.getMapY());
                TileType tile = map.getTile(nextPos.x(), nextPos.y());

                if (tile != TileType.WALL) {
                    if (nextPos.equals(targetPlayer.getPosition())) {
                        targetPlayer.takeDamage(enemy.getAttack());
                        System.out.println("⚔ Enemy hit player for " + enemy.getAttack() + " dmg");

                        if (targetPlayer.getHp() <= 0) {
                            respawnPlayer(targetPlayer);
                        }
                    } else {
                        enemy.setPosition(nextPos);
                    }
                }
            }
        }

        // Удаление мертвых мобов и дроп лута
        Iterator<Map.Entry<UUID, EnemyNpc>> it = enemies.entrySet().iterator();
        while (it.hasNext()) {
            EnemyNpc enemy = it.next().getValue();
            if (!enemy.isAlive()) {
                LootPile loot = new LootPile(enemy.getPosition(),
                        3 + new java.util.Random().nextInt(3));
                loot.setLevel(enemy.getCurrentLevel());
                loot.setMapGrid(enemy.getMapX(), enemy.getMapY());
                lootPiles.add(loot);
                System.out.println("Enemy died, dropped " + loot.getGoldAmount() + " gold");
                it.remove();
            }
        }

        // Автоподбор лута
        for (Player p : players.values()) {
            Iterator<LootPile> lootIt = lootPiles.iterator();
            while (lootIt.hasNext()) {
                LootPile loot = lootIt.next();
                if (p.getCurrentLevel() == loot.getLevel() &&
                        p.getMapX() == loot.getMapX() &&
                        p.getMapY() == loot.getMapY() &&
                        p.getPosition().equals(loot.getPosition())) {

                    p.addGold(loot.getGoldAmount());
                    System.out.println("💰 Player picked up " + loot.getGoldAmount() + " gold");
                    lootIt.remove();
                }
            }
        }
    }

    private void respawnPlayer(Player player) {
        DungeonLevel level = dungeonManager.getLevel(1);
        DungeonMap map = level.getMap(0, 0);
        Position respawnPos = map.getRandomFloorPosition();

        player.setLevel(1);
        player.setMapGrid(0, 0);
        player.setPosition(respawnPos);
        player.setHp(player.getMaxHp());
        player.setGold(0);

        System.out.println("Player died and respawned at " + respawnPos);
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

    private void spawnEnemiesOnMap(DungeonLevel level, int mapX, int mapY, int levelNum) {
        DungeonMap map = level.getMap(mapX, mapY);
        if (map == null) return;

        int mobCount = 2 + (levelNum - 1) + new java.util.Random().nextInt(3);
        mobCount = Math.min(mobCount, 8);

        for (int i = 0; i < mobCount; i++) {
            Position spawnPos = map.getRandomFloorPosition();
            EnemyNpc mob = EnemyNpc.createBasicMob(spawnPos);
            mob.setLevel(levelNum);
            mob.setMapGrid(mapX, mapY);
            enemies.put(mob.getId(), mob);
        }

        System.out.println("✓ Spawned " + mobCount + " enemies on level " + levelNum);
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

        DungeonLevel level = dungeonManager.getLevel(myPlayer.getCurrentLevel());
        DungeonMap map = level.getMap(myPlayer.getMapX(), myPlayer.getMapY());
        int w = DungeonMap.WIDTH;
        int h = DungeonMap.HEIGHT;
        char[][] chars = new char[h][w];
        int[][] colors = new int[h][w];

        // Рендер тайлов
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                TileType t = map.getTile(x, y);
                chars[y][x] = t.symbol;
                if (t == TileType.WALL) colors[y][x] = 90;
                else if (t == TileType.STAIRS_DOWN) colors[y][x] = 97;
                else colors[y][x] = 37;
            }
        }

        // Сбор мобов
        List<NpcSnapshot> npcSnapshots = new ArrayList<>();
        for (EnemyNpc enemy : enemies.values()) {
            if (enemy.getCurrentLevel() == myPlayer.getCurrentLevel() &&
                    enemy.getMapX() == myPlayer.getMapX() &&
                    enemy.getMapY() == myPlayer.getMapY() &&
                    enemy.isAlive()) {

                Position pos = enemy.getPosition();
                npcSnapshots.add(new NpcSnapshot(
                        pos.x(), pos.y(),
                        enemy.getHp(), enemy.getMaxHp(),
                        enemy.getSymbol()
                ));
            }
        }

        // Сбор лута
        List<LootSnapshot> lootSnapshots = new ArrayList<>();
        for (LootPile loot : lootPiles) {
            if (loot.getLevel() == myPlayer.getCurrentLevel() &&
                    loot.getMapX() == myPlayer.getMapX() &&
                    loot.getMapY() == myPlayer.getMapY()) {

                Position pos = loot.getPosition();
                lootSnapshots.add(new LootSnapshot(
                        pos.x(), pos.y(),
                        loot.getGoldAmount()
                ));
            }
        }

        // Рендер игроков
        for (Map.Entry<String, Player> entry : players.entrySet()) {
            String playerId = entry.getKey();
            Player p = entry.getValue();
            if (p.getCurrentLevel() == myPlayer.getCurrentLevel() &&
                    p.getMapX() == myPlayer.getMapX() &&
                    p.getMapY() == myPlayer.getMapY()) {

                Position pos = p.getPosition();
                if (playerId.equals(myPlayerId)) {
                    // Пользователь
                    chars[pos.y()][pos.x()] = '@';
                    colors[pos.y()][pos.x()] = 92;
                } else {
                    // Другие игроки
                    chars[pos.y()][pos.x()] = 'P';
                    colors[pos.y()][pos.x()] = p.getColorCode();
                }
            }
        }

        // Передача статов игрока
        return new WorldSnapshot(
                w, h, chars, colors, myPlayer.getPosition(),
                npcSnapshots, lootSnapshots,
                myPlayer.getHp(), myPlayer.getMaxHp(), myPlayer.getGold()
        );
    }

    public int getPlayerCount() {
        return players.size();
    }

    public long getSeed() {
        return seed;
    }
}

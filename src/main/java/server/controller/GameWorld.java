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
    private final Map<ClientHandler, Player> players = new ConcurrentHashMap<>();

    public GameWorld() {
        this.dungeonManager = new DungeonManager();
    }

    public void addPlayer(ClientHandler client) {
        // Спавним игрока на 1 уровне в случайной карте
        DungeonLevel level1 = dungeonManager.getLevel(1);
        int startMapX = 0; // Можно сделать рандомно
        int startMapY = 0;

        DungeonMap map = level1.getMap(startMapX, startMapY);
        Position spawnPos = map.getRandomFloorPosition();

        Player newPlayer = new Player(spawnPos);
        newPlayer.setMapGrid(startMapX, startMapY);

        players.put(client, newPlayer);
    }

    public void removePlayer(ClientHandler client) {
        players.remove(client);
    }

    public synchronized void applyInput(ClientHandler client, InputAction action) {
        Player player = players.get(client);
        if (player == null) return;

        // Получаем текущую карту игрока
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

        // --- ЛОГИКА ПЕРЕХОДА МЕЖДУ КАРТАМИ ---
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
            // TODO: реализовать логику перехода на след уровень
            // player.setLevel(player.getCurrentLevel() + 1)
            // Но пока просто стоим на ней
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
        Player myPlayer = players.get(forClient);
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
        for (Map.Entry<ClientHandler, Player> entry : players.entrySet()) {
            ClientHandler handler = entry.getKey();
            Player p = entry.getValue();

            if (p.getCurrentLevel() == myPlayer.getCurrentLevel() &&
                    p.getMapX() == myPlayer.getMapX() &&
                    p.getMapY() == myPlayer.getMapY()) {

                Position pos = p.getPosition();
                if (handler == forClient) {
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
}

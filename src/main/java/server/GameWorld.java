package server;

import server.model.Player;
import shared.InputAction;
import shared.Position;
import shared.WorldSnapshot;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameWorld {
    private final int width = 40;
    private final int height = 20;
    private final TileType[][] tiles = new TileType[height][width];

    private final Map<ClientHandler, Player> players = new ConcurrentHashMap<>();

    public GameWorld() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                boolean border = x == 0 || y == 0 || x == width - 1 || y == height - 1;
                tiles[y][x] = border ? TileType.WALL : TileType.FLOOR;
            }
        }
    }

    public void addPlayer(ClientHandler client) {
        // Спавн в свободной точке
        int id = players.size();
        Position startPos = new Position(2 + id % (width - 4), 2 + (id / (width - 4)));
        players.put(client, new Player(startPos));
    }

    public void removePlayer(ClientHandler client) {
        players.remove(client);
    }

    public synchronized void applyInput(ClientHandler client, InputAction action) {
        Player player = players.get(client);
        if (player == null) return;

        Position currentPos = player.getPosition();
        int nx = currentPos.x();
        int ny = currentPos.y();

        switch (action) {
            case MOVE_UP -> ny--;
            case MOVE_DOWN -> ny++;
            case MOVE_LEFT -> nx--;
            case MOVE_RIGHT -> nx++;
        }

        // Проверка границ и стен
        if (nx < 0 || nx >= width || ny < 0 || ny >= height || tiles[ny][nx] != TileType.FLOOR) {
            return;
        }

        // Проверка коллизий с другими игроками
        for (Player other : players.values()) {
            if (other != player && other.getPosition().x() == nx && other.getPosition().y() == ny) {
                return; // Клетка занята
            }
        }

        // Если все чисто - свдиг
        player.setPosition(new Position(nx, ny));
    }

    public synchronized WorldSnapshot snapshot(ClientHandler forClient) {
        char[][] chars = new char[height][width];
        int[][] colors = new int[height][width];

        // Отрисовка уровня
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                chars[y][x] = tiles[y][x].symbol;
                // 90 - серый для стен, 37 - белый для пола
                colors[y][x] = (tiles[y][x] == TileType.WALL) ? 90 : 37;
            }
        }

        // Рендер игроков
        for (Map.Entry<ClientHandler, Player> entry : players.entrySet()) {
            ClientHandler handler = entry.getKey();
            Player p = entry.getValue();
            Position pos = p.getPosition();

            if (handler == forClient) {
                chars[pos.y()][pos.x()] = '@';
                colors[pos.y()][pos.x()] = 92; // Зеленейший для себя
            } else {
                chars[pos.y()][pos.x()] = 'P';
                colors[pos.y()][pos.x()] = p.getColorCode(); // Цвет другого игрока
            }
        }

        Player myPlayer = players.get(forClient);
        Position myPos = (myPlayer != null) ? myPlayer.getPosition() : new Position(0, 0);

        return new WorldSnapshot(width, height, chars, colors, myPos);
    }

    public Player getPlayer(ClientHandler handler) {
        return players.get(handler);
    }
}

package server;

import shared.InputAction;
import shared.Position;
import shared.WorldSnapshot;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameWorld {
    private final int width = 40;
    private final int height = 20;
    private final TileType[][] tiles = new TileType[height][width];

    // Храним игроков по ID клиента (ClientHandler)
    private final Map<ClientHandler, Position> players = new ConcurrentHashMap<>();

    public GameWorld() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                boolean border = x == 0 || y == 0 || x == width - 1 || y == height - 1;
                tiles[y][x] = border ? TileType.WALL : TileType.FLOOR;
            }
        }
    }

    // Регистрация нового игрока
    public void addPlayer(ClientHandler client) {
        // Спавним в центре или случайно. Пока просто (2,2) + смещение
        int id = players.size();
        players.put(client, new Position(2 + id, 2));
    }

    public void removePlayer(ClientHandler client) {
        players.remove(client);
    }

    public synchronized void applyInput(ClientHandler client, InputAction action) {
        Position currentPos = players.get(client);
        if (currentPos == null) return;

        int nx = currentPos.x();
        int ny = currentPos.y();

        switch (action) {
            case MOVE_UP -> ny--;
            case MOVE_DOWN -> ny++;
            case MOVE_LEFT -> nx--;
            case MOVE_RIGHT -> nx++;
        }

        if (nx >= 0 && nx < width && ny >= 0 && ny < height &&
                tiles[ny][nx] == TileType.FLOOR) {
            players.put(client, new Position(nx, ny));
        }
    }

    // Генерируем снапшот персонально для каждого игрока
    // (чтобы камера центрировалась на нем, если нужно, или просто для подсветки себя)
    public synchronized WorldSnapshot snapshot(ClientHandler forClient) {
        char[][] chars = new char[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                chars[y][x] = tiles[y][x].symbol;
            }
        }

        // Отрисуем ВСЕХ игроков на карте
        for (Map.Entry<ClientHandler, Position> entry : players.entrySet()) {
            Position p = entry.getValue();
            ClientHandler pClient = entry.getKey();

            // Если это "мы", ставим @, если враг - E (Enemy)
            char symbol = (pClient == forClient) ? '@' : 'P';
            chars[p.y()][p.x()] = symbol;
        }

        // Возвращаем позицию именно этого клиента для фокуса камеры (если реализовано на клиенте)
        Position myPos = players.get(forClient);
        if (myPos == null) myPos = new Position(0, 0); // На случай бага

        return new WorldSnapshot(width, height, chars, myPos);
    }
}

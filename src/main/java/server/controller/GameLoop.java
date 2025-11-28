package server.controller;

import server.TcpServer;

public class GameLoop implements Runnable {
    private final TcpServer server;

    public GameLoop(TcpServer server) {
        this.server = server;
    }

    @Override
    public void run() {
        while (true) {
            // TODO: обновлять состояние мира, когда добавлю мобов. Типа такого:
            // world.tick();

            server.broadcastState();

            try {
                Thread.sleep(50); // 20 TPS (Тиков в секунду)
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}

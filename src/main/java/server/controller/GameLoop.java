package server.controller;

import server.TcpServer;

public class GameLoop implements Runnable {
    private final TcpServer server;
    private final int tickRate;

    public GameLoop(TcpServer server, int tickRate) {
        this.server = server;
        this.tickRate = tickRate;
    }

    @Override
    public void run() {
        System.out.println("Game loop started (tick rate: " + tickRate + "ms)");
        while (true) {
            try {
                // Обновление игрового мира
                server.getWorld().tick();

                // Рассылка состояния всем клиентам
                server.broadcastState();

                // Ожидать перед следующим тиком
                Thread.sleep(tickRate);
            } catch (InterruptedException e) {
                System.err.println("Game loop interrupted");
                break;
            } catch (Exception e) {
                System.err.println("Error in game loop: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}

package server;

import shared.WorldSnapshot;

public class GameLoop {
    public static void run(GameWorld world, ClientHandler handler) {
        final int TPS = 10; // 10 тиков в секунду
        final long OPTIMAL_TIME = 1_000_000_000L / TPS;
        long lastTime = System.nanoTime();

        while (true) {
            long now = System.nanoTime();
            long delta = now - lastTime;
            if (delta < OPTIMAL_TIME) {
                try {
                    long sleepMs = (OPTIMAL_TIME - delta) / 1_000_000L;
                    if (sleepMs > 0) Thread.sleep(sleepMs);
                } catch (InterruptedException ignored) {}
                continue;
            }
            lastTime = now;

            WorldSnapshot snapshot = world.snapshot();
            handler.sendSnapshot(snapshot);
        }
    }
}


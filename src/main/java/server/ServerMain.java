package server;

import server.controller.GameWorld;

public class ServerMain {

    public static void main(String[] args) {
        try {
            // Вся логика загрузки внутри ServerConfig
            ServerConfig config = ServerConfig.load();

            // Вывод и валидация
            config.printConfiguration();
            config.validate();

            // Создание и запуск
            GameWorld world = new GameWorld(config.getSeed());
            TcpServer server = new TcpServer(config, world);

            System.out.println("\nServer starting...\n");
            server.start();

        } catch (Exception e) {
            System.err.println("Fatal error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}

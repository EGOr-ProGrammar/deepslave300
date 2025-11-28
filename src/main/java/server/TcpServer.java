package server;

import server.controller.ClientHandler;
import server.controller.GameLoop;
import server.controller.GameWorld;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class TcpServer {
    private final int port;
    private final GameWorld world;
    // Список всех активных сессий для рассылки обновлений
    private final List<ClientHandler> clients = new ArrayList<>();

    public TcpServer(int port, GameWorld world) {
        this.port = port;
        this.world = world;
    }

    public void start() throws IOException {
        // 1. Запускаем единый игровой цикл в отдельном потоке
        new Thread(new GameLoop(this), "server-gameloop").start();

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port " + port);

            // 2. Вечный цикл приема подключений
            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    System.out.println("New client connected: " + socket.getInetAddress());

                    ClientHandler handler = new ClientHandler(socket, world);

                    synchronized (clients) {
                        clients.add(handler);
                    }

                    // Запускаем чтение ввода клиента в его личном потоке
                    new Thread(handler::readLoop, "client-handler").start();

                } catch (IOException e) {
                    System.err.println("Connection error: " + e.getMessage());
                }
            }
        }
    }

    public void broadcastState() {
        synchronized (clients) {
            // Удаляем отключившихся
            clients.removeIf(ClientHandler::isDisconnected);

            // Рассылаем состояние каждому
            for (ClientHandler client : clients) {
                client.sendSnapshot(world.snapshot(client));
            }
        }
    }
}

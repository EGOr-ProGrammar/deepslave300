package server;

public class ServerMain {
    public static void main(String[] args) throws Exception {
        GameWorld world = new GameWorld();
        TcpServer server = new TcpServer(4000, world);
        // TODO: блокируется, ждёт клиента и крутит game-loop, а надо в real time
        server.start();
    }
}

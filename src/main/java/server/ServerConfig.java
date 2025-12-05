package server;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ServerConfig {
    private final int port;
    private final String databasePath;
    private final int tickRate;
    private final int maxPlayers;
    private final long seed;
    private final String host;
    private final int maxConnections;

    private ServerConfig(Properties props) {
        this.port = Integer.parseInt(props.getProperty("server.port", "4000"));
        this.host = props.getProperty("server.host", "0.0.0.0");
        this.seed = Long.parseLong(props.getProperty("world.seed", "42"));
        this.databasePath = props.getProperty("database.path", "./data/deepslave.db");
        this.tickRate = Integer.parseInt(props.getProperty("game.tick-rate", "50"));
        this.maxPlayers = Integer.parseInt(props.getProperty("game.max-players", "4"));
        this.maxConnections = Integer.parseInt(props.getProperty("server.max-connections", "10"));
    }

    public static ServerConfig load() {
        Properties props = new Properties();

        try (InputStream input = ServerConfig.class
                .getClassLoader()
                .getResourceAsStream("application.properties")) {

            if (input == null) {
                System.out.println("⚠ application.properties not found, using defaults");
            } else {
                props.load(input);
                System.out.println("✓ Configuration loaded from application.properties");
            }

        } catch (IOException ex) {
            System.err.println("⚠ Error loading configuration: " + ex.getMessage());
        }

        return new ServerConfig(props);
    }

    public static ServerConfig loadFromFile(String filename) {
        Properties props = new Properties();

        try (InputStream input = ServerConfig.class
                .getClassLoader()
                .getResourceAsStream(filename)) {

            if (input == null) {
                throw new IllegalArgumentException("Config file not found: " + filename);
            }

            props.load(input);
            System.out.println("✓ Configuration loaded from " + filename);

        } catch (IOException ex) {
            throw new RuntimeException("Failed to load config from " + filename, ex);
        }

        return new ServerConfig(props);
    }

    public void validate() {
        if (port < 1 || port > 65535) {
            throw new IllegalStateException("Invalid port: " + port);
        }
        if (maxPlayers < 1 || maxPlayers > 100) {
            throw new IllegalStateException("Invalid max players: " + maxPlayers);
        }
        System.out.println("✓ Configuration validated");
    }

    public void printConfiguration() {
        System.out.println("=".repeat(60));
        System.out.println("DeepSlave300 Server Configuration");
        System.out.println("=".repeat(60));
        System.out.println("Network: " + host + ":" + port);
        System.out.println("Seed: " + seed);
        System.out.println("Max Players: " + maxPlayers);
        System.out.println("Tick Rate: " + tickRate + "ms");
        System.out.println("Database: " + databasePath);
        System.out.println("=".repeat(60));
    }

    // Геттеры
    public int getPort() { return port; }
    public long getSeed() { return seed; }
    public String getDatabasePath() { return databasePath; }
    public int getTickRate() { return tickRate; }
    public int getMaxPlayers() { return maxPlayers; }
    public String getHost() { return host; }
}

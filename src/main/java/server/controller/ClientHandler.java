package server.controller;

import shared.InputAction;
import shared.WorldSnapshot;

import java.io.*;
import java.net.Socket;
import java.util.UUID;

public class ClientHandler {
    private final Socket socket;
    private final GameWorld world;
    private final DataInputStream in;
    private final ObjectOutputStream out;  // ← изменено
    private volatile boolean disconnected = false;
    private final String clientAddress;
    private final String playerId;

    public ClientHandler(Socket socket, GameWorld world) throws IOException {
        this.socket = socket;
        this.world = world;
        this.clientAddress = socket.getInetAddress().toString();
        this.playerId = UUID.randomUUID().toString();

        // Нужно ObjectOutputStream создать до DataInputStream
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.out.flush();

        this.in = new DataInputStream(socket.getInputStream());
        world.addPlayer(this);
    }

    public void readLoop() {
        try {
            while (!disconnected) {
                int code = in.readInt();
                InputAction action = InputAction.values()[code];
                world.applyInput(this, action);
            }
        } catch (IOException e) {
            close();
        }
    }

    public synchronized void sendSnapshot(WorldSnapshot snapshot) {
        if (disconnected) return;
        try {
            out.writeObject(snapshot);
            out.reset();
            out.flush();
        } catch (IOException e) {
            close();
        }
    }

    private void close() {
        if (disconnected) return;
        disconnected = true;
        System.out.println("Client disconnected: " + clientAddress + " (ID: " + playerId + ")");
        world.removePlayer(this);
        try {
            socket.close();
        } catch (IOException ignored) {}
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getAddress() {
        return clientAddress;
    }

    public boolean isDisconnected() {
        return disconnected;
    }
}

package client;

import shared.WorldSnapshot;

public class ClientGameState {
    public volatile WorldSnapshot snapshot;
}

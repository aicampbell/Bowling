package resources;

import actors.Client;

/**
 * Created by mo on 17.11.16.
 */
public class BowlingAlley {
    private int id;
    private int clientsReadyToPlay;

    private BowlingArea bowlingArea;

    private boolean isFree;

    public BowlingAlley(int id, BowlingArea bowlingArea) {
        this.id = id;
        this.bowlingArea = bowlingArea;
    }

    public synchronized void play(Client client) {
        isFree = false;
        // not synchronized because multiple clients (all clients in a Group) should be able to start playing
        // is synchronized because we modify a variable here (playingClients) which can only be modified by one thread at a time
        clientsReadyToPlay++;

        if (clientsReadyToPlay == client.getGroup().getMaxSize()) {
            notifyAll();
        } else {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }

        // Clients play
        client.bowl();

        gameEnded(client);
    }

    // TODO: any Client should inform about end of game, not every Client.
    public synchronized void gameEnded(Client client) {
        if (!isFree) {
            isFree = true;
            bowlingArea.gameEnded(this);
        }
    }
}

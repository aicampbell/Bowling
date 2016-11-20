package resources;

import actors.Client;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mo on 17.11.16.
 */
public class BowlingAlley {
    //private Group playingGroup;
    private List<Client> playingClients;

    private boolean isFree;

    public BowlingAlley() {
        playingClients = new ArrayList<>();
    }

    public synchronized void play(Client client) {
        isFree = false;
        // not synchronized because multiple clients (all clients in a Group) should be able to start playing
        // is synchronized because we modify a variable here (playingClients) which can only be modified by one thread at a time
        playingClients.add(client);

        // If the last expected Client of a Group arrived, all Clients in the Group can start bowling.
        if(playingClients.size() == client.getGroup().getMaxSize()) {
            playingClients.forEach(c -> c.bowl());
        }

        // all other clients in the group must start playing as well
        // ...client.bowl() which Thread.sleep(3s) or similar
    }

    public synchronized void endPlaying(Client client) {
        isFree = true;
    }
}

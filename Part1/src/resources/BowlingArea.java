package resources;

import actors.Client;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mo on 17.11.16.
 */
public class BowlingArea {
    private static int NUM_ALLEYS = 3;

    List<BowlingAlley> bowlingAlleys;

    /**
     * Construct BowlingArea with 3 BowlingAlleys.
     */
    public BowlingArea() {
        bowlingAlleys = new ArrayList<>();
        for (int i = 0; i < NUM_ALLEYS; i++) {
            bowlingAlleys.add(new BowlingAlley());
        }
    }

    public synchronized void arrive(Client client) {
        // wait for all other clients in his group at an already assigned alley
    }

    public synchronized void leave(Client client) {
        // release Clients from their Group
    }
}

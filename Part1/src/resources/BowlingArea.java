package resources;

import actors.Client;
import utils.Group;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by mo on 17.11.16.
 */
public class BowlingArea {
    private static int NUM_ALLEYS = 3;

    Set<BowlingAlley> availableAlleys;
    Map<BowlingAlley, Group> bowlingAlleys;
    Set<Group> playingGroups;

    /**
     * Construct BowlingArea with 3 BowlingAlleys.
     */
    public BowlingArea() {
        availableAlleys = new HashSet<>();
        bowlingAlleys = new HashMap<>();
        for (int i = 0; i < NUM_ALLEYS; i++) {
            bowlingAlleys.put(new BowlingAlley(), null);
        }
    }

    public synchronized void requestAlley(Client client) {
        Group group = client.getGroup();

        if (!group.hasAlleyAssigned()) {
            return;
        }

        while (getFreeBowlingAlley() == null) { // TODO: Or IF?
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }

        BowlingAlley freeAlley = getFreeBowlingAlley();
        group.setBowlingAlley(freeAlley);
    }

    private BowlingAlley getFreeBowlingAlley() {
        if (availableAlleys.isEmpty()) {
            return null;
        }
        // Return any free BowlingAlley
        return availableAlleys.iterator().next();
    }

    public synchronized void gameEnded(Client client) {
        if (bowlingAlleys.get(client.getBowlingAlley()) != null) {
            bowlingAlleys.put(client.getBowlingAlley(), null);
            playingGroups.remove(client.getGroup());
            notify();
        }
    }
}

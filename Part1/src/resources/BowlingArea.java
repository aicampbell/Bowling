package resources;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by mo on 17.11.16.
 */
public class BowlingArea {
    private static int NUM_ALLEYS = 3;

    // need reference to the DancingRoom to inform him that BowlingAlley is free'd up after Group finished playing.
    DancingRoom dancingRoom;

    // Semaphore (init NUM_ALLEYS) feels suitable here. But probably not needed.
    Set<BowlingAlley> availableAlleys;
    Set<BowlingAlley> occupiedAlleys;

    /**
     * Construct BowlingArea with NUM_ALLEYS BowlingAlleys.
     */
    public BowlingArea(DancingRoom dancingRoom) {
        this.dancingRoom = dancingRoom;

        availableAlleys = new HashSet<>();
        occupiedAlleys = new HashSet<>();

        for (int i = 0; i < NUM_ALLEYS; i++) {
            availableAlleys.add(new BowlingAlley(i, this));
        }
    }

    // Return a free BowlingAlley if there is one. Otherwise return null;
    public BowlingAlley getFreeAlley() {
        if (availableAlleys.isEmpty()) {
            return null;
        }

        BowlingAlley freeAlley = availableAlleys.iterator().next();
        availableAlleys.remove(freeAlley);
        occupiedAlleys.add(freeAlley);

        return freeAlley;
    }

    // Notification of a BowlingAlley that a Group just finished playing on it.
    public synchronized void gameEnded(BowlingAlley releasedAlley) {
        occupiedAlleys.remove(releasedAlley);
        availableAlleys.add(releasedAlley);

        // Notify DancingRoom that game ended. no need for synchronization because DancingRoom is not a thread
        dancingRoom.gameEnded();
    }
}

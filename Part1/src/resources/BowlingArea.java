package resources;

import java.util.HashSet;
import java.util.Set;

/**
 * A BowlingArea has no direct interaction with the Clients. It is only responsible to inform
 * the DancingRoom that a BowlingAlley is free. Clients then work directly with the passed
 * object of a BowlingAlley.
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

    /**
     * Must be synchronized because up to NUM_ALLEYS Clients may want to register for a BowlingAlley
     * to the same time. Since this method modifies variables, only one Client can enter this method
     * at any given time.
     *
     * @return free BowlingAlley that requester's Group can play on
     */
    public synchronized BowlingAlley getFreeAlley() {
        // When calling getFreeAlley(), it is assumed that a free alley exists. The caller (DanceRoom) must check
        // for a free alley with method BowlingArea.isAlleyFree().
        assert !availableAlleys.isEmpty();

        BowlingAlley freeAlley = availableAlleys.iterator().next();
        availableAlleys.remove(freeAlley);
        occupiedAlleys.add(freeAlley);

        System.out.println("(BowlingArea): A free BowlingAlley just got assigned to a Group. Available BowlingAlleys now: " + availableAlleys.size() + "/" + NUM_ALLEYS);

        return freeAlley;
    }

    /**
     * The only calling method (in DanceRoom) is synchronized itself. An explicit
     * {@code synchronized} keywords is therefore not needed here. However we still
     * add it to increase the readability of the proposed solution. If there was
     * another caller or the calling-method was not synchronized, we would have to
     * insert a {@code synchronized} here.
     *
     * @return
     */
    public synchronized boolean isAlleyFree() {
        return !availableAlleys.isEmpty();
    }

    /**
     * Here we need a synchronized. The only calling method is already synchronized, however
     * it can be called from different instances of BowlingAlley. Therefore we need to guarantee
     * here that shared variables {@code occupiedAlleys} and {@code availableAlleys} are modified
     * by only one Thread at a time.
     *
     * @param releasedAlley the BowlingAlley object on which a game just ended
     */
    // Notification of a BowlingAlley that a Group just finished playing on it.
    public synchronized void gameEnded(BowlingAlley releasedAlley) {
        occupiedAlleys.remove(releasedAlley);
        availableAlleys.add(releasedAlley);

        System.out.println("(BowlingArea): A bowling game ended. Available BowlingAlleys now: " + availableAlleys.size() + "/" + NUM_ALLEYS);

        // Notify DancingRoom that game ended.
        dancingRoom.gameEnded();
    }
}

package stations;

import java.util.HashSet;
import java.util.Set;

/**
 * A BowlingArea has no direct interaction with the Clients. It is only responsible to inform
 * the DancingRoom that a BowlingAlley is free. Clients then work directly with the passed
 * object of a BowlingAlley.
 */
public class BowlingArea {
    /**
     * Total number of BowlingAlleys the BowlingArea consists of.
     */
    public static int NUM_ALLEYS = 3;

    /**
     * Reference to the DancingRoom to inform him that BowlingAlley is free'd up after
     * a Group finished playing.
     */
    DancingRoom dancingRoom;

    /**
     * This set keep track of the available BowlingAlleys.
     * <p>
     * A 'counting semaphore' may be used instead (not tested, just an idea)
     * but our implementation keeps track of available BowlingAlleys
     * via this set.
     */
    Set<BowlingAlley> availableAlleys;

    /**
     * Construct BowlingArea with NUM_ALLEYS BowlingAlleys.
     */
    public BowlingArea(DancingRoom dancingRoom) {
        this.dancingRoom = dancingRoom;

        availableAlleys = new HashSet<>();

        for (int i = 0; i < NUM_ALLEYS; i++) {
            availableAlleys.add(new BowlingAlley(i, this));
        }
    }

    /**
     * Must be {@code synchronized} because up to NUM_ALLEYS Clients may want to register for a BowlingAlley
     * to the same time. Since this method modifies variables, only one Client can enter this method
     * at any given time.
     *
     * @return free BowlingAlley on which Client's Group can play on
     */
    public synchronized BowlingAlley getFreeAlley() {
        /**
         * When calling getFreeAlley(), it is assumed that a free alley exists. The
         * caller (DanceRoom) must check for a free alley with method BowlingArea.isAlleyFree().
         */
        assert !availableAlleys.isEmpty();

        BowlingAlley freeAlley = availableAlleys.iterator().next();
        availableAlleys.remove(freeAlley);

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
     * Here we need a {@code synchronized}. The only calling method is already {@code synchronized},
     * however it can be called from _different instances_ of BowlingAlley. Therefore we need
     * to guarantee here that shared variables {@code occupiedAlleys} and {@code availableAlleys}
     * are modified by only one Thread at a time.
     *
     * @param releasedAlley the BowlingAlley object on which a game just ended
     */
    public synchronized void gameEnded(BowlingAlley releasedAlley) {
        availableAlleys.add(releasedAlley);

        System.out.println("(BowlingArea): A bowling game ended. Available BowlingAlleys now: " + availableAlleys.size() + "/" + NUM_ALLEYS);

        /** Notify DancingRoom that game has ended. */
        dancingRoom.gameEnded();
    }
}

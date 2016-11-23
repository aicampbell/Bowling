package resources;

import actors.Client;
import utils.Group;
import utils.GroupSynchronizer;

/**
 * Created by mo on 17.11.16.
 */
public class DancingRoom extends GroupSynchronizer {
    private BowlingArea bowlingArea;

    public DancingRoom() {
        super();
        bowlingArea = new BowlingArea(this);
    }

    public synchronized BowlingAlley danceAndRequestAlley(Client client) {
        // dance
        super.waitForWholeGroup(client);

        Group group = client.getGroup();

        /**
         * If Client's Group already has a BowlingAlley assigned, skip the while() and return the BowlingAlley.
         * If there is no BowlingAlley assigned, we first check if there is a BowlingAlley free. If not we wait.
         * If there is a BowlingAlley free, we reserve that for the Group and return it.
         * When a game ended (see below), all waiting threads are woken up and one Client will take the
         * lock of this method. This one will book the BowlingAlley that just free'd up for his Group. All other
         * Clients in his Group that are woken up, will eventually get the lock too and see in the while() that
         * a BowlingAlley has already booked for their team. With that they won't re-request a BowlingAlley.
         */
        while (!group.hasAlleyAssigned()) {
            if (!bowlingArea.isAlleyFree()) {
                try {
                    wait(); // dance even more
                } catch (InterruptedException e) {
                }
            } else {
                BowlingAlley freeAlley = bowlingArea.getFreeAlley();
                group.setBowlingAlley(freeAlley);
            }
        }
        return group.getBowlingAlley();
    }

    public synchronized void gameEnded() {
        notifyAll();
    }
}

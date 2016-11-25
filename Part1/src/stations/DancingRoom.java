package stations;

import actors.Client;
import utils.Group;
import utils.GroupSynchronizer;

/**
 * DancingRoom is a room every Client has to pass. It is entered after the ShoesRoom
 * and before going to a BowlingAlley
 *
 * It makes use of Group synchronization by extending {@link GroupSynchronizer}.
 */
public class DancingRoom extends GroupSynchronizer {
    /** BowlingArea which notifies DancingRoom about newly released BowlingAlleys. */
    private BowlingArea bowlingArea;

    public DancingRoom() {
        super();
        bowlingArea = new BowlingArea(this);
    }

    /**
     * Every Client has to wait for his Group in the DancingRoom and dance. Must be
     * {@code synchronized} because shared instance variables are accessed in this method.
     *
     * @param client Client that enters DancingRoom.
     * @return the BowlingAlley that Client eventually got assigned to.
     */
    public synchronized BowlingAlley danceAndRequestAlley(Client client) {
        System.out.print("Client(" + client.getId() + ") arrived in DancingRoom. ");

        /**
         * Check if Client's Group is complete now.
         * If not, wait for the remaining Clients for that Group.
         * If yes, go on with the whole Group.
         */
        super.waitForWholeGroup(client);

        Group group = client.getGroup();

        /**
         * If Client's Group already has a BowlingAlley assigned, skip the while() and return the BowlingAlley.
         * If there is no BowlingAlley assigned, we first check if there is a BowlingAlley free. If not we wait.
         * If there is a BowlingAlley free, we reserve that for the Group and return it.
         * When a game ended (see below), all waiting threads are woken up and one Client will take the
         * lock of this method. This first Client will book the BowlingAlley that just free'd up for his Group.
         * All other Clients in his Group that are woken up, will eventually get the lock too and
         * see in the while() that a BowlingAlley has already booked for their team. With that they
         * won't re-request a BowlingAlley. Therefore, the re-checking of the condition is essential and an if()
         * is not enough here. Because of a very similar reason we have to put an if() inside the while() (and not
         * a second while()) so that the first step for each woken up Client is that he checks if his Group has
         * already a BowlingAlley assigned.
         */
        while (!group.hasAlleyAssigned()) {
            if (!bowlingArea.isAlleyFree()) {
                System.out.println("Client(" + client.getId() + ") is disappointed because no BowlingAlley is free :(.");
                try {
                    /** Dance... */
                    wait();
                } catch (InterruptedException e) {
                }

                System.out.println("Client(" + client.getId() + ") is hyped about a free BowlingAlley -- Trying to get it!");
            } else {
                BowlingAlley freeAlley = bowlingArea.getFreeAlley();
                group.setBowlingAlley(freeAlley);
            }
        }

        System.out.println("Client(" + client.getId() + ") in Group(" + group.getId() + ") can play on BowlingAlley(" + group.getBowlingAlley().getId() + ").");

        return group.getBowlingAlley();
    }

    /**
     * This method is called from {@code bowlingArea} that informs about a released/free BowlingAlley.
     * {@code notify()} alone would be enough to ensure that the selection is anarchic/random.
     * However, we also need to make sure that the other Clients in the Group of the selected Client
     * are woken up in order to advance to the BowlingAlley. Therefore, a {@code notifyAll()} is needed.
     *
     * {@code notifyAll()} must be called from inside a {@code synchronized} block. That's why we add the
     * keyword {@code synchronized} to the method.
     */
    public synchronized void gameEnded() {
        System.out.println("DancingRoom got notified that a BowlingAlley just got free!");
        notifyAll();
    }
}

package stations;

import actors.Client;
import utils.Group;
import utils.GroupSynchronizer;

import java.util.ArrayList;
import java.util.List;

/**
 * DancingRoom is a room every Client has to pass. It is entered after the ShoesRoom
 * and before going to a BowlingAlley
 * <p>
 * It makes use of Group synchronization by extending {@link GroupSynchronizer}.
 */
public class DancingRoom extends GroupSynchronizer {
    /**
     * BowlingArea which notifies DancingRoom about newly released BowlingAlleys.
     */
    private BowlingArea bowlingArea;

    /**
     * List of arrived Groups. Order is important because we want to provide a priority
     * of Groups in Part 2.
     */
    private List<Group> arrivedGroups;

    public DancingRoom() {
        super();
        bowlingArea = new BowlingArea(this);
        arrivedGroups = new ArrayList<>();
    }

    /**
     * Every Client has to wait for his Group in the DancingRoom and dance. Must be
     * {@code synchronized} because shared instance variables are accessed in this method.
     *
     * @param client Client that enters DancingRoom.
     * @return the BowlingAlley that Client eventually got assigned to.
     */
    public synchronized BowlingAlley danceAndRequestAlley(Client client) {
        System.out.println("Client(" + client.getId() + ") arrived in DancingRoom.");
        Group group = client.getGroup();

        /**
         * If the Client's Group hasn't been seen until now in the DanceRoom, add it to the
         * list {@code arrivedGroups}. This is used to determine which Group is allowed to play
         * next based on the order of arrival (FIFO).
         */
        updateGroupOrder(group);

        /**
         * Check if Client's Group is complete now.
         * If not, wait for the remaining Clients for that Group.
         * If yes, go on with the whole Group.
         */
        super.waitForWholeGroup(client);

        /**
         * If Client's Group already has a BowlingAlley assigned, skip the while() and return the BowlingAlley.
         * If there is no BowlingAlley assigned, we first check if the Group is the next one to have access
         * and if there is a BowlingAlley free. If not we wait.
         *
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
            if (!isGroupNext(group) || !bowlingArea.isAlleyFree()) {
                System.out.println("Client(" + client.getId() + ") is disappointed because no BowlingAlley is free or another Group has priority.");
                try {
                    /** Dance... */
                    wait();
                } catch (InterruptedException e) {
                }

                System.out.println("Client(" + client.getId() + ") is hyped about a free BowlingAlley -- Trying to get it!");
            } else {
                BowlingAlley freeAlley = bowlingArea.getFreeAlley();
                group.setBowlingAlley(freeAlley);

                /** It is important to remove the Group who got access from {@code arrivedGroups}. */
                removeGroupFromArrivedGroups(group);
            }
        }

        System.out.println("Client(" + client.getId() + ") in Group(" + group.getId() + ") can play on BowlingAlley(" + group.getBowlingAlley().getId() + ").");

        return group.getBowlingAlley();
    }

    /**
     * Checks if Client's Group is already registered in the DanceRoom. If yes, do nothing.
     * If not, add this Group to the list {@code arrivedGroups}. It is important that a Group
     * is not added twice (uniqueness property is maintained).
     *
     * @param group that might be added to the list of arrived Groups.
     */
    private synchronized void updateGroupOrder(Group group) {
        boolean clientBelongsToNewGroup = true;
        for (Group arrivedGroup : arrivedGroups) {
            if (arrivedGroup.getId() == group.getId()) {
                clientBelongsToNewGroup = false;
            }
        }
        if (clientBelongsToNewGroup) {
            arrivedGroups.add(group);
        }
    }

    /**
     * Checks if a Group is given the next free BowlingAlley. Is {@code synchronized} because
     * calling method is {@code synchronized} and we also work on a shared variable here.
     *
     * @param group to be checked
     * @return true if Group is next. False if not next.
     */
    private synchronized boolean isGroupNext(Group group) {
        /**
         * No need to check for null because this method is only called when {@code arrivedGroups.size() > 0}.
         * Therefore, {@code arrivedGroups.get(0)} always returns a valid Group which is not null.
         */
        return arrivedGroups.get(0).getId() == group.getId();
    }

    /**
     * After a Group got a BowlingAlley, we need to make sure that this Group is removed from the list
     * {@code arrivedGroups}. This method is called from every Client in a Group that got access. That's why
     * we need to make sure that the first Group in the list is only removed, if it's the Group of the Client
     * invoking the method.
     *
     * @param group to be removed from the list
     */
    private synchronized void removeGroupFromArrivedGroups(Group group) {
        Group nextGroup = arrivedGroups.get(0);

        /** Here a null-check is needed because every Client is able to remove the entry. Therefore it can
         * be the case that {@code arrivedGroups} is empty.
         */
        if (nextGroup != null && nextGroup.getId() == group.getId()) {
            arrivedGroups.remove(0);
        }
    }

    /**
     * This method is called from {@code bowlingArea} that informs about a released/free BowlingAlley.
     * {@code notify()} alone would be enough to ensure that the selection is anarchic/random.
     * However, we also need to make sure that the other Clients in the Group of the selected Client
     * are woken up in order to advance to the BowlingAlley. Therefore, a {@code notifyAll()} is needed.
     * <p>
     * {@code notifyAll()} must be called from inside a {@code synchronized} block. That's why we add the
     * keyword {@code synchronized} to the method.
     */
    public synchronized void gameEnded() {
        System.out.println("DancingRoom got notified that a BowlingAlley just got free!");
        notifyAll();
    }
}

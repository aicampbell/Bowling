package stations;

import actors.Client;
import utils.Group;
import utils.GroupSynchronizer;
import utils.ShoePair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * ShoesRoom is a room every Client has to pass. It is entered after the RegistrationDesk
 * and before the DancingRoom.
 *
 * It makes use of Group synchronization by extending {@link GroupSynchronizer}.
 */
public class ShoesRoom extends GroupSynchronizer {
    public static int MAX_SHOES = Group.MAX_SIZE * BowlingArea.NUM_ALLEYS;

    /**
     * A set of ShoePairs which are available for borrowers.
     */
    private Set<ShoePair> availableShoes;

    /**
     * A helper monitor (beside an instance of this class) which is
     * used as 2nd monitor to put Clients depending on their type
     * (borrower or returner) in waiting state on different monitors.
     */
    private ReturnerMonitor returnerMonitor;

    /**
     * Reveals if the employee is currently available or not. Is used to provide
     * mutual exclusion for clients (borrowers and returners to be more specific).
     */
    private boolean isEmployeeFree;

    /**
     * Volatile so accesses are guaranteed to be correct.
     * This is needed because this value is modified outside of a
     * {@code synchronized} method ({@link ShoesRoom#requestBorrowingShoes(Client)}).
     */
    private volatile int numReturnersWaiting = 0;

    /**
     * Data structure to keep track of already served Groups and the number of served
     * Clients (borrowers) in each Group.
     */
    private Map<Group, Integer> servedBorrowerGroups;

    /**
     * Inner class to provide a second monitor object on which Client-threads
     * can be locked.
     */
    public class ReturnerMonitor {
        /**
         * Place a returner in the waiting queue on this monitor.
         *
         * @param returner to be put in wait state.
         */
        public synchronized void enqueueReturner(Client returner) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }

        /**
         * Notifies one waiting returner that he can release the lock and release
         * the monitor. Woken up thread got previously put in waiting state in
         * {@code enqueueReturner()}. When he is woken up, no more code in this
         * class is executed, he just leaves the method, returns to the calling
         * method (inside ShoesRoom.class), and releases this monitor.
         */
        public synchronized void wakeOneReturnerUp() {
            notify();
        }
    }

    public ShoesRoom() {
        super();
        availableShoes = new HashSet<>();
        returnerMonitor = new ReturnerMonitor();
        servedBorrowerGroups = new HashMap<>();
        isEmployeeFree = true;

        for(int i = 0; i < MAX_SHOES; i++) {
            availableShoes.add(new ShoePair());
        }
    }

    /**
     * Entry-method for every borrower. Since both called methods inside are synchronized,
     * it might be more efficient to makes this method {@code synchronized} too. It is
     * okay though, to not use {@code synchronized} for this method.
     *
     * This method logically separates the two steps (1) borrowing shoes and (2) waiting
     * for his Group.
     *
     * @param client who wants to borrow shoes (borrower)
     */
    public void requestBorrowingShoes(Client client) {
        borrowShoes(client);
        super.waitForWholeGroup(client);
    }

    /**
     * Entry-method for every returner. This is not synchronized because we need to make sure
     * that a returner can signal immediately that he arrived so he gets priority as fast as
     * possible (this is done by incrementing {@code numReturnersWaiting} which is checked
     * in other methods).
     *
     * The other method {@link ShoesRoom#returnShoes(Client)} is {@code synchronized} though.
     *
     * @param client who wants to return shoes (returner)
     */
    public void requestReturningShoes(Client client) {
        /** Increment to announce the arrival of this returner. Is done here (outside of a
         * {@code synchronized} method) so the announcement is made fast.
         *
         * When a returner X is done returning shoes in {@link ShoesRoom#returnShoes(Client)}
         * and he notifies either a borrower or a returner, it is crucial that a potentially
         * waiting returner Y incremented this variable. And not that Y can't enter
         * {@link ShoesRoom#returnShoes(Client)} because it's {@code synchronized}
         * and currently occupied by X. If that happens, a borrower might be woken up and the
         * rule of given priority to returners is broken.
         *
         * So the goal is to announce Y as early as possible.
         */
        numReturnersWaiting++;

        /** This one is {@code synchronized}. */
        returnShoes(client);
    }

    /**
     * Not {@code synchronized} since instance variables are not touched here. Only the call
     * to {@link GroupSynchronizer#waitForWholeGroup(Client)} is synchronized.
     */
    private synchronized void borrowShoes(Client client) {
        System.out.println("---Client(" + client.getId() + ") wants to borrow shoes.");
        Group group = client.getGroup();

        /**
         * WITH prioritizing served Groups:
         * [waiting condition: !isEmployeeFree || numReturnersWaiting > 0 || !isShoePairAvailable() ||
         (!servedBorrowerGroups.isEmpty() && !servedBorrowerGroups.containsKey(group)) ]
         * --------------------------------
         * We need a while() now because multiple borrowers might be woken up when respecting the
         * priority-feature of served Groups.
         *
         * We check {@code servedBorrowerGroups} to keep track of the Groups which are partially served.
         *
         * Altogether, there can be 4 reasons now why a borrower has to wait.
         */
        while(!isEmployeeFree || numReturnersWaiting > 0 || !isShoePairAvailable() ||
                (!servedBorrowerGroups.isEmpty() && !servedBorrowerGroups.containsKey(group))) {

            System.out.println("---Client(" + client.getId() + ") has to wait for the employee or returners or another Group has priority or shoes (" + availableShoes.size() + "/" + MAX_SHOES + ") are insufficient.");

            try {
                wait();
            } catch (InterruptedException e) {
            }
        }

        /** The employee won't be available while serving this borrower. */
        isEmployeeFree = false;

        System.out.println("---Client(" + client.getId() + ") can borrow shoes(" + availableShoes.size() + "/" + MAX_SHOES + ") now! (soon -1 !)");

        /**
         * At this point, Borrower _can_ borrow shoes, he escaped the waiting condition from above.
         * Therefore we can add him to the set {@code servedBorrowerGroups} which contains
         * all partially or fully handled Groups.
         *
         * Increment the client count for a Group in the map {@code servedBorrowerGroups}. If this Group
         * is completely processed, remove this Group from the map. This removal is important - it is
         * not only there for memory optimization. It is needed in the while-condition above.
         */
        int newCount = servedBorrowerGroups.containsKey(group) ? servedBorrowerGroups.get(group) + 1 : 1;
        if(newCount < Group.MAX_SIZE) {
            servedBorrowerGroups.put(group, newCount);
        } else {
            servedBorrowerGroups.remove(group);
        }

        /**
         * As stated in the text: We need to make sure that we give every Client a
         * separate ShoePair. Since the supply of shoes is infinite, we give each
         * Client a 'new' pair of shoes.
         */
        client.borrowShoes(getShoePair());

        /** Borrowing shoes takes some time... */
        client.waitInShoesRoom();

        /** The employee won't be available while serving this returner. */
        isEmployeeFree = true;

        /**
         * Notify a returner if there is at least one waiting.
         *
         * If there is no borrower waiting, we notify this monitor in order to wake up
         * another potentially waiting borrower. This ensures that Borrowers are
         * handled one after another. Returners are waiting on a different monitor
         * ({@code returnerMonitor}) so the only type of Client we can wake up here are
         * Borrowers.
         */
        if(numReturnersWaiting > 0) {
            returnerMonitor.wakeOneReturnerUp();
        } else if(isShoePairAvailable()){
            /**
             * Without prioritizing already served Groups, this can be a {@code notify()}. With this
             * additional requirement however, we have to look through all waiting borrowers to determine
             * which one we allow to borrow shoes (done at the beginning of this method).
             */
            notifyAll();
        }
    }

    /**
     * Clients return their shoes here. Their ShoePair gets added to {@code availableShoes}.
     * Is {@code synchronized} because shared instance variables are touched here.
     */
    private synchronized void returnShoes(Client client) {
        System.out.println("---Client(" + client.getId() + ") returns his shoes now. He's done for today and goes home.");

        /**
         * An if() is enough here since only one returner is woken up. For him it is guaranteed that
         * the employee is free since he got woken up upon the event meaning that the employee is free now.
         * Since the woken-up returner enjoys priority over every other thread, he doesn't have to recheck
         * the condition.
         */
        if(!isEmployeeFree) {
            /**
             * Alternative place of incrementing {@code numReturnersWaiting}. Not used because of
             * the reason explained above in {@link ShoesRoom#requestReturningShoes(Client)}.
             */
            //numReturnersWaiting++;

            /** Let the returner wait on the helper monitor {@code returnerMonitor}. */
            returnerMonitor.enqueueReturner(client);
        }

        /** It's the returners turn so the waiting number of returned decreased by 1. */
        numReturnersWaiting--;

        /** The employee won't be available while serving this returner. */
        isEmployeeFree = false;

        /** Client returns ShoePair which are added to {@code availableShoes} again. */
        availableShoes.add(client.returnShoes());
        client.forgetShoes();

        /** Returning shoes takes some time... */
        client.waitInShoesRoom();

        /** Returner is served thus the employee is available again. */
        isEmployeeFree = true;

        /**
         * If there is at least one more returner waiting, we wake 1 returner up.
         * If not, we wake up a borrower (realizes priority for returners over
         * borrowers).
         *
         * We could count the number of waiting borrowers in a variable like numBorrowersWaiting
         * to check if we really need to run the {@code notifyAll()}. This is analogous to the
         * implementation of {@code numReturnersWaiting} and thus considered trivial ;)
         */
        if(numReturnersWaiting > 0) {
            System.out.println("---Client(" + client.getId() + ") finished returning shoes. However there are another " + numReturnersWaiting + " returner(s) waiting. We notify one returner.");
            returnerMonitor.wakeOneReturnerUp();
        } else {
            /**
             * WITHOUT respecting already served Groups:
             * -----------------------------------------
             * Use {@code notify()}. We don't have to check for availableShoes because we
             * just returned one. So it's sure that there is at least one shoe pair available
             * for a waiter on this monitor (=Borrower) to grab.
             *
             * WITH respecting already served Groups:
             * --------------------------------------
             * Use {@code notifyAll()}. With this additional requirement, we have to look through
             * all waiting borrowers to determine which one we allow to borrow shoes (done at
             * the beginning of this method by checking {@code servedBorrowerGroups).
             */
            notifyAll();
        }
    }

    /**
     * Checks if ShoePair is available or not.
     *
     * @return true if ShoePair is available. False if not.
     */
    private synchronized boolean isShoePairAvailable() {
        return !availableShoes.isEmpty();
    }

    /**
     * Returns a free shoe pair of the set {@code availableShoes}.
     * Removes the returned value from the set.
     *
     * @return a ShoePair object from {@code availableShoes}
     */
    private synchronized ShoePair getShoePair() {
        assert !availableShoes.isEmpty();
        ShoePair chosen = availableShoes.iterator().next();
        availableShoes.remove(chosen);
        return chosen;
    }
}

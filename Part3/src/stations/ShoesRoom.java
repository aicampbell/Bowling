package stations;

import actors.Client;
import utils.Group;
import utils.GroupSynchronizer;
import utils.ShoePair;

import java.util.HashSet;
import java.util.Set;

/**
 * ShoesRoom is a room every Client has to pass. It is entered after the RegistrationDesk
 * and before the DancingRoom.
 *
 * It makes use of Group synchronization by extending {@link GroupSynchronizer}.
 */
public class ShoesRoom extends GroupSynchronizer {
    private int MAX_SHOES = Group.MAX_SIZE * BowlingArea.NUM_ALLEYS;

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
     * Volatile so read-accesses are guaranteed to read the most recent value.
     * This is needed because this value is checked and modified outside of
     * {@code synchronized} methods.
     */
    private volatile int numReturners = 0;

    private Set<Client> waitingBorrowers;
    private Set<Group> servedBorrowerGroups;

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
        waitingBorrowers = new HashSet<>();
        servedBorrowerGroups = new HashSet<>();

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
    public synchronized void requestBorrowingShoes(Client client) {
        borrowShoes(client);
        super.waitForWholeGroup(client);
    }

    /**
     * Entry-method for every returner. This is not synchronized because we need to make sure
     * that a returner can signal immediately that he arrived so he gets priority as fast as
     * possible (this is done by {@code numReturners} which is checked in other methods).
     *
     * The method {@code returnShoes(Client)} must be {@code synchronized} again because
     * mutual exclusion is required between borrowing and returning shoes (only 1 employee
     * available who can server either of them at a time.). Also this method works on shared
     * variables.
     *
     * @param client who wants to return shoes (returner)
     */
    public void requestReturningShoes(Client client) {
        numReturners++;

        if(numReturners > 1) {
            returnerMonitor.enqueueReturner(client);
        }
        returnShoes(client); // synchronized
    }

    /**
     * Not {@code synchronized} since instance variables are not touched here. Only the call
     * to {@link GroupSynchronizer#waitForWholeGroup(Client)} is synchronized.
     */
    private synchronized void borrowShoes(Client client) {
        System.out.println("---Client(" + client.getId() + ") wants to borrow his shoes.");
        Group group = client.getGroup();

        /**
         * WITHOUT prioritizing served Groups: [waiting condition: numReturners > 0 || !isShoePairAvailable()) ]
         * -----------------------------------
         * Two possibilities here:
         *
         * while() because we have to recheck if a ShoePair is available. This re-check
         * is needed because the corresponding notify() (at the end of this method) is
         * optimistic and doesn't check for isShoePairAvailable(). The woken up borrower
         * however is only allowed to proceed if there is a ShoePair available.
         *
         * As an alternative, we can place an if() here and check for isShoePairAvailable()
         * at the end of this method before calling notify(). This makes sure that a borrower
         * is only woken up if there is an available ShoePair. This is more efficient since
         * the scenario of waking up a borrower who checks the condition and wait()s again,
         * is avoided (redundant notify() avoided).
         *
         *
         * WITH prioritizing served Groups: [waiting condition: numReturners > 0 || !isShoePairAvailable() ||
         !(servedBorrowerGroups.contains(group) || waitingBorrowers.isEmpty()) ]
         * --------------------------------
         * We track the waiting borrowers right before and right after the {@code wait()}. After the while(),
         * we also track the Groups which are already served (partially or fully).
         * With these 2 new kinds of information, we can extend the waiting condition.
         *
         * The condition extension reads like: "IF borrower is in a served Group OR he's the only
         * remaining waiting burrower who's Group hasn't been served yet, THEN escape the while-loop and go on."
         */
        while(numReturners > 0 || !isShoePairAvailable() ||
                !(servedBorrowerGroups.contains(group) || waitingBorrowers.isEmpty())) {
            System.out.println("---Client(" + client.getId() + ") has to wait for returners or shoes (" + availableShoes.size() + "/" + MAX_SHOES + ").");

            /** Register every waiting borrower in {@code waitingBorrowers}. */
            waitingBorrowers.add(client);
            try {
                wait();
            } catch (InterruptedException e) {
            }
            /** A notifyAll() got invoked (there is no single notify() on this monitor anymore). */
            waitingBorrowers.remove(client);
        }
        System.out.println("---Client(" + client.getId() + ") can borrow shoes(" + availableShoes.size() + "/" + MAX_SHOES + ") now! (soon -1 !)");

        /**
         * At this point, Borrower _can_ borrow shoes, he escaped the waiting condition from above.
         * Therefore we can add him to the set {@code servedBorrowerGroups} which contains
         * all partially or fully handled Groups.
         *
         * This can be made more memory-efficient by distinguishing between partially and fully handled
         * Groups so that fully handled Groups are removed from this set again to save memory (just
         * a suggestion for improvement. Not implemented because purpose of all this is synchronization...).
         */
        servedBorrowerGroups.add(group);

        /**
         * As stated in the text: We need to make sure that we give every Client a
         * separate ShoePair. Since the supply of shoes is infinite, we give each
         * Client a 'new' pair of shoes.
         */
        client.borrowShoes(getShoePair());

        /** Borrowing shoes takes some time... */
        client.waitInShoesRoom();

        /**
         * Notify a returner if there is at least one waiting.
         *
         * If there is no borrower waiting, we notify this monitor in order to wake up
         * another potentially waiting borrower. This ensures that Borrowers are
         * handled one after another. Returners are waiting on a different monitor
         * ({@code returnerMonitor}) so the only type of Client we can wake up here are
         * Borrowers.
         */
        if(numReturners > 0) {
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
     * Not synchronized since instance variables are not touched here.
     */
    private synchronized void returnShoes(Client client) {
        System.out.println("---Client(" + client.getId() + ") returns his shoes now. He's done for today and goes home.");

        /** Client returns ShoePair which are added to {@code availableShoes} again. */
        availableShoes.add(client.returnShoes());
        client.forgetShoes();

        /** Returning shoes takes some time... */
        client.waitInShoesRoom();

        /**
         * We decrement {@code numReturners} because this returner is done now. He only
         * might inform others now that he's finished. If there is a returner waiting, we
         * wake him up. If not we wake up a Borrower (realizes priority for returners over
         * borrowers).
         */
        numReturners--;
        if(numReturners > 0) {
            System.out.println("---Client(" + client.getId() + ") finished returning shoes. However there are " + numReturners + " returners waiting. We notify one returner.");
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
             * the beginning of this method).
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

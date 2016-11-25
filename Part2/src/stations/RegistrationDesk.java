package stations;

import actors.Client;
import utils.Group;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * RegistrationDesk is a room every Client has to pass. It is entered as a first step
 * so that the Client can register himself and gets assigned to a Group with which
 * he will play a bowling game.
 */
public class RegistrationDesk {
    private static int NUM_CASHIERS = 3;

    /**
     * This list contains all Groups that are complete.
     */
    private List<Group> fullGroups;

    /**
     * This list contains all Clients that are registered.
     */
    private List<Client> clients;

    /**
     * This counter is used to initialize new Groups. It is incremented every
     * time a new Group is created.
     */
    private int groupCounter = 1;

    /**
     * This object holds the only incomplete Group that exists at any given point of time.
     * When this Group is filled, it is moved to {@code fullGroups} and this variable is
     * renewed.
     */
    private Group currentGroup;

    private Set<Cashier> availableCashiers;

    public RegistrationDesk() {
        fullGroups = new ArrayList<>();
        clients = new ArrayList<>();
        availableCashiers = new HashSet<>();

        for (int i = 0; i < NUM_CASHIERS; i++) {
            availableCashiers.add(new Cashier(i, this));
        }
    }

    /**
     * Top-level method that is invoked when Client wants to register.
     * Only methods {@code getCashier} and {@code waitForCompleteGroup} are
     * {@code synchronized} because they work with shared variables.
     * <p>
     * With this split in multiple methods, we make sure that the monitor on
     * RegistrationDesk and the monitor on a Cashier instance are used
     * the most efficiently (somewhat independent of each other).
     *
     * @param client Client that wants to register
     */
    public void register(Client client) {
        Cashier cashier = getCashier(client);
        cashier.register(client);

        waitForCompleteGroup(client);
    }

    /**
     * Top-level method that is invoked when Client wants to pay.
     * It's analogous to {@link RegistrationDesk#register(Client)}.
     *
     * @param client Client that wants to pay
     */
    public void chargeFee(Client client) {
        Cashier cashier = getCashier(client);
        cashier.chargeFee(client);
    }

    /**
     * Returns an object of a free Cashier once it is free.
     *
     * @param client Client who wants to get to the Cashier
     * @return free Cashier instance
     */
    public synchronized Cashier getCashier(Client client) {
        /**
         * Here we need to make sure that the condition is re-checked once
         * every waiting Customer got notified that
         */
        while (!isCashierFree()) {
            try {
                System.out.println("Client(" + client.getId() + ") has to wait because no Cashier is free.");
                wait();
            } catch (InterruptedException e) {
            }
        }
        /**
         * When a Client can leave the while() loop, it means that there is an
         * free Cashier at which the Client can register.
         */
        return getFreeCashier();
    }

    /**
     * Method in which Clients wait until their Group is complete.
     *
     * @param client Client who awaits his Group
     */
    public synchronized void waitForCompleteGroup(Client client) {
        /** Get a reference to the assigned Group. */
        Group group = client.getGroup();

        /**
         * When Group is full, notify all Group members ({@code notifyAll()})
         * that they can advance. If Group is not full, {@code wait()} until last
         * arriving Client of a Group arrives and wakes up waiting Clients.
         *
         * Also for Part 2, {@code notifyAll()} because we still need to make sure
         * that all Clients of a Group are woken up. However we need to replace the
         * if() with a while() to recheck the condition. This is because the wake-up
         * might come from a Cashier who just got free (while the Group is still
         * incomplete --> recheck condition).
         *
         * In order to only allow one {@code notifyAll()} for signalling that a Group
         * is complete, we need to place a if() before the while() as shown below. If we place
         * the {@code notifyAll()} after the {@code wait()} inside the while(), it also
         * works but will be much more inefficient because there are Group.size()-1
         * redundant {@code notifyAll()} executed (by every waiting Client in Group)
         * which is generated overhead. Every woken up Client by these redundant
         * {@code notifyAll}s will have to wait again since neither his Group is complete
         * nor a Cashier is guaranteed to be free.
         */
        if (group.isFull()) {
            System.out.println("Group(" + group.getId() + ") is complete!");
            notifyAll();
        }
        while (!group.isFull()) {
            System.out.println("Group(" + group.getId() + ") isn't complete yet.");
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
    }

    /**
     * Must be {@code synchronized} because up to NUM_ALLEYS Clients may want to register for a BowlingAlley
     * to the same time. Since this method modifies variables, only one Client can enter this method
     * at any given time.
     *
     * @return free BowlingAlley on which Client's Group can play on
     */
    private synchronized Cashier getFreeCashier() {
        /**
         * When calling getFreeCashier(), it is assumed that a free cashier exists. The
         * caller of this method must check for a free cashier before
         * (see {@link RegistrationDesk#register(Client)}.
         */
        assert !availableCashiers.isEmpty();

        Cashier freeCashier = availableCashiers.iterator().next();
        availableCashiers.remove(freeCashier);

        System.out.println("(RegistrationDesk): A free Cashier just got assigned to a Client. Available Cashiers now: " + availableCashiers.size() + "/" + NUM_CASHIERS);

        return freeCashier;
    }

    /**
     * A convenience helper method to check if there is at least one free Cashier.
     * Is {@code synchronized} because caller method is synchronized itself.
     *
     * @return true if there is at least one free Cashier. False otherwise.
     */
    public synchronized boolean isCashierFree() {
        return !availableCashiers.isEmpty();
    }

    /**
     * Returns the Group that an arriving Client got assigned to. Is {@code synchronized}
     * to show that this method works with shared variables. The only calling method
     * however is already synchronized itself which makes this keyword here technically
     * redundant.
     *
     * @return the assigned Group object.
     */
    public synchronized Group getAssignedGroupForClient() {
        if (currentGroup == null || currentGroup.isFull()) {
            currentGroup = new Group(groupCounter);
        }

        /** Add a Client to this not-null and not-full Group (see previous code). */
        currentGroup.addClient();

        /**
         * If Group is full with last Client, place it in {@code fullGroups} and
         * increment {@code groupCounter}.
         */
        if (currentGroup.isFull()) {
            fullGroups.add(currentGroup);
            groupCounter++;
        }

        return currentGroup;
    }

    /**
     * Invoked by a Cashier. Must be {@synchronized} because there might be multiple
     * Cashiers and because they work on the shared variable {@code clients}.
     *
     * @param client
     */
    public synchronized void addClient(Client client) {
        clients.add(client);
    }

    /**
     * Invoked by a Cashier. Must be {@synchronized} because there might be multiple
     * Cashiers and because they work on the shared variable {@code clients}.
     *
     * @param client
     */
    public synchronized void removeClient(Client client) {
        clients.remove(client);
    }

    /**
     * Is called from a Cashier and informs the RegistrationDesk that he is now available
     * to process the next Client.
     *
     * @param cashier Cashier that just got free.
     */
    public synchronized void cashierGotAvailable(Cashier cashier) {
        System.out.println("Cashier(" + cashier.getId() + ") is free now!");
        availableCashiers.add(cashier);
        /**
         * Since one cashier can only handle one Client at a time, a {@code notify()} seems
         * sufficient. However, there might be other Clients waiting on this monitor who are
         * waiting until their Group is complete. Therefore we need to make sure that we wake
         * up the correct kind of Client (namely these Clients who wait for registering or paying).
         */
        notifyAll();
    }
}

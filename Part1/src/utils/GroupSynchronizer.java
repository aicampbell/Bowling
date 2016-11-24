package utils;

import actors.Client;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * This abstract class provides a synchronization among Clients that belong to one Group.
 * It provides this synchronization also in the presence of different Groups and Clients.
 * Extending classes (ShoesRoom, DancingRoom) only need to make a call to
 * waitForWholeGroup(Client) in order to synchronize at a certain point of code.
 *
 * This implementation is in an abstract class to reuse code.
 */
public abstract class GroupSynchronizer {
    /** Maps each Group G to a set of waiting Clients that belong to G. */
    private Map<Group, Set<Client>> groupsWaiting;

    /**
     * Groups contained in this set are considered complete.
     * Clients of this Group will be able to advance.
     */
    private Set<Group> groupsWithAccess;

    protected GroupSynchronizer() {
        groupsWaiting = new HashMap<>();
        groupsWithAccess = new HashSet<>();
    }

    /**
     * Let's a Client wait for the other Clients in his Group.
     *
     * Is {@code synchronized} because instance variables are are accessed in this method.
     */
    protected synchronized void waitForWholeGroup(Client client) {
        Group group = client.getGroup();
        System.out.print("Client(" + client.getId() + ") is waiting for his Group(" + group.getId() + ").");
        Set<Client> clientsWaiting = getWaitingClientsForGroup(group);

        /**
         * Check if Group is complete with this arriving Client
         * If it is, register Client's Group as 'having access' which means all
         * Group members can advance. All other waiting Clients are woken up
         * with a {@code notifyAll()}. We chose {@notifyAll} because there are
         * multiple Clients waiting for the Group if group size > 2.
         */
        if (clientsWaiting.size() + 1 == group.getMaxSize()) {
            System.out.print(" Now Group is complete!\n");
            groupsWithAccess.add(group);
            notifyAll();
        }

        /**
         * If Client's Group has (still) no access, make sure he's added to the waiting
         * Clients and suspend thread with {@code wait()}. If Client's Group has access,
         * its Group is complete and he can advance (leave this while()-loop).
         *
         * We need a while() instead of an if() because it is essential that a Client
         * that is woken up rechecks the condition. Since we have multiple Groups
         * in which each might be some waiting Clients, and a {@code notifyAll()}
         * as a wakup-mechanism, only the Clients which Group got access are allowed
         * to proceed (leave the while-loop). All other Clients may be woken up in the
         * same {@code notifyAll} but will be send into another {@code wait()} because
         * it was not their Group who got access.
         */
        while (!groupsWithAccess.contains(group)) {
            System.out.print(" But Group isn't complete yet.\n");
            /**
             * Add client to waiting Clients for a Group, if he's not in this set yet.
             * Note that the {@code add()} method on a Set will not change the set of the
             * Client is already in the Set ({@code add()} is idempotent).
             */
            clientsWaiting.add(client);
            groupsWaiting.put(group, clientsWaiting);

            try {
                wait();
            } catch (InterruptedException e) {
            }
        }

        /**
         * Clean data structures accordingly so information that is not required
         * anymore, is removed.
         */
        clientsWaiting.remove(client);
        if (clientsWaiting.isEmpty()) {
            groupsWaiting.remove(group);
            groupsWithAccess.remove(group);
        }
    }

    /**
     *
     * This method returns an empty set if there is no Client waiting yet for the Group
     * or a non-empty set if there is at least one other Client waiting already.
     *
     * Will be synchronized too since the only calling method is already synchronized.
     * We however indicate with {@code synchronized} that this method works on shared
     * instance variables and thus must be protected with {@code synchronized}.
     */
    private synchronized Set<Client> getWaitingClientsForGroup(Group group) {
        Set<Client> clientsWaiting = groupsWaiting.get(group);
        return clientsWaiting == null ? new HashSet<>() : clientsWaiting;
    }
}

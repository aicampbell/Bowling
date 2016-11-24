package utils;

import actors.Client;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by mo on 23.11.16.
 */
public abstract class GroupSynchronizer {
    // Associates each Group with the Group's Clients that are waiting already
    private Map<Group, Set<Client>> groupsWaiting;
    // Groups registered in this Set are complete and ready to advance.
    private Set<Group> groupsWithAccess;

    protected GroupSynchronizer() {
        groupsWaiting = new HashMap<>();
        groupsWithAccess = new HashSet<>();
    }

    /**
     * Synchronized since instance variables are are accessed in this method.
     */
    protected synchronized void waitForWholeGroup(Client client) {
        Group group = client.getGroup();
        System.out.print("Client(" + client.getId() + ") is waiting for his Group(" + group.getId() + ").");
        Set<Client> clientsWaiting = getWaitingClientsForGroup(group);

        // Check if Group is complete with this arriving Client.
        // If it is, register Client's Group as 'having access' which means all Group members can advance.
        // All other waiting Clients are woken up (notifyAll) and each will check if his/her Group has access
        // now (see following while()-loop).
        if (clientsWaiting.size() + 1 == group.getMaxSize()) {
            System.out.print(" Now Group is complete!\n");
            groupsWithAccess.add(group);
            notifyAll();
        }

        // If Client's Group has (still) no access, make sure he's added to the waiting Clients and suspend thread.
        // If Client's Group has access, its Group is complete and he can advance (leave this while()-loop).
        while (!groupsWithAccess.contains(group)) {
            System.out.print(" But Group isn't complete yet.\n");
            // Add client to waiting clients for a Group, if he's not in the Group yet.
            // The add() method on a Set will not change the set of the Client is already in the Set.
            clientsWaiting.add(client);
            groupsWaiting.put(group, clientsWaiting);

            try {
                wait();
            } catch (InterruptedException e) {
            }
        }

        // Clean data structures accordingly so information that is not required anymore is removed.
        clientsWaiting.remove(client);
        if (clientsWaiting.isEmpty()) {
            groupsWaiting.remove(group);
            groupsWithAccess.remove(group);
        }
    }

    /**
     * Will be synchronized too since the only calling method is already synchronized.
     */
    private synchronized Set<Client> getWaitingClientsForGroup(Group group) {
        Set<Client> clientsWaiting = groupsWaiting.get(group);
        return clientsWaiting == null ? new HashSet<>() : clientsWaiting;
    }
}

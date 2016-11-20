package resources;

import actors.Client;
import utils.Group;

import java.util.*;

/**
 * Created by mo on 17.11.16.
 */
public class ShoesRoom {
    // Right now: Unlimited shoes available.
    // Later: Manage stock of shoes.
    private Map<Group, Set<Client>> groupsWaiting;
    //private Group groupAccess;
    private Set<Group> groupsWithAccess;

    public ShoesRoom() {
        groupsWaiting = new HashMap<>();
        groupsWithAccess = new HashSet<>();
    }

    /**
     * Not synchronized since instance variables are not touched (so far).
     */
    public void giveShoes(Client client) {
        // borrowing shoes takes some time...
        client.waitInShoesRoom();

        // We need to make sure that we give every Client a separate ShoePair.
        // Since the supply of shoes is infinite, we give each client a 'new' pair of shoes.
        client.borrowShoes(new ShoePair());

        // Check if Client's Group is complete now.
        // If not, wait for the remaining Clients for that Group.
        // If yes, go on with the whole group
        waitForWholeGroupAndGo(client);
    }

    /**
     * Synchronized since instance variables are are accessed in this method.
     */
    private synchronized void waitForWholeGroupAndGo(Client client) {
        Group group = client.getGroup();
        Set<Client> clientsWaiting = getWaitingClientsForGroup(group);

        // Check if Group is complete with this arriving Client.
        // If it is, register Client's Group as 'having access' which means all Group members can advance.
        // All other waiting Clients are woken up and each will check if his/her Group has access
        // now (see following while()-loop).
        if (clientsWaiting.size() + 1 == group.getMaxSize()) {
            groupsWithAccess.add(group);
            notifyAll();
        }

        // If Client's Group has (still) no access, make sure he's added to the waiting Clients and suspend thread.
        // If Client's Group has access, its Group is complete and he can advance (leave this while()-loop).
        while (!groupsWithAccess.contains(group)) {
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

    private Set<Client> getWaitingClientsForGroup(Group group) {
        Set<Client> clientsWaiting = groupsWaiting.get(group);
        return clientsWaiting == null ? new HashSet<>() : clientsWaiting;
    }

    /**
     * Not synchronized since instance variables are not touched (so far).
     */
    public void returnShoes(Client client) {
        // returning shoes takes some time...
        client.waitInShoesRoom();

        client.returnShoes();
    }
}

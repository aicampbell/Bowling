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
    private Map<Integer, Set<Client>> waitingGroups;
    private int groupAccess;

    public ShoesRoom() {
        waitingGroups = new HashMap<>();
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
        Set<Client> clientsForGroup = waitingGroups.get(client.getGroupId());
        if(clientsForGroup == null) {
            clientsForGroup = new HashSet<>();
        }

        // Client cannot advance yet since his Group is incomplete
        while(client.getGroupId() != groupAccess) {
            // Group is complete with this arriving Client
            if(clientsForGroup.size() + 1 == client.getGroup().getMaxSize()) {
                groupAccess = client.getGroupId();
                notifyAll();
            }
            // Group is yet incomplete. Add client to list of clients who are already waiting.
            else {
                // Add client to waiting clients for a Group, if he's not in the Group yet.
                if(!clientsForGroup.contains(client)) {
                    clientsForGroup.add(client);
                    waitingGroups.put(client.getGroupId(), clientsForGroup);
                }
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }
        }
        // When Client-thread left the while-loop, the Client's Group is complete and all its members
        // can advance to their next common destination.
        // (leave this method...)
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

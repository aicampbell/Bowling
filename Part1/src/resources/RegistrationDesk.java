package resources;

import actors.Client;
import utils.Group;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mo on 17.11.16.
 */
public class RegistrationDesk {
    // Right now, one monitor is enough since we only need one cashier for Part 1.
    // Later, when we have multiple cashiers we have to change this.

    // whenever a Group is full, add it to this list
    private List<Group> fullGroups;
    private List<Client> clients;

    private int groupCounter = 1;

    // only one incomplete group at a time (might be a problem in Part 2 according to Damien
    private Group currentGroup;

    public RegistrationDesk() {
        fullGroups = new ArrayList<>();
        clients = new ArrayList<>();
    }

    public synchronized void register(Client client) {
        // registering takes some time...
        client.waitAtRegistrationDesk();

        // Register Clients independently of a Group
        clients.add(client);

        // Assign Client to a Group and let Client know about his Group
        Group group = getAssignedGroupForClient();
        client.setGroup(group);

        // When Group is full, notify all Group members that they can advance.
        if (group.isFull()) {
            notifyAll();
        } else {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
    }

    private synchronized Group getAssignedGroupForClient() {
        if (currentGroup == null || currentGroup.isFull()) {
            currentGroup = new Group(groupCounter);
        }

        currentGroup.addClient();

        if (currentGroup.isFull()) {
            fullGroups.add(currentGroup);
            groupCounter++;
        }

        return currentGroup;
    }

    public synchronized void pay(Client client) {
        // paying takes some time...
        client.waitAtRegistrationDesk();
    }
}

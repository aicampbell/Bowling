package resources;

import actors.Client;
import utils.Group;

import java.util.ArrayList;
import java.util.List;

import static utils.Group.MAX_SIZE;

/**
 * Created by mo on 17.11.16.
 */
public class RegistrationDesk {
    // Right now, one monitor is enough since we only need one cashier for Part 1.
    // Later, when we have multiple cashiers we have to change this.

    // whenever a Group is full, add it to this list
    private List<Group> groups;
    private List<Client> clients;

    private int groupCounter = 0;

    // only one incomplete group at a time (might be a problem in Part 2 according to Damien
    private Group incompleteGroup;

    public RegistrationDesk() {
        groups = new ArrayList<>();
        clients = new ArrayList<>();
        incompleteGroup = new Group(groupCounter);
    }

    public synchronized void register(Client client) {
        clients.add(client);

        incompleteGroup.addClient(client);
        if(incompleteGroup.isFull()) {
            groups.add(new Group(incompleteGroup));
            groupCounter++;
            incompleteGroup = new Group(groupCounter);
        }
    }

    public synchronized void payAndLeave(Client client) {

    }
}

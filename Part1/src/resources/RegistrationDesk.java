package resources;

import actors.Client;
import utils.Group;

import java.util.ArrayList;
import java.util.List;

/**
 * RegistrationDesk is a room every Client has to pass. It is entered as a first step
 * so that the Client can register himself and gets assigned to a Group with which
 * he will play a bowling game.
 */
public class RegistrationDesk {
    /** This list contains all Groups that are complete. */
    private List<Group> fullGroups;

    /** This list contains all Clients that are registered. */
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

    public RegistrationDesk() {
        fullGroups = new ArrayList<>();
        clients = new ArrayList<>();
    }

    /**
     * Every Client must register at the RegistrationDesk. Must be {@code synchronized} because
     * shared variables are used inside this method.
     *
     * @param client Client that wants to register
     */
    public synchronized void register(Client client) {
        System.out.println("Client(" + client.getId() + ") arrived at RegistrationDesk to register.");

        /** Registering takes some time... */
        client.waitAtRegistrationDesk();

        /**
         * Register the Client independent of a Group. {@code clients} is not used at another
         * place in code, might make sense though in reality to keep track of all Clients.
         */
        clients.add(client);

        /** Assign Client to a Group and let Client know about his Group. */
        Group group = getAssignedGroupForClient();
        client.setGroup(group);
        System.out.print(" He got assigned to Group(" + group.getId() + ").");

        /**
         * When Group is full, notify all Group members ({@code notifyAll()})
         * that they can advance. If Group is not full, {@code wait()} until last
         * arriving Client of a Group arrives and wakes up waiting Clients.
         */
        if (group.isFull()) {
            System.out.print(" Now Group is complete!\n");
            notifyAll();
        } else {
            System.out.print(" But Group isn't complete yet.\n");
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
    }

    /**
     * Returns the Group that an arriving Client got assigned to. Is {@code synchronized}
     * to show that this method works with shared variables. The only calling method
     * however is already synchronized itself which makes this keyword here technically
     * redundant.
     *
     * @return the assigned Group object.
     */
    private synchronized Group getAssignedGroupForClient() {
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
     * Every Client must pay at the RegistrationDesk after his bowling match is over.
     *
     * This method is {@code synchronized} because a shared variable is currently modified.
     * See last method comment for an alternative approach.
     *
     * @param client Client that has to pay
     */
    public synchronized void pay(Client client) {
        System.out.println("Client(" + client.getId() + ") returned to RegistrationDesk for paying.");

        /** Paying takes some time... */
        client.waitAtRegistrationDesk();

        /**
         * Remove Client from {@code clients}. This can be removed if variable {@code clients}
         * is supposed to even keep track of Clients after they left the venue. If it's removed
         * this method doesn't have to be {@code synchronized}.
         */
        clients.remove(client);
    }
}

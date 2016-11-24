package resources;

import actors.Client;
import utils.Group;

/**
 * Created by mo on 17.11.16.
 */
public class BowlingAlley {
    private int id;
    private int clientsReadyToPlay;

    private BowlingArea bowlingArea;

    public BowlingAlley(int id, BowlingArea bowlingArea) {
        this.id = id;
        this.bowlingArea = bowlingArea;
    }

    /**
     * We don't need to make use of GroupSynchronizer because for a BowlingAlley not more than
     * one Group can be assigned to at any point of time. There we don't need to keep track of
     * waiting Clients per Group but just can count the waiting Clients at this BowlingAlley.
     *
     * @param client every Client will call this method and will wait for other Clients in his Group
     *               if it isn't complete yet.
     */
    public synchronized void waitAtAlleyForGroup(Client client) {
        System.out.print("Client(" + client.getId() + ") is waiting for his Group(" + client.getGroup().getId() + ") at BowlingAlley(" + id + ").");
        clientsReadyToPlay++;

        if (clientsReadyToPlay == client.getGroup().getMaxSize()) {
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
     * Client can finally start bowling. Every Client of a Group plays to the same time, so this method
     * is not allowed to be synchronized. This is okay because we don't read or write shared variables here.
     *
     * @param client Client that will start bowling
     */
    public void play(Client client) {
        System.out.println("Client(" + client.getId() + ") in Group(" + client.getGroup().getId() + ") is bowling now on BowlingAlley(" + id + ")!");

        // Clients play
        client.bowl();

        gameEnded(client);
    }

    /**
     * Here we make sure that only one Client (as asked in the exercise) reports to the
     * BowlingArea a new BowlingAlley is free now (= game has ended).
     *
     * @param client Every Client of a Group enters this method
     */
    // TODO: Maybe performance gains when this method is not synchronized. We can however only remove synchronized if we don't work on shared variables. For this a BowlingAlley must be registered to a Client, not to a Group.
    public synchronized void gameEnded(Client client) {
        Group group = client.getGroup();
        if(group.getBowlingAlley() != null) {
            System.out.println("Client(" + client.getId() + ") informs BowlingArea that Group(" + group.getId() + ")'s game is over now.");
            clientsReadyToPlay = 0;
            group.forgetBowlingAlley();
            bowlingArea.gameEnded(this);
        }
    }

    public synchronized int getId() {
        return id;
    }
}

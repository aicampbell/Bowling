package stations;

import actors.Client;
import utils.Group;


/**
 * BowlingAlley can be assigned to one Group at a time that will then play on it.
 *
 * It is managed by an instance of {@link BowlingArea}.
 */
public class BowlingAlley {
    /** Id of a BowlingAlley. */
    private int id;

    /** Number of Clients waiting at this BowlingAlley. */
    private int clientsReadyToPlay;

    /** Instance of BowlingArea which manages all BowlingAlleys. */
    private BowlingArea bowlingArea;

    public BowlingAlley(int id, BowlingArea bowlingArea) {
        this.id = id;
        this.bowlingArea = bowlingArea;
    }

    /**
     * Although a BowlingAlley is only meant for one Group, we need to eliminate the possibility that
     * multiple Clients of this one Group enter this method to the same time. Therefore we need to
     * put {@code synchronized} because instance variables are shared in this method.
     *
     * We don't need to make use of {@link utils.GroupSynchronizer} because for a BowlingAlley
     * not more than one Group can be assigned to at any point of time. There we don't need
     * to keep track of waiting Clients per Group but just can count the waiting Clients at
     * this BowlingAlley.
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

        /** Client plays the bowling match of his life... */
        client.bowl();

        gameEnded(client);
    }

    /**
     * Here we make sure that only one Client (as asked in the exercise) reports to the
     * BowlingArea a new BowlingAlley is free now (= game has ended).
     *
     * With it's current implementation a {@code synchronized} is needed because we need
     * to make sure that a first Client releases the BowlingAlley followed by a natification
     * for the BowlingArea. All other Clients must skip the if-Block, so they don't re-notify
     * the BowlingArea. In oder to make this work, the whole method can only be entered by
     * one Client at a time.
     *
     * @param client Every Client of a Group enters this method
     */
    public synchronized void gameEnded(Client client) {
        Group group = client.getGroup();
        if(group.getBowlingAlley() != null) {
            System.out.println("Client(" + client.getId() + ") informs BowlingArea that Group(" + group.getId() + ")'s game is over now.");
            clientsReadyToPlay = 0;
            group.forgetBowlingAlley();
            bowlingArea.gameEnded(this);
        }
    }

    /**
     * Is called from inside a already {@code synchronized} method. For visibility we put
     * the keyword here too though.
     *
     * @return the Id of the BowlingAlley
     */
    public synchronized int getId() {
        return id;
    }
}

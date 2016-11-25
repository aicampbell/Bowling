package utils;

import actors.Client;
import stations.BowlingAlley;

/**
 * Represents a Group that Clients can be assigned to. The Group object doesn't know its belonging
 * Clients but each Client knows his Group once it is assigned.
 *
 * Since a Group object is shared by more than one Client (if group size > 1), we need to treat it
 * as monitor. Because of that every method is {@code syncronized}.
 */
public class Group {
    /** The amount of Clients that be assigned to a Group at most. */
    private static int MAX_SIZE = 5;

    /** Id of the Group. */
    private int id;
    /** The maximum number of Clients this Group holds. Is MAX_SIZE by default. */
    private int maxSize;
    /** The number of Clients currently assigned to this Group. */
    private int numClients;
    /**
     * The BowlingAlley that the Group is supposed to play one. This information
     * will be provided during runtime.
     */
    private BowlingAlley bowlingAlley;

    public Group(int id) {
        this.id = id;
        this.maxSize = MAX_SIZE;
        numClients = 0;
    }

    /** Enables creation of variable Group sizes. Not used but offered for extendability. */
    public Group(int id, int maxSize) {
        this(id);
        this.maxSize = maxSize;
    }

    public int getId() {
        return id;
    }

    /**
     * A Group object doesn't need to know its Clients, however it needs to
     * know the number of Clients assigned to it. By assigning a new Client
     * to this Group, this method is called to keep track of that number.
     */
    public synchronized void addClient() {
        numClients++;
    }

    public synchronized boolean isFull() {
        return numClients == maxSize;
    }

    public synchronized int getMaxSize() {
        return maxSize;
    }

    public synchronized BowlingAlley getBowlingAlley() {
        return bowlingAlley;
    }

    /**
     * This method is called once it is decided during runtime on which BowlingAlley
     * the Clients of this Group will be playing on.
     *
     * @param bowlingAlley the Clients of this Group will be playing on
     */
    public synchronized void setBowlingAlley(BowlingAlley bowlingAlley) {
        this.bowlingAlley = bowlingAlley;
    }

    /**
     * In {@link BowlingAlley#gameEnded(Client)} we make sure that this method is only called
     * once per Group object. So a synchronized is not really needed here. But we program
     * defensively so we put it.
     */
    public synchronized void forgetBowlingAlley() {
        bowlingAlley = null;
    }

    public synchronized boolean hasAlleyAssigned() {
        return bowlingAlley != null;
    }
}

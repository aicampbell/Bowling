package utils;

import resources.BowlingAlley;

/**
 * A Group has to be treated as Monitor because it its instances are shared among up to MAX_SIZE Clients.
 */
public class Group {
    private static int MAX_SIZE = 5;

    private int id;
    private int maxSize;
    private int numClients;
    private BowlingAlley bowlingAlley;

    public Group(int id) {
        this.id = id;
        this.maxSize = MAX_SIZE;
        numClients = 0;
    }

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

    public synchronized void setBowlingAlley(BowlingAlley bowlingAlley) {
        this.bowlingAlley = bowlingAlley;
    }

    /**
     * In the other code we make sure that this method is only called once per Group object.
     * So a synchronized is not really needed here. But we program defensively so we put it.
     */
    public synchronized void forgetBowlingAlley() {
        bowlingAlley = null;
    }

    public synchronized boolean hasAlleyAssigned() {
        return bowlingAlley != null;
    }
}

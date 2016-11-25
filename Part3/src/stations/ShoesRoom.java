package stations;

import actors.Client;
import utils.Group;
import utils.GroupSynchronizer;
import utils.ShoePair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * ShoesRoom is a room every Client has to pass. It is entered after the RegistrationDesk
 * and before the DancingRoom.
 *
 * It makes use of Group synchronization by extending {@link GroupSynchronizer}.
 */
public class ShoesRoom extends GroupSynchronizer {
    private int MAX_SHOES = Group.MAX_SIZE * BowlingArea.NUM_ALLEYS;

    private class ShoeMonitor {
        Set<Client> waiters;

        public ShoeMonitor() {
            waiters =  new HashSet<>();
        }

        public synchronized void enqueue(Client client) {
            waiters.add(client);
            try {
                wait();
            } catch (InterruptedException e) {
            }
            //client.borrowShoes(getShoePair());
        }

        public synchronized void wakeUpOne() {
            notify();
        }

        public synchronized void borrowShoes(Client client) {
            while(!isShoePairAvailable() || !returnMonitor.isEmpty()) {

            }

            client.borrowShoes(getShoePair());
            waiters.add(client);
            // Enqueue Client
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }

        public synchronized void returnShoes(Client client) {
            while(!returnMonitor.isEmpty()) {
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }

            availableShoes.add(client.returnShoes());
            client.forgetShoes();

            // Enqueue Client

        }

        public boolean isEmpty() {
            return waiters.isEmpty();
        }
    }

    private Set<ShoePair> availableShoes;
    private ShoeMonitor borrowMonitor;
    private ShoeMonitor returnMonitor;

    private boolean returnerIsWaiting = false;

    public ShoesRoom() {
        super();
        availableShoes = new HashSet<>();
        borrowMonitor = new ShoeMonitor();
        returnMonitor = new ShoeMonitor();

        for(int i = 0; i < MAX_SHOES; i++) {
            availableShoes.add(new ShoePair());
        }
    }

    /**
     * Not {@code synchronized} since instance variables are not touched here. Only the call
     * to {@link GroupSynchronizer#waitForWholeGroup(Client)} is synchronized.
     */
    public synchronized void giveShoes(Client client) {
        System.out.println("Client(" + client.getId() + ") arrived in ShoesRoom and gets requests shoes.");

        while(!isShoePairAvailable() || !returnMonitor.isEmpty()) {
            borrowMonitor.enqueue(client);
        }

        /** Borrowing shoes takes some time... */
        client.waitInShoesRoom();

        /**
         * As stated in the text: We need to make sure that we give every Client a
         * separate ShoePair. Since the supply of shoes is infinite, we give each
         * Client a 'new' pair of shoes.
         */
        client.borrowShoes(getShoePair());

        /**
         * Check if Client's Group is complete now.
         * If not, wait for the remaining Clients for that Group.
         * If yes, go on with the whole Group.
         */
        //super.waitForWholeGroup(client);
        if(!returnMonitor.isEmpty()) {
            returnMonitor.wakeUpOne();
        } else if(!borrowMonitor.isEmpty()) {
            borrowMonitor.wakeUpOne();
        }
    }

    /**
     * Not synchronized since instance variables are not touched here.
     */
    public synchronized void returnShoes(Client client) {
        System.out.println("Client(" + client.getId() + ") returned to ShoesRoom and returned his shoes. He's done for today and goes home.");

        returnMonitor.returnShoes(client);

        /** Returning shoes takes some time... */
        client.waitInShoesRoom();

        /** Client returns ShoePair which are added to {@code availableShoes} again. */
        availableShoes.add(client.returnShoes());
        client.forgetShoes();

        /*if(!returnMonitor.isEmpty()) {
            returnMonitor.notify();
        } else {
            borrowMonitor.notify();
        }*/
        borrowMonitor.wakeUpOne();
    }

    public boolean isShoePairAvailable() {
        return !availableShoes.isEmpty();
    }

    public ShoePair getShoePair() {
        assert !availableShoes.isEmpty();
        return availableShoes.iterator().next();
    }
}

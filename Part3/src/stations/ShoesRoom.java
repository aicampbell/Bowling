package stations;

import actors.Client;
import utils.Group;
import utils.GroupSynchronizer;
import utils.ShoePair;

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
        private int numReturnersWaiting = 0;
        private int numBorrowersWaiting = 0;
        private boolean isClientProcessed = false;

        public synchronized void enqueueReturner(Client client) {
            System.out.println("---Client(" + client.getId() + ") wants to return his shoes.");
            /*if (borrowMonitor.isProcessingClient()) {
                numReturnersWaiting++;
                System.out.println("---Client(" + client.getId() + ") must wait before returning his shoes because someone is currently borrowing shoes.");
                try {
                    wait();
                } catch (InterruptedException e) {
                }
                numReturnersWaiting--;
                System.out.println("---Client(" + client.getId() + ") got notified that he can return his shoes now.");
            }*/

            isClientProcessed = true;
            returnShoePair(client);
            isClientProcessed = false;

            //borrowMonitor.wakeUpOne();

            /*if(numReturnersWaiting > 0) {
                System.out.print("---(at least 1 returner is waiting. we wake one returner up.");
                wakeUpOne();
            } else {
                borrowMonitor.wakeUpOne();
                /*if (borrowMonitor.getNumBorrowersWaiting() > 0){
                System.out.print("---(no returners waiting but at least 1 borrower is waiting. we wake one borrower up.");
                borrowMonitor.wakeUpOne();
            }
            }*/
            //System.out.println("Client(" + client.getId() + "): passed last wakingUp section.");
        }

        public synchronized void enqueueBorrower(Client client) {
            while(!isShoePairAvailable() ||
                    /*returnMonitor.isProcessingClient() ||*/
                    returnMonitor.getNumReturnersWaiting() > 0) {
                numBorrowersWaiting++;
                try {
                    wait();
                } catch (InterruptedException e) {
                }
                numBorrowersWaiting--;
            }

            isClientProcessed = true;
            borrowShoePair(client);
            isClientProcessed = false;
            //System.out.println("Client(" + client.getId() + "): isProcessed=false now.");

            //returnMonitor.wakeUpOne();
            //returnMonitor.notify();
            if(numBorrowersWaiting > 0) {
                wakeUpOne();
            }
            //System.out.println("Client(" + client.getId() + "): passed last wakingUp section.");
            /*if(returnMonitor.getNumReturnersWaiting() > 0) {
                returnMonitor.wakeUpOne();
            } else if (numBorrowersWaiting > 0){
                wakeUpOne();
            }*/
        }

        public synchronized void wakeUpOne() {
            notify();
        }

        public synchronized int getNumReturnersWaiting() {
            return numReturnersWaiting;
        }
        public synchronized int getNumBorrowersWaiting() {
            return numBorrowersWaiting;
        }
        public synchronized boolean isProcessingClient() {
            return isClientProcessed;
        }

        public synchronized void returnShoePair(Client client) {
            returnShoes(client);
        }

        public synchronized void borrowShoePair(Client client) {
            borrowShoes(client);
        }
    }

    private Set<ShoePair> availableShoes;
    private ShoeMonitor borrowMonitor;
    private ShoeMonitor returnMonitor;

    public ShoesRoom() {
        super();
        availableShoes = new HashSet<>();
        borrowMonitor = new ShoeMonitor();
        returnMonitor = new ShoeMonitor();

        for(int i = 0; i < MAX_SHOES; i++) {
            availableShoes.add(new ShoePair());
        }
    }

    public void requestBorrowingShoes(Client client) {
        borrowMonitor.enqueueBorrower(client);
        super.waitForWholeGroup(client);
    }
    public void requestReturningShoes(Client client) {
        returnMonitor.enqueueReturner(client);
    }

    /**
     * Not {@code synchronized} since instance variables are not touched here. Only the call
     * to {@link GroupSynchronizer#waitForWholeGroup(Client)} is synchronized.
     */
    private synchronized void borrowShoes(Client client) {
        System.out.println("---Client(" + client.getId() + ") is borrowing his Shoes right now.");

        /**
         * As stated in the text: We need to make sure that we give every Client a
         * separate ShoePair. Since the supply of shoes is infinite, we give each
         * Client a 'new' pair of shoes.
         */
        client.borrowShoes(getShoePair());

        /** Borrowing shoes takes some time... */
        client.waitInShoesRoom();
    }

    /**
     * Not synchronized since instance variables are not touched here.
     */
    private synchronized void returnShoes(Client client) {
        System.out.println("---Client(" + client.getId() + ") is returning his shoes right now. He's done for today and goes home.");

        /** Client returns ShoePair which are added to {@code availableShoes} again. */
        availableShoes.add(client.returnShoes());
        client.forgetShoes();

        /** Returning shoes takes some time... */
        client.waitInShoesRoom();
    }

    private synchronized boolean isShoePairAvailable() {
        return !availableShoes.isEmpty();
    }

    private synchronized ShoePair getShoePair() {
        assert !availableShoes.isEmpty();
        return availableShoes.iterator().next();
    }
}

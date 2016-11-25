package stations;

import actors.Client;
import utils.GroupSynchronizer;
import utils.ShoePair;

/**
 * ShoesRoom is a room every Client has to pass. It is entered after the RegistrationDesk
 * and before the DancingRoom.
 *
 * It makes use of Group synchronization by extending {@link GroupSynchronizer}.
 */
public class ShoesRoom extends GroupSynchronizer {
    public ShoesRoom() {
        super();
    }

    /**
     * Not {@code synchronized} since instance variables are not touched here. Only the call
     * to {@link GroupSynchronizer#waitForWholeGroup(Client)} is synchronized.
     */
    public void giveShoes(Client client) {
        System.out.println("Client(" + client.getId() + ") arrived in ShoesRoom and gets nice shoes.");

        /** Borrowing shoes takes some time... */
        client.waitInShoesRoom();

        /**
         * As stated in the text: We need to make sure that we give every Client a
         * separate ShoePair. Since the supply of shoes is infinite, we give each
         * Client a 'new' pair of shoes.
         */
        client.borrowShoes(new ShoePair());

        /**
         * Check if Client's Group is complete now.
         * If not, wait for the remaining Clients for that Group.
         * If yes, go on with the whole Group.
         */
        super.waitForWholeGroup(client);
    }

    /**
     * Not synchronized since instance variables are not touched here.
     */
    public void returnShoes(Client client) {
        System.out.println("Client(" + client.getId() + ") returned to ShoesRoom and returned his shoes. He's done for today and goes home.");

        /** Returning shoes takes some time... */
        client.waitInShoesRoom();

        /** Just sets shoes to null in the Client object. */
        client.returnShoes();
    }
}

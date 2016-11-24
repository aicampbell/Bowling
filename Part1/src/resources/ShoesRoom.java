package resources;

import actors.Client;
import utils.GroupSynchronizer;

/**
 * Created by mo on 17.11.16.
 */
public class ShoesRoom extends GroupSynchronizer {
    // Right now: Unlimited shoes available.
    // Later: Manage stock of shoes.

    public ShoesRoom() {
        super();
    }

    /**
     * Not synchronized since instance variables are not touched (so far). Only the call
     * to waitForWholeGroup(Client) is synchronized.
     */
    public void giveShoes(Client client) {
        // borrowing shoes takes some time...
        client.waitInShoesRoom();

        // We need to make sure that we give every Client a separate ShoePair.
        // Since the supply of shoes is infinite, we give each client a 'new' pair of shoes.
        client.borrowShoes(new ShoePair());

        // Check if Client's Group is complete now.
        // If not, wait for the remaining Clients for that Group.
        // If yes, go on with the whole group
        super.waitForWholeGroup(client);
    }

    /**
     * Not synchronized since instance variables are not touched (so far).
     */
    public void returnShoes(Client client) {
        // returning shoes takes some time...
        client.waitInShoesRoom();

        // just sets shoes to null in the Client object.
        client.returnShoes();

        // No need to wait for his whole Group since Group splits after playing
    }
}

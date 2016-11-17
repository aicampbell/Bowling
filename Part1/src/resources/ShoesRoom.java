package resources;

import actors.Client;

/**
 * Created by mo on 17.11.16.
 */
public class ShoesRoom {
    // Right now: Unlimited shoes available.
    // Later: Manage stock of shoes.

    public ShoesRoom() {

    }

    public synchronized void giveShoes(Client client) {
        client.setShoes(true);
    }

    public synchronized void returnShoes(Client client) {
        client.setShoes(false);
    }
}

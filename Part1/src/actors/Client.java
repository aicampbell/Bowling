package actors;

import utils.Group;

/**
 * Created by mo on 17.11.16.
 */
public class Client implements Runnable {
    private int id;
    private Group group;
    private boolean hasShoes = false;

    public Client(int id, Group group) {
        this.id = id;
        this.group = group;
    }



    @Override
    public void run() {

    }

    public void setShoes(boolean hasShoes) {
        this.hasShoes = hasShoes;
    }
}

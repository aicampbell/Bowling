package actors;

import resources.*;
import utils.Group;

/**
 * Created by mo on 17.11.16.
 */
public class Client implements Runnable {
    private int id;
    private Group group;
    private ShoePair shoePair;

    private RegistrationDesk registrationDesk;
    private ShoesRoom shoesRoom;
    private DancingRoom dancingRoom;

    public Client(int id) {
        this.id = id;
    }

    @Override
    public void run() {
        randomArrivalDelay();

        registrationDesk.register(this);

        shoesRoom.giveShoes(this);

        BowlingAlley alley = dancingRoom.danceAndRequestAlley(this);
        alley.waitAtAlleyForGroup(this);
        alley.play(this);

        /**
         * Forget notion of Group. From now on every Client acts as an individual and doesn't sync with
         * other Group members anymore.
         */
        forgetAboutGroup();

        registrationDesk.pay(this);

        shoesRoom.returnShoes(this);

        // Client goes home...
    }

    public void waitAtRegistrationDesk() {
        chill(1000);
    }

    public void waitInShoesRoom() {
        chill(500);
    }

    public void bowl() {
        chill(5000);
    }

    private void randomArrivalDelay() {
        int randomDelay = (int)(100000 * Math.random());
        chill(randomDelay);
    }

    private void chill(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
        }
    }

    public void borrowShoes(ShoePair shoePair) {
        this.shoePair = shoePair;
    }

    public void returnShoes() {
        this.shoePair = null;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public void forgetAboutGroup() {
        this.group = null;
    }

    public Client setRegistrationDesk(RegistrationDesk registrationDesk) {
        this.registrationDesk = registrationDesk;
        return this;
    }

    public Client setShoesRoom(ShoesRoom shoesRoom) {
        this.shoesRoom = shoesRoom;
        return this;
    }

    public Client setDancingRoom(DancingRoom dancingRoom) {
        this.dancingRoom = dancingRoom;
        return this;
    }
}

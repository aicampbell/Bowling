package actors;

import stations.*;
import utils.Group;
import utils.ShoePair;

/**
 * A Client is considered a Thread in our design. He visits the different
 * rooms and desks in order to bowl with other Clients in his Group.
 */
public class Client implements Runnable {
    /**
     * Different times in milliseconds for waiting at different steps of the lifecycle
     * of a thread. They exist to mimic reality a little.
     */
    private static int MAX_DELAY_TIME = 10000;
    private static int BOWLING_TIME = 3000;
    private static int REGISTER_PAY_TIME = 1000;
    private static int BORROW_RETURN_SHOES_TIME = 1000;

    /** Id of a Client. */
    private int id;

    /**
     * The Group this Client got assigned to. The value will be determined at
     * the RegistrationDesk during runtime.
     */
    private Group group;

    /** The ShoePair given to a Client. Value will be assigned in ShoesRoom during runtime. */
    private ShoePair shoePair;

    /**
     * In our design, every Client needs to know about the rooms/desk he has to visit.
     */
    private RegistrationDesk registrationDesk;
    private ShoesRoom shoesRoom;
    private DancingRoom dancingRoom;

    public Client(int id) {
        this.id = id;
    }

    @Override
    public void run() {
        /** Adds an initial delay between 0 and 10s so that Clients are a bit distributed over time. */
        randomArrivalDelay();

        /** Register at RegistrationDesk. */
        registrationDesk.register(this);

        /** Get shoes in ShoesRoom. */
        shoesRoom.giveShoes(this);

        /**
         * Go to DanceRoom and dance until a BowlingAlley gets free AND is assigned to
         * this Client's Group.
         */
        BowlingAlley alley = dancingRoom.danceAndRequestAlley(this);
        alley.waitAtAlleyForGroup(this);
        alley.play(this);

        /** Forget notion of Group. From now on every Client acts as an individual. */
        forgetAboutGroup();

        /** Pay at RegistrationDesk. */
        registrationDesk.pay(this);

        /** Return shoes in ShoesRoom. */
        shoesRoom.returnShoes(this);

        /** go home */
    }

    public void waitAtRegistrationDesk() {
        chill(REGISTER_PAY_TIME);
    }

    public void waitInShoesRoom() {
        chill(BORROW_RETURN_SHOES_TIME);
    }

    public void bowl() {
        chill(BOWLING_TIME);
    }

    private void randomArrivalDelay() {
        int randomDelay = (int)(MAX_DELAY_TIME * Math.random());
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

    public int getId() {
        return id;
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

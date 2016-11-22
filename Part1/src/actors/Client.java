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
    private BowlingArea bowlingArea;
    private BowlingAlley bowlingAlley;

    public Client(int id) {
        this.id = id;
    }

    @Override
    public void run() {
        registrationDesk.register(this);

        shoesRoom.giveShoes(this);

        dancingRoom.warmUp(this);

        BowlingAlley alley = dancingRoom.requestAlley(this);

        alley.play(this);

        //group.getBowlingAlley().play(this);

        // OPTIONAL
        // Remove Group assignment. For the remaining steps, a Client acts as individual.
        setGroup(null);

        registrationDesk.pay(this);

        shoesRoom.returnShoes(this);
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

    public Client setBowlingArea(BowlingArea bowlingArea) {
        this.bowlingArea = bowlingArea;
        return this;
    }

    public Client setBowlingAlley(BowlingAlley bowlingAlley) {
        this.bowlingAlley = bowlingAlley;
        return this;
    }

    public BowlingAlley getBowlingAlley() {
        return bowlingAlley;
    }
}

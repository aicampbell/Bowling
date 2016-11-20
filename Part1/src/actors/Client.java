package actors;

import resources.*;
import utils.Group;

/**
 * Created by mo on 17.11.16.
 */
public class Client implements Runnable {
    private int id;
    private int groupId;
    private Group group;
    private ShoePair shoePair;

    private RegistrationDesk registrationDesk;
    private ShoesRoom shoesRoom;
    private DancingRoom dancingRoom;
    private BowlingArea bowlingArea;

    public Client(int id) {
        this.id = id;
    }

    @Override
    public void run() {
        groupId = registrationDesk.register(this);

        shoesRoom.giveShoes(this);

        dancingRoom.warmUp(this);

    }

    public void waitAtRegistrationDesk() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
    }

    public void waitInShoesRoom() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
        }
    }

    public void bowl() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
        }
    }

    public void borrowShoes(ShoePair shoePair) {
        this.shoePair = shoePair;
    }

    public void returnShoes() {
        this.shoePair = null;
    }

    public boolean hasShoes() {
        return shoePair != null;
    }

    public Group getGroup(){
        return group;
    }

    public int getGroupId(){
        return groupId;
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
}

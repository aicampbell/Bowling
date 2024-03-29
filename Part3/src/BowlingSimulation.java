import actors.Client;
import stations.BowlingArea;
import stations.DancingRoom;
import stations.RegistrationDesk;
import stations.ShoesRoom;
import utils.Group;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for setting up the system and create the Client-threads
 */
public class BowlingSimulation {
    /** Number of Groups == number of bowling matches played */
    public static int NUM_GROUPS = 10;

    /** Number of Client-threads that are executed. */
    public static int NUM_CLIENTS = Group.MAX_SIZE * NUM_GROUPS;

    /**
     * The rooms/desk a Client has to go through. In our design he will have to know
     * about these in order to proceed.
     */
    private RegistrationDesk registrationDesk;
    private ShoesRoom shoesRoom;
    private DancingRoom dancingRoom;

    /**
     * List of Client-threads that are created and started. We need these references of the
     * threads in order to join() them at the very end to print the successful runthrough of
     * our simulation.
     */
    private List<Thread> clientThreads;

    public BowlingSimulation() {
        /** Create RegistrationDesk, ShoesRoom, DancingRoom, BowlingArea with BowlingAlleys. */
        registrationDesk = new RegistrationDesk();
        shoesRoom = new ShoesRoom();
        dancingRoom = new DancingRoom();

        /** Create Client threads (don't start them right away). */
        clientThreads = new ArrayList<>();
        for (int i = 0; i < NUM_CLIENTS; i++) {
            Client client = new Client(i);
            client.setRegistrationDesk(registrationDesk);
            client.setShoesRoom(shoesRoom);
            client.setDancingRoom(dancingRoom);

            clientThreads.add(new Thread(client));
        }
    }

    /** Starts the bowling simulation by starting the previously created threads. */
    public void start() {
        System.out.println("Bowling simulation started.\n---------------------------");

        /** Start Client threads. */
        clientThreads.forEach(t -> t.start());

        /** Wait until every Client finished. */
        clientThreads.forEach(t -> {
            try {
                t.join();
            } catch (InterruptedException e) {
            }
        });

        System.out.println("-------------------------\nBowling simulation ended.");

        System.out.println("Number of Clients: " + NUM_CLIENTS);
        System.out.println("Games played (number of Groups): " + NUM_GROUPS);
        System.out.println("Group size: " + Group.MAX_SIZE);
        System.out.println("Number of BowlingAlleys: " + BowlingArea.NUM_ALLEYS);
        System.out.println("Number of Cashiers: " + RegistrationDesk.NUM_CASHIERS);
        System.out.println("Number of ShoePairs: " + ShoesRoom.MAX_SHOES);
    }
}

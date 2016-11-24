import actors.Client;
import resources.DancingRoom;
import resources.RegistrationDesk;
import resources.ShoesRoom;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mo on 24.11.16.
 */
public class BowlingSimulation {
    private static int NUM_CLIENTS = 5;

    private RegistrationDesk registrationDesk;
    private ShoesRoom shoesRoom;
    private DancingRoom dancingRoom;

    private List<Thread> clientThreads;

    public BowlingSimulation() {
        // Create RegistrationDesk, ShoesRoom, DancingRoom, BowlingArea with BowlingAlleys
        registrationDesk = new RegistrationDesk();
        shoesRoom = new ShoesRoom();
        dancingRoom = new DancingRoom();

        // Create Client threads (don't start them yet)
        clientThreads = new ArrayList<>();
        for (int i = 0; i < NUM_CLIENTS; i++) {
            Client client = new Client(i);
            client.setRegistrationDesk(registrationDesk);
            client.setShoesRoom(shoesRoom);
            client.setDancingRoom(dancingRoom);

            clientThreads.add(new Thread(client));
        }
    }

    public void start() {
        System.out.println("Bowling simulation started");

        // start Client threads
        clientThreads.forEach(t -> t.start());

        // wait until every Client finished
        clientThreads.forEach(t -> {
            try {
                t.join();
            } catch (InterruptedException e) {
            }
        });

        System.out.println("Bowling simulation ended.");
    }
}

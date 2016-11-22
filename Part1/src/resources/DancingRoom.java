package resources;

import actors.Client;
import utils.Group;

import java.util.*;

/**
 * Created by mo on 17.11.16.
 */
public class DancingRoom {
    List<Group> dancingGroups;

    // Associates each Group with the Group's Clients that are waiting already
    private Map<Group, Set<Client>> groupsWaiting;
    // Groups registered in this Set are complete and ready to advance.
    private Set<Group> groupsWithAccess;

    private BowlingArea bowlingArea;

    public DancingRoom() {
        dancingGroups = new ArrayList<>();
        groupsWaiting = new HashMap<>();

        bowlingArea = new BowlingArea(this);
    }

    /*public void groupArrives(Group group) {
        dancingGroups.add(group);

        // ....
    }*/

    public void warmUp(Client client) {
        // dance

        //waitForWholeGroup(client);
    }

    public synchronized BowlingAlley requestAlley(Client client) {
        // put in wait if no alley is free

        while (/*group has not alley yet*/ && bowlingArea.getFreeAlley(client) == null) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }

        return bowlingArea.getAlley(client.getGroup());

        // TODO continue here, in this line

        // give access to first Client who request free bowling alley
        // for other Clients in his group: give assigned alley
        // for other Clients in other Groups: put in wait again.
        return null;
    }

    /*public void alleyIsFree() {
        // when notify arrives from BowlingArea
        // notifyAll() ... notify all waiting groups
    }*/

    public synchronized void gameEnded() {
        notifyAll();
        // notify dancing room
    }
}

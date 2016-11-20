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

    public DancingRoom() {
        dancingGroups = new ArrayList<>();
        groupsWaiting = new HashMap<>();
    }

    /*public void groupArrives(Group group) {
        dancingGroups.add(group);

        // ....
    }*/

    public void warmUp(Client client) {
        //waitForWholeGroup(client);


        // mark this client waiting or notify him immediately that alley is free (?)
        // His whole Group will follow him then.
    }

    public void alleyIsFree() {
        // when notify arrives from BowlingArea

        if(!dancingGroups.isEmpty()) {
            Group nextGroup = dancingGroups.get(0);
            //Client clientInGroup = nextGroup.getAClient();

            // group will go to area/alley to play
        }
    }
}

package resources;

import actors.Client;
import utils.Group;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mo on 17.11.16.
 */
public class DancingRoom {
    List<Group> dancingGroups;

    public DancingRoom() {
        dancingGroups = new ArrayList<>();
    }

    /*public void groupArrives(Group group) {
        dancingGroups.add(group);

        // ....
    }*/

    public void warmUp(Client client) {
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

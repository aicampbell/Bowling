package resources;

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

    public void groupArrives(Group group) {
        dancingGroups.add(group);

        // ....
    }

    public void alleyIsFree() {
        // when notify arrives from BowlingArea

        if(!dancingGroups.isEmpty()) {
            Group nextGroup = dancingGroups.get(0);

            // group will go to area/alley to play
        }
    }
}

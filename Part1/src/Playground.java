import utils.Group;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mo on 20.11.16.
 */
public class Playground {
    public static void main(String[] args) {
        Playground playground = new Playground();
        playground.testSomeObjRefs();
    }

    public void testSomeObjRefs() {
        List<Group> groups = new ArrayList<>();
        int groupCounter = 0;
        Group currentGroup = null;

        for (int i = 0; i < 20; i++) {
            if(currentGroup == null || currentGroup.isFull()) {
                currentGroup = new Group(groupCounter);
            }

            currentGroup.addClient();

            if (currentGroup.isFull()) {
                groups.add(currentGroup);
                groupCounter++;
            }

            System.out.println(currentGroup);
        }

    }
}

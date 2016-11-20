package utils;

import actors.Client;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by mo on 17.11.16.
 */
public class Group {
    public static int MAX_SIZE = 5;

    int id;
    int maxSize;
    int numClients;

    public Group(int id) {
        this.id = id;
        this.maxSize = MAX_SIZE;
        numClients = 0;
    }

    public void addClient() {
        numClients++;
    }

    public boolean isFull() {
        return numClients == maxSize;
    }

    public int getId() {
        return id;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public int getNumClients() {
        return numClients;
    }

    /**
     * When comparing Groups with equals(), only the groupId is taken into account.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Group)) return false;
        Group group = (Group) o;
        return id == group.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

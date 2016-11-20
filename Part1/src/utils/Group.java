package utils;

import actors.Client;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by mo on 17.11.16.
 */
public class Group {
    public static int MAX_SIZE = 10;

    int id;
    int maxSize;

    List<Client> clients;

    public Group(int id) {
        this.id = id;
        this.maxSize = MAX_SIZE;
        clients = new ArrayList<>();
    }

    public Group(Group group) {
        this.id = group.getId();
        this.clients = group.getClients();
        this.maxSize = group.getMaxSize();
    }

    public void addClient(Client client) {
        if (clients.size() < maxSize) {
            clients.add(client);
        }
        // throw exception
    }

    public boolean isFull() {
        return clients.size() == maxSize;
    }

    public int getId() {
        return id;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public List<Client> getClients() {
        return clients;
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

package stations;

import actors.Client;
import utils.Group;

/**
 * A Cashier handles registration and payment of a Client. Cashier objects are used to
 * distribute the sleeping time (time for registering) among {@link RegistrationDesk#NUM_CASHIERS}
 * Cashiers.
 */
public class Cashier {
    /** Id of Cashier. */
    int id;

    /** Reference to the RegistrationDesk. */
    RegistrationDesk registrationDesk;

    public Cashier(int id, RegistrationDesk registrationDesk) {
        this.id = id;
        this.registrationDesk = registrationDesk;
    }

    /**
     * Every Client must register at the RegistrationDesk. Must be {@code synchronized} because
     * shared variables are used inside this method.
     *
     * @param client Client that wants to register
     */
    public void register(Client client) {
        //System.out.println("Client(" + client.getId() + ") arrived at Cashier(" + id + ") to register.");

        /** Registering takes some time... */
        client.waitAtRegistrationDesk();

        /**
         * Register the Client independent of a Group. {@code clients} is not used at another
         * place in code, might make sense though in reality to keep track of all Clients.
         */
        registrationDesk.addClient(client);

        /** Assign Client to a Group and let Client know about his Group. */
        Group group = registrationDesk.getAssignedGroupForClient();
        client.setGroup(group);
        //System.out.println("Client(" + client.getId() + ") got assigned to Group(" + group.getId() + ").");

        /** Notify the RegistrationDesk that this Cashier is now available again. */
        cashierGotAvailable();
    }

    /**
     * Every Client must chargeFee at the RegistrationDesk after his bowling match is over.
     * <p>
     * This method is {@code synchronized} because a shared variable is currently modified.
     * See last method comment for an alternative approach.
     *
     * @param client Client that has to chargeFee
     */
    public void chargeFee(Client client) {
        //System.out.println("Client(" + client.getId() + ") returned to Cashier(" + id + ") for paying.");

        /** Paying takes some time... */
        client.waitAtRegistrationDesk();

        /**
         * Remove Client from {@code clients}. This can be removed if variable {@code clients}
         * is supposed to even keep track of Clients after they left the venue. If it's removed
         * this method doesn't have to be {@code synchronized}.
         */
        registrationDesk.removeClient(client);
        System.out.println("Client(" + client.getId() + ") just paid.");

        /** Notify the RegistrationDesk that this Cashier is now available again. */
        cashierGotAvailable();
    }

    /** Method that informs the RegistrationDesk about the fact that this Cashier just got free. */
    private void cashierGotAvailable() {
        registrationDesk.cashierGotAvailable(this);
    }

    public int getId() {
        return id;
    }
}

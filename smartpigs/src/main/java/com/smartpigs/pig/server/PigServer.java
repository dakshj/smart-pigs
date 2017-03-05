package com.smartpigs.pig.server;

import com.smartpigs.game.server.GameServer;
import com.smartpigs.model.Address;
import com.smartpigs.model.Cell;
import com.smartpigs.model.Occupant;
import com.smartpigs.model.Pig;
import com.smartpigs.pig.client.ShelterInformer;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Set;

public interface PigServer extends Remote {

    /**
     * Establishes a connection with a {@link PigServer} identified by {@code pig}.
     *
     * @param pig The pig whose {@link PigServer} needs to be connected to
     * @return An instance of the connected {@link PigServer}, connected remotely via Java RMI
     * @throws RemoteException   Thrown when a Java RMI exception occurs
     * @throws NotBoundException Thrown when the remote binding does not exist in the {@link Registry}
     */
    static PigServer connect(final Pig pig) throws RemoteException, NotBoundException {
        final Registry registry = LocateRegistry.getRegistry(pig.getAddress().getHost(),
                pig.getAddress().getPortNo());
        return (PigServer) registry.lookup(PigServerImpl.NAME);
    }

    /**
     * Initializes all data in the {@link PigServer} instance.
     * <p>
     * Local data is reset to default values.
     * <p>
     * Remote data fetched from parameters overwrites the data currently stored
     * in the running instance, if any.
     *
     * @param gameServerAddress {@link Address} of the {@link GameServer}
     * @param pig               An encapsulation of the pig's ID and address
     * @param peers             The {@link Set} of peers of the pig
     * @param neighbors         The {@link List} of neighbors of the pig
     * @param maxHopCount       The network's maximum hop count
     * @param hopDelay          The network's hop delay
     * @throws RemoteException Thrown when a Java RMI exception occurs
     */
    void receiveData(Address gameServerAddress, Pig pig, Set<Pig> peers, List<List<Occupant>> neighbors,
            int maxHopCount, long hopDelay) throws RemoteException;

    /**
     * Informs the current {@link PigServer} instance about an incoming bird attack.
     * <p>
     * This instance will flood this message to all its peers with a decremented hop count,
     * if there is still a valid (i.e., greater than 0) hop count.
     * <p>
     * If the cell being attacked is the cell where the pig is located at,
     * then the pig will send a message to all its neighbors to take shelter via {@link ShelterInformer}.
     * <p>
     * If there is still time for the bird attack to happen on the pig's cell, it will find an
     * empty cell to move to so as to evade the attack.
     * <p>
     * If there is no empty cell and the pig is going to eventually die, then the pig will
     * mark itself as dead after that amount of time has gone by, and also fall onto
     * a random neighboring cell, be it a stone or another pig.
     * <p>
     * If the attack has already happened on the pig, it will mark itself as dead,
     * then fall onto a random neighboring cell.
     * <p>
     * If the random neighboring cell that the pig falls onto is a stone, then the pig will
     * inform {@link GameServer} about the stone that it fell on.
     * <p>
     * It is them {@link GameServer}'s responsibility to propogate the collateral damage
     * caused by the attack on the stone onto a random neighboring cell of the stone, on which
     * the stone falls.
     *
     * @param path            The {@link List} of pigs that have formed a unique path during flooding
     * @param attackEta       The remaining time to a bird attack
     * @param attackedCell    The cell that is going to be the target of the bird attack
     * @param currentHopCount The remaining number of hops the message can be flooded
     * @throws RemoteException Thrown when a Java RMI exception occurs
     */
    void birdApproaching(List<Pig> path, long attackEta, Cell attackedCell, int currentHopCount)
            throws RemoteException;

    /**
     * Kills the pig because of collateral death.
     *
     * @throws RemoteException Thrown when a Java RMI exception occurs
     */
    void killByFallingOver() throws RemoteException;

    /**
     * Makes a pig take shelter away from the sender, who is a pig that is dead or about to die,
     * and thus cause collateral death to this pig.
     * <p>
     * To take shelter, a pig needs to move at least two steps away from the sender.
     *
     * @param sender The pig that initiated {@link ShelterInformer}, and who is dead or about to die
     */
    void takeShelter(Pig sender) throws RemoteException;

    /**
     * Updates the {@link Cell} of a neighbor when that neighbor moves itself.
     * <p>
     * Useful since after a pig dies and randomly selects the cell which was the original cell of
     * the neighbor, it should have updated data that this cell is actually empty and does not
     * contain the neighbor.
     *
     * @param neighbor The neighbor which was moved to another cell
     * @throws RemoteException Thrown when a Java RMI exception occurs
     */
    void updateNeighborCell(Pig neighbor) throws RemoteException;
}

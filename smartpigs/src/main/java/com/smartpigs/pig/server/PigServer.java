package com.smartpigs.pig.server;

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

    // TODO Add Javadoc to all methods!

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

    void receiveData(Pig pig, Set<Pig> peers, List<List<Occupant>> neighbors,
            int hopCount, long hopDelay) throws RemoteException;

    void birdApproaching(List<Pig> path, long attackEta, Cell attackedCell, int currentHopCount)
            throws RemoteException;

    void killByFallingOver() throws RemoteException;

    /**
     * Makes a pig take shelter away from the sender, who is a pig that is dead or about to die,
     * and thus cause collateral death to this pig.
     * <p>
     * To take shelter, a pig needs to move at least two steps away from the sender.
     *
     * @param sender The pig that initiated {@link ShelterInformer}, and who is dead or about to die
     */
    void takeShelter(Pig sender);

    void updateNeighborCell(Pig neighbor);
}

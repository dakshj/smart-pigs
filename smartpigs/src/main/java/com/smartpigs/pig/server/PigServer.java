package com.smartpigs.pig.server;

import com.smartpigs.model.Cell;
import com.smartpigs.model.Occupant;
import com.smartpigs.model.Pig;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Set;

public interface PigServer extends Remote {

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

    void takeShelter(Pig sender);

    void updateNeighborCell(Pig neighbor);
}

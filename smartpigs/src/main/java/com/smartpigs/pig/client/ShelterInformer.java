package com.smartpigs.pig.client;

import com.smartpigs.enums.OccupantType;
import com.smartpigs.model.Occupant;
import com.smartpigs.model.Pig;
import com.smartpigs.pig.PigServer;
import com.smartpigs.pig.PigServerImpl;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Collection;
import java.util.List;

public class ShelterInformer {

    private final Pig sender;
    private final List<List<Occupant>> neighbors;

    public ShelterInformer(final Pig sender, final List<List<Occupant>> neighbors) {
        this.sender = sender;
        this.neighbors = neighbors;
    }

    public void inform(final NeighborCellUpdateListener listener) {
        neighbors.stream()
                .flatMap(Collection::stream)
                .filter(occupant -> occupant.getOccupantType() == OccupantType.PIG)
                .forEach(occupant -> inform((Pig) occupant, listener));
    }

    private boolean inform(final Pig pig, final NeighborCellUpdateListener listener) {
        try {
            final Registry registry = LocateRegistry.getRegistry(pig.getAddress().getHost(),
                    pig.getAddress().getPortNo());
            PigServer pigServer = (PigServer) registry.lookup(PigServerImpl.NAME);

            pigServer.takeShelter(sender);
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }

        return false;
    }

    public interface NeighborCellUpdateListener {
        void onCellUpdated(final Pig neighbor);
    }
}

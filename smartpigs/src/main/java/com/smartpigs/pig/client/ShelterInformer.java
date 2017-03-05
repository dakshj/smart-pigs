package com.smartpigs.pig.client;

import com.smartpigs.enums.OccupantType;
import com.smartpigs.model.Occupant;
import com.smartpigs.model.Pig;
import com.smartpigs.pig.server.PigServer;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.List;

public class ShelterInformer {

    private final Pig sender;
    private final List<List<Occupant>> neighbors;

    public ShelterInformer(final Pig sender, final List<List<Occupant>> neighbors) {
        this.sender = sender;
        this.neighbors = neighbors;
    }

    public void inform() {
        neighbors.stream()
                .flatMap(Collection::stream)
                .filter(occupant -> occupant.getOccupantType() == OccupantType.PIG)
                .forEach(occupant -> inform((Pig) occupant));
    }

    private boolean inform(final Pig pig) {
        try {
            PigServer.connect(pig).takeShelter(sender);
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }

        return false;
    }
}

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

    /**
     * Informs all pig {@link #neighbors} of a {@link #sender} pig to take shelter.
     * <p>
     * The pig {@link #neighbors} will then find an appropriate empty cell to move to, if any.
     */
    public void inform() {
        neighbors.stream()
                .flatMap(Collection::stream)
                .filter(occupant -> occupant.getOccupantType() == OccupantType.PIG)
                .forEach(occupant -> new Thread(() -> inform((Pig) occupant)).start());
    }

    private void inform(final Pig pig) {
        try {
            PigServer.connect(pig).takeShelter(sender);
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
    }
}

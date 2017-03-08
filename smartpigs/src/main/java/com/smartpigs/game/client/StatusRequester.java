package com.smartpigs.game.client;

import com.smartpigs.model.Pig;
import com.smartpigs.pig.server.PigServer;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class StatusRequester {

    private final Set<Pig> pigSet;

    public StatusRequester(final Set<Pig> pigSet) {
        this.pigSet = pigSet;
    }

    /**
     * Requests the hit status of all pigs.
     * <p>
     * Analogous to "status_all()".
     *
     * @return A {@link Map} of Pig IDs vs. their corresponding hit statuses
     */
    public Map<String, Boolean> request() {
        final Map<String, Boolean> hitMap = new HashMap<>();

        for (final Pig pig : pigSet) {
            try {
                hitMap.put(pig.getId(), request(pig));
            } catch (RemoteException | NotBoundException e) {
                e.printStackTrace();
            }
        }

        return hitMap;
    }

    /**
     * Requests the hit status of a single pig.
     * <p>
     * Analogous to "status(pigID)".
     *
     * @param pig The pig whose hit status needs to be queried
     * @return {@code true} if the pig was hit;
     * {@code false} otherwise
     * @throws RemoteException   Thrown when a Java RMI exception occurs
     * @throws NotBoundException Thrown when the remote binding does not exist in the {@link Registry}
     */
    private boolean request(final Pig pig) throws RemoteException, NotBoundException {
        return PigServer.connect(pig).wasHit();
    }
}

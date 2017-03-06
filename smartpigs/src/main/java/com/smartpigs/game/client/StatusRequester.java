package com.smartpigs.game.client;

import com.smartpigs.model.Pig;
import com.smartpigs.pig.server.PigServer;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class StatusRequester {

    private final Set<Pig> pigSet;

    public StatusRequester(final Set<Pig> pigSet) {
        this.pigSet = pigSet;
    }

    /**
     * Requests the hit status of each pig.
     *
     * @return A {@link Map} of Pig IDs vs. their corresponding hit statuses
     */
    public Map<String, Boolean> request() {
        final Map<String, Boolean> hitMap = new HashMap<>();

        for (final Pig pig : pigSet) {
            try {
                hitMap.put(pig.getId(), PigServer.connect(pig).wasHit());
            } catch (RemoteException | NotBoundException e) {
                e.printStackTrace();
            }
        }

        return hitMap;
    }
}

package com.smartpigs.pig.client;

import com.smartpigs.model.Cell;
import com.smartpigs.model.Pig;
import com.smartpigs.pig.server.PigServer;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Set;

public class BirdAttackInformer {

    private final Pig sender;
    private final List<Pig> path;
    private final Set<Pig> peers;
    private final long attackEta;
    private final Cell attackedCell;
    private final int hopCount;
    private final long hopDelay;

    public BirdAttackInformer(final Pig sender, final List<Pig> path,
            final Set<Pig> peers, final long attackEta,
            final Cell attackedCell, final int hopCount, final long hopDelay) {
        this.sender = sender;
        this.path = path;
        this.peers = peers;
        this.attackEta = attackEta;
        this.attackedCell = attackedCell;
        this.hopCount = hopCount;
        this.hopDelay = hopDelay;
    }

    /**
     * Informs all peers of a pig about a bird that approaching.
     * <p>
     * Adds an artificial delay ({@link #hopDelay}) to simulate network latency.
     * <p>
     * Sends to only those peers which are not already in that specific broadcast path.
     * <p>
     * <strong>NOTE:</strong> A peer may be reached by multiple pigs, thus via multiple paths.
     * Thus, maintaining a separate path {@link List} for each unique path is very important.
     */
    public void inform() {
        path.add(sender);

        // TODO use a working parallelStream() instead of spawning Threads
        peers.stream()
                .filter(peer -> !path.contains(peer))

                // Execute each birdApproaching() in parallel Threads
                .forEach(peer -> new Thread(() -> {
                    try {
                        PigServer.connect(peer)
                                .birdApproaching(path, attackEta - hopDelay,
                                        attackedCell, hopCount - 1);
                    } catch (RemoteException | NotBoundException e) {
                        e.printStackTrace();
                    }
                }).start());
    }
}

package com.smartpigs.pig.client;

import com.smartpigs.model.Cell;
import com.smartpigs.model.Pig;
import com.smartpigs.pig.PigServer;
import com.smartpigs.pig.PigServerImpl;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
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

    public void inform() {
        path.add(sender);

        try {
            Thread.sleep(hopDelay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // FIXME parallelStream() is not resulting into parallelism!
        peers.parallelStream()
                .filter(peer -> !path.contains(peer))
                .forEach(peer -> {
                    try {
                        final Registry registry = LocateRegistry.getRegistry(peer.getAddress().getHost(),
                                peer.getAddress().getPortNo());
                        PigServer pigServer = (PigServer) registry.lookup(PigServerImpl.NAME);

                        pigServer.birdApproaching(path, attackEta - hopDelay,
                                attackedCell, hopCount - 1);
                    } catch (RemoteException | NotBoundException e) {
                        e.printStackTrace();
                    }
                });
    }
}

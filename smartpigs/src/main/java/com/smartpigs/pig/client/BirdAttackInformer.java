package com.smartpigs.pig.client;

import com.smartpigs.model.Address;
import com.smartpigs.model.Cell;
import com.smartpigs.pig.PigServer;
import com.smartpigs.pig.PigServerImpl;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Set;

public class BirdAttackInformer {

    private final Address senderAddress;
    private final List<Address> path;
    private final Set<Address> peerAddresses;
    private final long attackEta;
    private final Cell attackedCell;
    private final int hopCount;
    private final long hopDelay;

    public BirdAttackInformer(final Address senderAddress, final List<Address> path,
            final Set<Address> peerAddresses, final long attackEta,
            final Cell attackedCell, final int hopCount, final long hopDelay) {
        this.senderAddress = senderAddress;
        this.path = path;
        this.peerAddresses = peerAddresses;
        this.attackEta = attackEta;
        this.attackedCell = attackedCell;
        this.hopCount = hopCount;
        this.hopDelay = hopDelay;
    }

    public void inform() {
        path.add(senderAddress);

        try {
            Thread.sleep(hopDelay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // FIXME parallelStream() is not resulting into parallelism!
        peerAddresses.parallelStream()
                .filter(address -> !path.contains(address))
                .forEach(address -> {
                    final Registry registry;
                    try {
                        registry = LocateRegistry.getRegistry(address.getHost(),
                                address.getPortNo());
                        PigServer pigServer = (PigServer) registry.lookup(PigServerImpl.NAME);

                        pigServer.birdApproaching(path, attackEta - hopDelay,
                                attackedCell, hopCount - 1);
                    } catch (RemoteException | NotBoundException e) {
                        e.printStackTrace();
                    }
                });
    }
}

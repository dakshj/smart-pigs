package com.smartpigs.pig.server;

import com.smartpigs.enums.OccupantType;
import com.smartpigs.model.Cell;
import com.smartpigs.model.Occupant;
import com.smartpigs.model.Pig;
import com.smartpigs.pig.client.BirdAttackInformer;
import com.smartpigs.pig.client.NeighborCellUpdater;
import com.smartpigs.pig.client.PigKiller;
import com.smartpigs.pig.client.ShelterInformer;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class PigServerImpl extends UnicastRemoteObject implements PigServer {

    static final String NAME = "Pig Server";

    private boolean aliveStatus;
    private boolean floodedBirdApproaching = false;
    private Pig pig;
    private Set<Pig> peers;
    private List<List<Occupant>> neighbors;
    private int maxHopCount;
    private long hopDelay;

    public PigServerImpl(final int portNo) throws RemoteException {
        try {
            startServer(portNo);
        } catch (RemoteException ignored) {
            System.out.println(NAME + "@" + portNo + " failed to start!");
        }
    }

    private void startServer(final int portNo) throws RemoteException {
        final Registry registry = LocateRegistry.createRegistry(portNo);
        registry.rebind(NAME, this);
    }

    @Override
    public void receiveData(final Pig pig, final Set<Pig> peers, final List<List<Occupant>> neighbors,
            final int maxHopCount, final long hopDelay) throws RemoteException {
        resetLocalData();
        setPig(pig);
        setPeers(peers);
        setNeighbors(neighbors);
        setMaxHopCount(maxHopCount);
        setHopDelay(hopDelay);
    }

    private void resetLocalData() {
        setAliveStatus(true);
        setFloodedBirdApproaching(false);
    }

    @Override
    public void birdApproaching(final List<Pig> path, final long attackEta,
            final Cell attackedCell, final int currentHopCount) throws RemoteException {
        if (getPig().getOccupiedCell().equals(attackedCell)) {
            new ShelterInformer(getPig(), getNeighbors()).inform();

            if (attackEta > 0) {
                final Optional<Occupant> emptyOccupantOptional = getNeighbors().stream()
                        .flatMap(Collection::stream)
                        .filter(occupant -> occupant.getOccupantType() == OccupantType.EMPTY)
                        .findFirst();

                if (emptyOccupantOptional.isPresent()) {
                    getPig().setOccupiedCell(emptyOccupantOptional.get().getOccupiedCell());
                    return;
                } else {
                    try {
                        Thread.sleep(attackEta);
                        killSelfAndAnotherOccupant();
                        return;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                killSelfAndAnotherOccupant();
            }

            return;
        }

        if (currentHopCount == 0 || hasFloodedBirdApproaching()) {
            return;
        }

        setFloodedBirdApproaching(true);

        new BirdAttackInformer(getPig(), new ArrayList<>(path), peers, attackEta, attackedCell,
                currentHopCount, getHopDelay()).inform();
    }

    @Override
    public void killByFallingOver() throws RemoteException {
        setAliveStatus(false);
    }

    @Override
    public void takeShelter(final Pig sender) {
        getNeighbors().stream()
                .flatMap(Collection::stream)

                // Filter only empty cells
                .filter(occupant -> occupant.getOccupantType() == OccupantType.EMPTY)

                // Filter only those empty cells that are at least two steps away from the sender
                .filter(occupant -> Math.max(
                        Math.abs(sender.getOccupiedCell().getRow() - occupant.getOccupiedCell().getRow()),
                        Math.abs(sender.getOccupiedCell().getCol() - occupant.getOccupiedCell().getCol())
                ) >= 2)

                // Select the first such empty cell
                .findFirst()
                .ifPresent(occupant -> {
                            // Move to that empty cell
                            getPig().setOccupiedCell(occupant.getOccupiedCell());

                            // Since this pig has moved to another cell,
                            // its neighbors list is useless now
                            setNeighbors(null);

                            // Inform sender that this pig has moved
                            new NeighborCellUpdater(getPig(), sender).update();
                        }
                );
    }

    @Override
    public void updateNeighborCell(final Pig neighbor) {
        getNeighbors().stream()
                .flatMap(Collection::stream)
                .filter(occupant -> {
                    if (occupant instanceof Pig) {
                        final Pig pig = (Pig) occupant;
                        if (pig.getId().equals(neighbor.getId())) {
                            return true;
                        }
                    }

                    return false;
                }).findFirst()
                .ifPresent(occupant ->
                        occupant.setOccupiedCell(neighbor.getOccupiedCell()));
    }

    private void killSelfAndAnotherOccupant() {
        setAliveStatus(false);

        final int row = ThreadLocalRandom.current().nextInt(0, 3);
        final int col = ThreadLocalRandom.current().nextInt(0, 3);

        final Occupant occupant = getNeighbors().get(row).get(col);

        if (occupant == null) {
            killSelfAndAnotherOccupant();
            return;
        }

        switch (occupant.getOccupantType()) {
            case PIG:
                new PigKiller(((Pig) occupant)).kill();
                break;

            case STONE:
                // TODO Tell Game Server that this pig has fallen onto a stone
                // TODO Game Server will then choose a random Cell for the stone to drop and kill accordingly
                break;
        }
    }

    private Pig getPig() {
        return pig;
    }

    private void setPig(final Pig pig) {
        this.pig = pig;
    }

    private void setMaxHopCount(final int maxHopCount) {
        this.maxHopCount = maxHopCount;
    }

    private long getHopDelay() {
        return hopDelay;
    }

    private void setHopDelay(final long hopDelay) {
        this.hopDelay = hopDelay;
    }

    private void setPeers(final Set<Pig> peers) {
        this.peers = peers;
    }

    private List<List<Occupant>> getNeighbors() {
        return neighbors;
    }

    private void setNeighbors(final List<List<Occupant>> neighbors) {
        this.neighbors = neighbors;
    }

    private boolean hasFloodedBirdApproaching() {
        return floodedBirdApproaching;
    }

    private void setFloodedBirdApproaching(final boolean floodedBirdApproaching) {
        this.floodedBirdApproaching = floodedBirdApproaching;
    }

    private void setAliveStatus(final boolean aliveStatus) {
        this.aliveStatus = aliveStatus;
    }
}

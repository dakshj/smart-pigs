package com.smartpigs.pig.server;

import com.smartpigs.enums.OccupantType;
import com.smartpigs.model.Address;
import com.smartpigs.model.Cell;
import com.smartpigs.model.Occupant;
import com.smartpigs.model.Pig;
import com.smartpigs.pig.client.BirdAttackInformer;
import com.smartpigs.pig.client.FallingOnStoneInformer;
import com.smartpigs.pig.client.PigKiller;
import com.smartpigs.pig.client.ShelterInformer;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

public class PigServerImpl extends UnicastRemoteObject
        implements PigServer, ShelterInformer.NeighborMovedListener {

    static final String NAME = "Pig Server";

    private boolean floodedBirdApproaching = false;
    private Address gameServerAddress;
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

        System.out.println("Pig Server created.\nWaiting for data...");
    }

    private void startServer(final int portNo) throws RemoteException {
        final Registry registry = LocateRegistry.createRegistry(portNo);
        registry.rebind(NAME, this);
    }

    @Override
    public void receiveData(final Address gameServerAddress, final Pig pig, final Set<Pig> peers,
            final List<List<Occupant>> neighbors,
            final int maxHopCount, final long hopDelay) throws RemoteException {
        resetLocalData();
        setGameServerAddress(gameServerAddress);
        setPig(pig);
        setPeers(peers);
        setNeighbors(neighbors);
        setMaxHopCount(maxHopCount);
        setHopDelay(hopDelay);

        System.out.println("Received data.");
        System.out.println("\tPig ID : " + getPig().getId());
        System.out.println("\tCell : " + getPig().getOccupiedCell());
        System.out.println("\tPeers : " + getPeers());
        System.out.println("\tNeighbors : " + getNeighbors());
    }

    private void resetLocalData() {
        setFloodedBirdApproaching(false);
    }

    @Override
    public void birdApproaching(final List<Pig> path, final long attackEta,
            final Cell attackedCell, final int currentHopCount) throws RemoteException {
        // Since a pig can be contacted via multiple peers, it could already be dead
        // when the second peer contacts this pig. So, do nothing, since you're dead.
        if (!getPig().isAlive()) {
            return;
        }

        if (attackEta > 0) {
            System.out.println("Bird approaching at Cell " + attackedCell
                    + ". ETA : " + attackEta + " ms.");
        } else {
            System.out.println("Bird crashed at Cell " + attackedCell + " "
                    + Math.abs(attackEta) + " ms ago.");
        }

        if (getPig().getOccupiedCell().equals(attackedCell)) {
            new ShelterInformer(getPig(), getNeighbors(), this).inform();

            if (attackEta > 0) {
                final Optional<Occupant> emptyOccupantOptional = getNeighbors().stream()
                        .flatMap(Collection::stream)
                        .filter(Objects::nonNull)
                        .filter(occupant -> occupant.getOccupantType() == OccupantType.EMPTY)
                        .findFirst();

                if (emptyOccupantOptional.isPresent()) {
                    getPig().setOccupiedCell(emptyOccupantOptional.get().getOccupiedCell());
                    System.out.println("Found safe haven at Cell " + getPig().getOccupiedCell());
                    return;
                } else {
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            killSelfAndAnotherOccupant();
                        }
                    }, attackEta);
                    return;
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

        new BirdAttackInformer(getPig(), new ArrayList<>(path), getPeers(), attackEta, attackedCell,
                currentHopCount, getHopDelay()).inform();
    }

    @Override
    public void killedByFallingOver() throws RemoteException {
        getPig().setDead();
        System.out.println("I AM DEAD!");
    }

    @Override
    public boolean takeShelter(final Pig sender) {
        System.out.println(sender + " asked to take shelter!");

        final Optional<Occupant> optional = getNeighbors().stream()
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)

                // Filter only empty cells
                .filter(occupant -> occupant.getOccupantType() == OccupantType.EMPTY)

                // Filter only those empty cells that are at least two steps away from the sender
                .filter(occupant -> Math.max(
                        Math.abs(sender.getOccupiedCell().getRow() - occupant.getOccupiedCell().getRow()),
                        Math.abs(sender.getOccupiedCell().getCol() - occupant.getOccupiedCell().getCol())
                ) >= 2)

                // Select the first such empty cell
                .findFirst();

        if (optional.isPresent()) {
            final Occupant occupant = optional.get();
            // Update neighbors list so as to set pig's cell to empty
            final Occupant emptyOccupant = new Occupant();
            emptyOccupant.setOccupiedCell(getPig().getOccupiedCell());
            emptyOccupant.setOccupantType(OccupantType.EMPTY);

            // Set {1,1} as empty because a pig is in the center of its neighbor list,
            // thus moving from there will make {1,1} empty
            getNeighbors().get(1).set(1, emptyOccupant);

            // Move pig to the found empty cell
            getPig().setOccupiedCell(occupant.getOccupiedCell());

            // FIXME this is wrong because getNeighbors() is 3x3 and currently it is referencing through larger grid's row,col
            // Update neighbors list to reflect pig's movement to a new empty cell
            getNeighbors().get(getPig().getOccupiedCell().getRow())
                    .set(getPig().getOccupiedCell().getCol(), getPig());

            System.out.println("Moved to Cell "
                    + getPig().getOccupiedCell()
                    + " to increase chances of survival!");

            return true;
        }

        return false;
    }

    private void killSelfAndAnotherOccupant() {
        try {
            killedByFallingOver();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        final int row = ThreadLocalRandom.current().nextInt(0, 3);
        final int col = ThreadLocalRandom.current().nextInt(0, 3);

        final Occupant occupant = getNeighbors().get(row).get(col);

        if (occupant == null) {
            killSelfAndAnotherOccupant();
            return;
        }

        System.out.println("Falling on Cell " + occupant.getOccupiedCell());

        switch (occupant.getOccupantType()) {
            case PIG:
                new PigKiller(((Pig) occupant)).kill();
                break;

            case STONE:
                new FallingOnStoneInformer(getGameServerAddress(), occupant).inform();
                break;
        }
    }

    private Address getGameServerAddress() {
        return gameServerAddress;
    }

    private void setGameServerAddress(final Address gameServerAddress) {
        this.gameServerAddress = gameServerAddress;
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

    private Set<Pig> getPeers() {
        return peers;
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

    @Override
    public void moved(final Pig neighbor) {
        for (int row = 0; row < getNeighbors().size(); row++) {
            for (int col = 0; col < getNeighbors().get(row).size(); col++) {
                final Occupant occupant = getNeighbors().get(row).get(col);
                if (occupant.getOccupantType() == OccupantType.PIG &&
                        ((Pig) occupant).getId().equals(neighbor.getId())) {

                    // Set old cell of neighbor as empty
                    final Occupant emptyOccupant = new Occupant();
                    emptyOccupant.setOccupantType(OccupantType.EMPTY);
                    emptyOccupant.setOccupiedCell(new Cell(row, col));
                    getNeighbors().get(row).set(col, emptyOccupant);

                    // FIXME this is wrong because getNeighbors() is 3x3 and currently it is referencing through larger grid's row,col
                    // Set new cell of neighbor, as neighbor
                    getNeighbors().get(neighbor.getOccupiedCell().getRow())
                            .set(neighbor.getOccupiedCell().getCol(), neighbor);
                }
            }
        }
    }
}

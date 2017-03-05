package com.smartpigs.pig;

import com.smartpigs.model.Cell;
import com.smartpigs.model.Pig;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Set;

public interface PigServer extends Remote {
    void receiveData(Pig pig, Set<Pig> peers, int hopCount, long hopDelay) throws RemoteException;

    void birdApproaching(List<Pig> path, long attackEta, Cell attackedCell, int currentHopCount)
            throws RemoteException;
}

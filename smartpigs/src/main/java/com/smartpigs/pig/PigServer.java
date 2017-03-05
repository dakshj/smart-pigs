package com.smartpigs.pig;

import com.smartpigs.model.Address;
import com.smartpigs.model.Cell;
import com.smartpigs.model.Pig;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface PigServer extends Remote {
    void receiveData(Pig pig, int hopCount, long hopDelay) throws RemoteException;

    void birdLaunched(long attackEta, Cell attackedCell) throws RemoteException;

    void birdApproaching(List<Address> path, long attackEta, Cell attackedCell, int currentHopCount)
            throws RemoteException;
}

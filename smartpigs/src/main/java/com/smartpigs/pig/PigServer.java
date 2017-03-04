package com.smartpigs.pig;

import com.smartpigs.model.Pig;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface PigServer extends Remote {
    void receiveData(Pig pig, int hopCount, int hopDelay) throws RemoteException;
}

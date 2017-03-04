package com.smartpigs.pig;

import com.smartpigs.model.Address;

import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class PigClient {

    private final Address address;

    public PigClient(final Address address) throws RemoteException {
        this.address = address;
        final String urlServer = "rmi://"
                + address.getIpAddress()
                + ":" + address.getPortNo()
                + "/" + PigServerImpl.NAME;

        final Registry registry = LocateRegistry
                .getRegistry(address.getIpAddress(), address.getPortNo());

        try {
            PigServer pigServer = (PigServer) registry.lookup(PigServerImpl.NAME);
        } catch (ConnectException | NotBoundException e) {
            System.out.println("Pig@" + address + " is not running!");
            e.printStackTrace();
        }
    }
}

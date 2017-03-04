package com.smartpigs.game;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class GameServerImpl extends UnicastRemoteObject implements GameServer {

    private static final String NAME = GameServerImpl.class.getName();

    public GameServerImpl(final int portNo, final String configFilePath) throws RemoteException {
        try {
            startServer(portNo);
        } catch (RemoteException ignored) {
            System.out.println(NAME + "@" + portNo + " failed to start!");
        }

        final Configuration configuration = readConfigurationFromFile(configFilePath);

        // TODO initialize Pigs using configuration
        // TODO create grid using configuration
        // TODO assign neighbors to Pigs
    }

    private void startServer(final int portNo) throws RemoteException {
        final Registry registry = LocateRegistry.createRegistry(portNo);
        registry.rebind(NAME, this);
    }

    private Configuration readConfigurationFromFile(final String configFilePath) {
        // TODO parse json stored at configFilePath
        return null;
    }
}

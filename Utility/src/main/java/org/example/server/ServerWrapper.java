package org.example.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ServerWrapper extends UnicastRemoteObject implements ServerRemote {
    private final ServerRemote server;

    public ServerWrapper(ServerRemote server) throws RemoteException {
        this.server = server;
    }

    @Override
    public void run() throws RemoteException {
        server.run();
    }

    @Override
    public String shutdown() throws RemoteException {
        return server.shutdown();
    }

    @Override
    public boolean isRunning() throws RemoteException {
        return server.isRunning();
    }

    @Override
    public String getState() throws RemoteException {
        return server.getState();
    }

    @Override
    public String getName() throws RemoteException {
        return server.getName();
    }
}

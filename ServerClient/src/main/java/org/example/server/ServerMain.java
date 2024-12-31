package org.example.server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Klasse zur Implementierung des Multithreaded Servers
 */
public class ServerMain {


    public static void main(String[] arg) {
        ServerRemote server = ThreadedServer.getServer();
        try {
            ServerWrapper wrapper = new ServerWrapper(server);
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("ThreadedServer", wrapper);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
        try {
            server.run();
            while (server.isRunning()) {
                try {
                    Thread.sleep(1_000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            System.out.println("ServerMain is stopped");
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
        System.out.println("END");
    }
}

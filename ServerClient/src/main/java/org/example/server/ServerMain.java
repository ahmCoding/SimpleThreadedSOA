package org.example.server;

import org.example.helper.Config;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * Klasse zur Implementierung des Multithreaded Servers
 */
public class ServerMain {


    public static void main(String[] arg) {
        ServerRemote server = ThreadedServer.getServer();

        try {
            ServerWrapper wrapper = new ServerWrapper(server);
            final Registry registry = LocateRegistry.createRegistry(Config.RMI_PORT);
            registry.rebind("ThreadedServer", wrapper);
            // shutdown hook für das saubere beenden des Servers im Notfall (ctrl+c, exit(0),...)
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    registry.unbind("ThreadedServer");
                    UnicastRemoteObject.unexportObject(registry, true);
                } catch (RemoteException | NotBoundException e) {
                    System.err.println("Fehler beim Beenden: " + e.getMessage());
                }
            }));
            // Start the server
            server.run();
            while (server.isRunning()) {
                try {
                    Thread.sleep(Config.SERVER_SLEEP_TIME);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            // stop the server and RMI-Services
            registry.unbind("ThreadedServer");
            UnicastRemoteObject.unexportObject(registry, true);
            System.out.println("ServerMain is stopped");
            System.exit(0);
        } catch (RemoteException | NotBoundException e) {
            throw new RuntimeException(e);
        }
        System.out.println("END");
    }
}

package org.example.server;

import org.example.helper.Config;
import org.example.loggerModule.LoggerClass;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * Klasse zur Implementierung des Multithreaded Servers
 */
public class ServerMain {
    private static LoggerClass logger;

    /**
     * Gibt eine Instanz der Logger-Klasse zurueck.
     *
     * @param className Der Name der Klasse, die logt.
     **/
    public static LoggerClass getLogger(String className) {
        return LoggerClass.getLogger(className);
    }

    public static void main(String[] arg) {
        // init logger
        logger = LoggerClass.getLogger(ServerMain.class.getName());
        logger.logInfo("logger initialized");
        logger.logInfo("ServerMain is started");
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
                    logger.logWarning("Fehler beim Beenden: " + e.getMessage());
                }
            }));
            // Start the server
            server.run();

            while (server.isRunning()) {
                try {
                    Thread.sleep(Config.SERVER_SLEEP_TIME);
                } catch (InterruptedException e) {
                    logger.logInfo("ServerMain is interrupted: " + e.getMessage());
                }
            }
            // stop the server and RMI-Services
            registry.unbind("ThreadedServer");
            UnicastRemoteObject.unexportObject(registry, true);
            logger.logInfo("ServerMain is stopped");
            System.exit(0);
        } catch (RemoteException | NotBoundException e) {
            throw new RuntimeException(e);
        }
        logger.logInfo("logger shutdown");
        logger.shutdown();
    }
}

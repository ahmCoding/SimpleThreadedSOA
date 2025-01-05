package org.example.client;

import org.example.dataLoader.DAO;
import org.example.helper.Config;
import org.example.loggerModule.LoggerClass;
import org.example.server.ServerRemote;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Klasse zur Simulation von Clients-Anfragen
 */
public class ClientMain {
    private static LoggerClass logger;

    public static void main(String[] args) {
        // init logger
        logger = LoggerClass.getLogger(ClientMain.class.getName());
        logger.logInfo("logger initialized");
        logger.logInfo("ClientMain is started");
        Registry registry = null;
        try {
            registry = LocateRegistry.getRegistry("localhost", Config.RMI_PORT);
            ServerRemote serverRemote = (ServerRemote) registry.lookup("ThreadedServer");
            logger.logInfo("ClientMain is connected to the server: " + serverRemote.getName());
            Thread[] threads;
            int numOfIt = 1;
            DAO dao = DAO.getDao();
            for (int i = 1; i <= numOfIt; i++) {
                threads = new Thread[i];
                for (int j = 0; j < i; j++) {
                    threads[j] = new Thread(new Client(serverRemote, dao));
                    threads[j].setName("Client-" + j);
                    threads[j].start();
                }
                for (int j = 0; j < i; j++) {
                    try {
                        threads[j].join();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            Thread tShutDown = new Thread(new Client(serverRemote, dao, true));
            tShutDown.setName("Client-Shutdown-Thread");
            logger.logInfo("Client sendet den Shutdown-Befehl an Server");
            tShutDown.start();
        } catch (RemoteException | NotBoundException ex) {
            logger.logWarning(ex.getMessage());
        }
        logger.logInfo("ClientMain is shutting down");
        logger.logInfo("logger shutdown");
        logger.shutdown();
    }


    public static LoggerClass getLogger(String className) {
        return LoggerClass.getLogger(className);
    }
}

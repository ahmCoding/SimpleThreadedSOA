package org.example.client;

import org.example.dataLoader.DAO;
import org.example.server.ServerRemote;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Klasse zur Simulation von Clients-Anfragen
 */
public class ClientMain {
    public static void main(String[] args) {

        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            ServerRemote serverRemote = (ServerRemote) registry.lookup("ThreadedServer");
            Thread[] threads;
            int numOfIt = 5;
            DAO dao = DAO.getDao();
            for (int i = 1; i <= numOfIt; i++) {
                threads = new Thread[i];
                for (int j = 0; j < i; j++) {
                    threads[j] = new Thread(new Client(serverRemote, dao));
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
            tShutDown.start();
        } catch (RemoteException | NotBoundException ex) {
            throw new RuntimeException(ex);
        }

    }


}

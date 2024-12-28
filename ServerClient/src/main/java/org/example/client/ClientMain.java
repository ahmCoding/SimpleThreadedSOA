package org.example.client;

import org.example.helper.DAO;

/**
 * Klasse zur Simulation von Clients-Anfragen
 */
public class ClientMain {
    public static void main(String[] args) {
        Thread[] threads;
        int numOfIt = 5;
        DAO dao = DAO.getDao();
        for (int i = 1; i <= numOfIt; i++) {
            threads = new Thread[i];
            for (int j = 0; j < i; j++) {
                threads[j] = new Thread(new Client(dao));
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
        // server shutdown
        Thread tShutDown = new Thread(new Client(dao, true));
        tShutDown.start();
    }

}

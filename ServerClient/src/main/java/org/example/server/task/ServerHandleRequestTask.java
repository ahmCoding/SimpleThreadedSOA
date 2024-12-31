package org.example.server.task;

import org.example.server.ThreadedServer;

/**
 * Klasse zur Behandlung von Clients-Anfragen.
 * Diese Klasse führt die {@link ThreadedServer#run()} Methode aus.
 */
public class ServerHandleRequestTask implements Runnable {
    private final ThreadedServer server;

    public ServerHandleRequestTask(ThreadedServer server) {
        this.server = server;

    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            server.handleRequests();
        }
        System.err.println("Server is stopped and can't response to any client");
    }
}

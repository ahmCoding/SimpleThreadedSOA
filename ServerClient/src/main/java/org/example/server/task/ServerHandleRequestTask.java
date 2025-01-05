package org.example.server.task;

import org.example.loggerModule.LoggerClass;
import org.example.server.ServerMain;
import org.example.server.ThreadedServer;

/**
 * Klasse zur Behandlung von Clients-Anfragen.
 * Diese Klasse führt die {@link ThreadedServer#run()} Methode aus.
 */
public class ServerHandleRequestTask implements Runnable {
    private final ThreadedServer server;
    private LoggerClass logger;

    public ServerHandleRequestTask(ThreadedServer server) {
        this.server = server;
        logger = ServerMain.getLogger(this.getClass().getName());

    }

    @Override
    public void run() {
        try {
            logger.logInfo("server-service for handling client requests started.");
            server.handleRequests();
        } catch (Exception e) {
            if (server.isRunning()) {
                logger.logError("Error while handling client requests.");
                logger.logError(e.getMessage());
            }
        }
        logger.logInfo("ServerHandleRequestTask shut down.");
    }
}

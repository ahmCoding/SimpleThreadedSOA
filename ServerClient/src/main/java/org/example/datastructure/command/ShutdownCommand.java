package org.example.datastructure.command;

import org.example.server.Server;

import java.net.ServerSocket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Klasse zur Implementierung des Befehls "Shutdown"
 */
public class ShutdownCommand extends Command {
    private ServerSocket serverSocket;
    public ShutdownCommand(String[] argument,ServerSocket serverSocket) {
        super(argument);
        this.serverSocket = serverSocket;
    }

    @Override
    public String execute() {
        Server.stopServer = true;
        // keine weiteren Anfragen annehmen
        Server.getExecutor().shutdown();
        try {
            // ServerSocket schließen damit serverSocket.accept() beendet wird
            serverSocket.close();

        } catch (Exception e) {
            System.err.println("Error while closing the server socket.");
            e.printStackTrace();
        }
        return "Server Socket closed!";
    }
}

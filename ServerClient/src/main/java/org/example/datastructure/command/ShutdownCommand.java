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
    public ShutdownCommand(String[] argument) {
        super(argument);
    }

    @Override
    public String execute() {
        return Server.shutdown();
    }
}

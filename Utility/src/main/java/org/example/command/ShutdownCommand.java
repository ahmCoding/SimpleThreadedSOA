package org.example.command;

import org.example.server.ServerRemote;

import java.rmi.RemoteException;

/**
 * Klasse zur Implementierung des Befehls "Shutdown"
 */
public class ShutdownCommand extends Command {
    private ServerRemote server;

    public ShutdownCommand(String[] argument, ServerRemote server) {
        super(argument);
        this.server = server;
    }

    @Override
    public String execute() throws RemoteException {
        return server.shutdown();
    }
}

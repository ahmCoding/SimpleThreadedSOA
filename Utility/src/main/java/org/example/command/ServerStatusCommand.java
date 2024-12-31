package org.example.command;


import org.example.server.ServerRemote;

import java.rmi.RemoteException;

/**
 * Gibt den aktuellen Status des Servers zurück.
 */
public class ServerStatusCommand extends Command {
    private final ServerRemote server;

    public ServerStatusCommand(String[] argument, ServerRemote server) {
        super(argument);
        this.server = server;
    }

    @Override
    public String execute() {
//        StringWriter writer = new StringWriter();
//        int numberOfThreads = Server.getExecutor().getPoolSize();
//        int load = (int)(1.0 * Server.getExecutor().getActiveCount() / numberOfThreads * 100);
//        writer.write("Number of Threads in the ThreadPool: ");
//        writer.write(Integer.toString(numberOfThreads));
//        writer.write(";");
//        writer.write("Load of the ThreadPool: ");
//        writer.write(Integer.toString(load) + " %");
//        writer.write(";");
//        writer.write("Number of successfully executed tasks: ");
//        writer.write(Integer.toString((int)Server.getExecutor().getCompletedTaskCount()));
//        writer.write(";");
//        writer.write("Number of all tasks: ");
//        writer.write(Integer.toString((int)Server.getExecutor().getTaskCount()));
//        writer.write(";");
//        writer.write("Current size of queue: ");
//        writer.write(Integer.toString((int)Server.getExecutor().getQueue().size()));
//        writer.write(";");
//        return writer.toString();

        try {
            return server.getState();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }
}

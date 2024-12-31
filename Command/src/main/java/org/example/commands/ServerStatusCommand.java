package org.example.commands;


import java.io.StringWriter;

public class ServerStatusCommand extends Command {
    public ServerStatusCommand(String[] argument) {
        super(argument);
    }

    @Override
    public String execute() {
        StringWriter writer = new StringWriter();
        int numberOfThreads = Server.getExecutor().getPoolSize();
        int load = (int)(1.0 * Server.getExecutor().getActiveCount() / numberOfThreads * 100);
        writer.write("Number of Threads in the ThreadPool: ");
        writer.write(Integer.toString(numberOfThreads));
        writer.write(";");
        writer.write("Load of the ThreadPool: ");
        writer.write(Integer.toString(load) + " %");
        writer.write(";");
        writer.write("Number of successfully executed tasks: ");
        writer.write(Integer.toString((int)Server.getExecutor().getCompletedTaskCount()));
        writer.write(";");
        writer.write("Number of all tasks: ");
        writer.write(Integer.toString((int)Server.getExecutor().getTaskCount()));
        writer.write(";");
        writer.write("Current size of queue: ");
        writer.write(Integer.toString((int)Server.getExecutor().getQueue().size()));
        writer.write(";");
        return writer.toString();
    }
}

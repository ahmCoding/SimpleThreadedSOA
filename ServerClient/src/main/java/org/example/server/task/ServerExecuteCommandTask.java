package org.example.server.task;

import org.example.command.*;
import org.example.server.ThreadedServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Klasse zur Ausführung von Commands, die aus Clients-Anfrage kommen.
 */
public class ServerExecuteCommandTask implements Runnable {
    private final Socket clientSocket;
    private final ThreadedServer server;

    public ServerExecuteCommandTask(Socket clientSocket, ThreadedServer server) {
        this.clientSocket = clientSocket;
        this.server = server;

    }

    @Override
    public void run() {
        PrintWriter out = null;
        BufferedReader in = null;

        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String line = in.readLine();
            Command command;
            String[] commandData = line.split(";");
            System.err.println("Command: " + commandData[0]);
            // Command nicht vorhanden in Cache
            command = switch (commandData[0]) {
                case "q" -> {
                    System.err.println("Query");
                    yield new QueryCommand(commandData);
                }
                case "r" -> {
                    System.err.println("Report");
                    yield new ReportCommand(commandData);
                }
                case "s" -> {
                    System.err.println("Status");
                    yield new ServerStatusCommand(commandData, server);
                }
                case "z" -> {
                    System.err.println("Stop");
                    yield new ShutdownCommand(commandData, server);
                }
                default -> {
                    System.err.println("Error");
                    yield new ErrorCommand(commandData);
                }
            };
            // ServerCache wird zuerst gesucht-> Cache-Hit
            String result = server.getServerCache().get(command);
            if (result != null) {
                out.println(server.getServerCache().get(command));
                System.err.println("**** Cache hit for command: " + line + " ****");
                return;
            }

            result = command.execute();
            // wenn der Command zum Zwischenspeichern geeignet ist (Serverstatus oder Shutdown sind nicht!)
            if (command.isCacheable()) server.getServerCache().put(command, result);
            System.err.println(result);
            out.println(result);
        } catch (IOException e) {
            System.err.println("Error while processing the client request.");
            System.err.println(e.getMessage());
        }
        // ClientSocket schließen, finally-block wegen 'return' in 'Cache-hit' Fall notwendig
        finally {
            try {
                clientSocket.close();
                if (out != null) out.close();
                if (in != null) in.close();
            } catch (IOException e) {
                System.err.println("Error while closing the client socket/streams.");
                System.err.println(e.getMessage());
            }
        }
    }
}

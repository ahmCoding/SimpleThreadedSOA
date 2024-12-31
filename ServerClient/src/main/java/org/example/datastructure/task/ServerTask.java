package org.example.datastructure.task;

import org.example.commands.*;
import org.example.server.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerTask implements Runnable{
    private final Socket clientSocket;

    public ServerTask(Socket clientSocket) {
        this.clientSocket = clientSocket;

    }

    @Override
    public void run() {

        try (PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));) {
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
                    yield new ServerStatusCommand(commandData);
                }
                case "z" -> {
                    System.err.println("Stop");
                    yield new ShutdownCommand(commandData);
                }
                default -> {
                    System.err.println("Error");
                    yield new ErrorCommand(commandData);
                }
            };
            // ServerCache wird zuerst gesucht
            String result =Server.getServerCache().get(command);
            if (result != null) {
                out.println(Server.getServerCache().get(command));
                System.err.println("**** Cache hit for command: " + line+" ****");
                return;
            }

            result = command.execute();
            Server.getServerCache().put(line, result);
            System.err.println(result);
            out.println(result);
        } catch (IOException e) {
            System.err.println("Error while processing the client request.");
            System.err.println(e.getMessage());
        }
        // ClientSocket schließen, finally-block wegen 'return' in 'Cache-hit' Fall
        finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error while closing the client socket.");
                System.err.println(e.getMessage());
            }
        }
    }
}

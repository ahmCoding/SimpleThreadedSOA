package org.example.datastructure.task;

import org.example.datastructure.command.*;
import org.example.server.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerTask implements Runnable{
    private final Socket clientSocket;
    private final ServerSocket serverSocket;

    public ServerTask(ServerSocket serverSocket,Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.serverSocket = serverSocket;
    }

    @Override
    public void run() {
        try (PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));) {
            String line = in.readLine();
            Command command;
            String[] commandData = line.split(";");
            System.err.println("Command: " + commandData[0]);
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
                    yield new ShutdownCommand(commandData, serverSocket);
                }
                default -> {
                    System.err.println("Error");
                    yield new ErrorCommand(commandData);
                }
            };
            String response = command.execute();
            System.err.println(response);
            out.println(response);
        } catch (
                IOException e) {
            e.printStackTrace();
        }
        // ClientSocket schließen
        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

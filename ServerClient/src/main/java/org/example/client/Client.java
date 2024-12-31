package org.example.client;

import org.example.dataLoader.DAO;
import org.example.helper.Config;
import org.example.server.ServerRemote;
import org.example.wdi.WDI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Random;

/**
 * Klasse zur Implementierung des seriellen Clients.
 * Jedes Client-Objekt führt 10 Iterationen von jeweils 9 Query-Abfragen und einer Report-Abfrage durch.
 * Die Klasse wird dann zur Erzeugung von mehreren Client-Objekten verwendet, die in Threads ausgeführt werden.
 */
public class Client implements Runnable {

    private static final int QUERY_ITERATIONS = 10;
    private static final int QUERIES_PER_ITERATION = 9;
    private ServerRemote server;
    private final DAO dao;
    private final Random random;
    private boolean shutdownServer;

    public Client(ServerRemote server, DAO daoToset, boolean shutdownServer) {
        dao = daoToset;
        random = new Random();
        this.shutdownServer = shutdownServer;
        this.server = server;
    }

    public Client(ServerRemote server, DAO daoToset) {

        this(server, daoToset, false);
    }

    @Override
    public void run() {
        if (!shutdownServer) {
            List<WDI> data = dao.getDataset();
            for (int i = 0; i < QUERY_ITERATIONS; i++) {
                executeQueryBatch(data);
                executeServerState();
                executeReportQuery(data);
            }
            return;
        }
        executeServerState();
        executeShutdownCommand();
    }

    private String buildServerStateCommand() {
        return "s";
    }

    private void executeServerState() {
        String command = buildServerStateCommand();
        executeCommand(command);
    }

    private WDI getRandomWDI(List<WDI> data) {
        return data.get(random.nextInt(data.size()));
    }

    private String buildQueryCommand(WDI wdi) {
        return String.format("q;%s;%s", wdi.getCountryCode(), wdi.getIndicatorCode());
    }

    private String buildReportCommand(WDI wdi) {
        return String.format("r;%s", wdi.getIndicatorCode());
    }

    private String buildShutdownCommand() {
        return "z";
    }

    /**
     * Führt einen Befehl auf dem Server aus.
     *
     * @param command Befehl
     */
    private void executeCommand(String command) {
        try {
            if (server.isRunning()) {
                try (Socket socket = new Socket("localhost", Config.PARALLEL_PORT);
                     PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                    out.println(command);
                    String response = in.readLine();
                    System.err.println("Server response: " + response);
                } catch (IOException e) {
                    System.err.println("Error while executing command: " + command);
                    System.err.println("Error message: " + e.getMessage());
                }
                return;
            }
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
        System.err.println("Server is not running." + command + " cannot be executed");
    }

    /**
     * Führt eine Batch von Query-Befehlen auf dem Server aus.
     *
     * @param data Liste von WDI-Objekten
     */
    private void executeQueryBatch(List<WDI> data) {
        for (int j = 0; j < QUERIES_PER_ITERATION; j++) {
            WDI randomWdi = getRandomWDI(data);
            String command = buildQueryCommand(randomWdi);
            executeCommand(command);
        }
    }

    /**
     * Führt den Shutdown-Befehl auf dem Server aus.
     */
    private void executeShutdownCommand() {
        String command = buildShutdownCommand();
        executeCommand(command);

    }

    /**
     * Führt einen Report-Befehl auf dem Server aus.
     *
     * @param data Liste von WDI-Objekten
     */
    private void executeReportQuery(List<WDI> data) {
        WDI randomWdi = getRandomWDI(data);
        String command = buildReportCommand(randomWdi);
        executeCommand(command);
    }
}

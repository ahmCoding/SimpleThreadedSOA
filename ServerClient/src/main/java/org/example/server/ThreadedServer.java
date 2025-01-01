package org.example.server;

import org.example.cache.CacheSystem;
import org.example.helper.Config;
import org.example.server.task.ServerExecuteCommandTask;
import org.example.server.task.ServerHandleRequestTask;

import java.io.IOException;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Klasse zur Implementierung des Multithreaded Servers
 */
public class ThreadedServer implements ServerRemote {
    private String name;
    private static ThreadedServer server;
    // Server kann von verschiedenen Threads gestoppt werden, deswegen volatile
    private volatile boolean stopServer;
    private ThreadPoolExecutor executor;
    private ServerSocket serverSocket;
    private CacheSystem serverCache;
    private Thread handleRequestTask;

    public ThreadPoolExecutor getExecutor() {
        return executor;
    }

    private ThreadedServer() {
        name = "Singleton multithreaded server";
        init();
    }

    /**
     * Gibt eine Instanz des Servers zurück.
     *
     * @return Eine Instanz des Servers
     */
    public static ServerRemote getServer() {
        if (server == null) {
            server = new ThreadedServer();
        }
        return server;
    }


    private void init() {
        stopServer = true;
        executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        serverCache = new CacheSystem();
        try {
            serverSocket = new ServerSocket(Config.PARALLEL_PORT, 50, InetAddress.getByName("127.0.0.1"));
        } catch (IOException e) {
            System.err.println("Error while creating the server socket.");
            System.err.println(e.getMessage());
            return;
        }
        System.err.println("Initialization completed.");
    }

    /**
     * Gibt den Server-Cache zurück.
     *
     * @return Der Server-Cache
     */
    public CacheSystem getServerCache() {
        return serverCache;
    }

    @Override
    public void run() {
        if (!isRunning()) {
            // Der Task, der auf Anfragen wartet
            handleRequestTask = new Thread(new ServerHandleRequestTask(server));
            handleRequestTask.setName("ServerHandleRequestTask-Thread");
            handleRequestTask.start();
            stopServer = false;
            System.out.println("Server is running and can receives client requests.");
            return;
        }
        System.out.println("Server is already running.");
    }

    public void handleRequests() {
        do {
            try {
                Socket clientSocket = serverSocket.accept();
                Thread delegatedTask = new Thread(new ServerExecuteCommandTask(clientSocket, server));
                executor.execute(delegatedTask);
            } catch (IOException e) {
                System.err.println("Error while accepting a client connection.");
                System.err.println(e.getMessage());
                e.getStackTrace();
            }
        } while (isRunning());
        System.out.println("Server prepared for shutdown.");
        try {
            // warte, bis alle Tasks beendet sind
            executor.awaitTermination(15, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Server shutdown completed.");

    }

    /**
     * Beendet den Server.
     *
     * @return Eine Meldung, ob der Server erfolgreich beendet wurde
     */
    public String shutdown() {
        if (isRunning()) {
            // keine weiteren Anfragen annehmen
            executor.shutdown();
            handleRequestTask.interrupt();
            serverCache.shutdown();
            try {
                // ServerSocket schließen damit serverSocket.accept() beendet wird
                serverSocket.close();

            } catch (Exception e) {
                System.err.println("Error while closing the server socket.");
                System.err.println(e.getMessage());
                return "Error while closing the server socket.";
            }
            stopServer = true;
            return Thread.currentThread().getName() + " : Server Socket closed!";
        }
        return Thread.currentThread().getName() + " : Server already closed!";
    }

    @Override
    public boolean isRunning() {
        return !stopServer;
    }

    @Override
    public String getState() {
        StringWriter writer = new StringWriter();
        writer.write(name);
        writer.write(" --> ");
        int numberOfThreads = getExecutor().getPoolSize();
        int load = (int) (1.0 * getExecutor().getActiveCount() / numberOfThreads * 100);
        writer.write("Number of Threads in the ThreadPool: ");
        writer.write(Integer.toString(numberOfThreads));
        writer.write(";");
        writer.write("Load of the ThreadPool: ");
        writer.write(Integer.toString(load) + " %");
        writer.write(";");
        writer.write("Number of successfully executed tasks: ");
        writer.write(Integer.toString((int) getExecutor().getCompletedTaskCount()));
        writer.write(";");
        writer.write("Number of all tasks: ");
        writer.write(Integer.toString((int) getExecutor().getTaskCount()));
        writer.write(";");
        writer.write("Current size of queue: ");
        writer.write(Integer.toString((int) getExecutor().getQueue().size()));
        writer.write(";");
        return writer.toString();
    }

    @Override
    public String getName() {
        return name;
    }


}

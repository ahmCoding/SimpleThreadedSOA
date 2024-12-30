package org.example.server;

import org.example.datastructure.task.ServerTask;
import org.example.helper.Config;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Klasse zur Implementierung des Multithreaded Servers
 */
public class Server {
    // Server kann von verschiedenen Threads gestoppt werden, deswegen volatile
    private static volatile boolean stopServer;
    private static ThreadPoolExecutor executor;
    private  static ServerSocket serverSocket;
    public static ThreadPoolExecutor getExecutor() {
        return executor;
    }

    public static void main(String[] arg) {
        stopServer = false;
        executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        try {
            serverSocket = new ServerSocket(Config.PARALLEL_PORT, 50, InetAddress.getByName("127.0.0.1"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Initialization completed.");
        do {
            try {
                    Socket clientSocket = serverSocket.accept();
                    Thread delegatedTask = new Thread(new ServerTask(serverSocket,clientSocket));
                    executor.execute(delegatedTask);
                }catch (IOException e) {
                    System.err.println("Error while accepting a client connection.");
                    System.err.println(e.getMessage());
                }
            } while (!stopServer);
            System.out.println("Server prepared for shutdown.");
            try {
             // warte, bis alle Tasks beendet sind
            executor.awaitTermination(15, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Server shutdown completed.");

    }

    public static String shutdown() {
        if(!stopServer){
        stopServer = true;
        // keine weiteren Anfragen annehmen
        executor.shutdown();
        try {
            // ServerSocket schließen damit serverSocket.accept() beendet wird
            serverSocket.close();

        } catch (Exception e) {
            System.err.println("Error while closing the server socket.");
            e.printStackTrace();
            return "Error while closing the server socket.";
        }
        return Thread.currentThread().getName() + " : Server Socket closed!";
    }
        return Thread.currentThread().getName() + " : Server already closed!";
    }
    /**
     * Überprüft, ob der Server noch aktiv ist.
     *
     * @return true, wenn der Server noch aktiv ist, sonst false
     */
    public static boolean isOn() {
        return !stopServer;
    }

}

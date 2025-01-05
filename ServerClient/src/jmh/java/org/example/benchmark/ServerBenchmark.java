package org.example.benchmark;

import org.example.client.Client;
import org.example.dataLoader.DAO;
import org.example.helper.Config;
import org.example.server.ServerRemote;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(value = 1)
@Warmup(iterations = 2)
@Measurement(iterations = 3)
public class ServerBenchmark {

    private ServerRemote server;
    private DAO dao;

    @Setup
    public void setup() {
        try {
            // Registry aufbauen wie in ClientMain
            Registry registry = LocateRegistry.getRegistry("localhost", Config.RMI_PORT);
            server = (ServerRemote) registry.lookup("ThreadedServer");
            dao = DAO.getDao();
        } catch (Exception e) {
            throw new RuntimeException("Setup failed", e);
        }
    }

    private void runClientsAndWait(int numClients) {
        Thread[] threads = new Thread[numClients];

        // Start threads
        for (int j = 0; j < numClients; j++) {
            threads[j] = new Thread(new Client(server, dao));
            threads[j].start();
        }

        // Wait for completion
        for (int j = 0; j < numClients; j++) {
            try {
                threads[j].join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Benchmark
    public void benchmark1Client() {
        runClientsAndWait(1);
    }

    @Benchmark
    public void benchmark2Clients() {
        runClientsAndWait(2);
    }

    @Benchmark
    public void benchmark3Clients() {
        runClientsAndWait(3);
    }

    @Benchmark
    public void benchmark4Clients() {
        runClientsAndWait(4);
    }

    @Benchmark
    public void benchmark5Clients() {
        runClientsAndWait(5);
    }

    @TearDown
    public void teardown() {
        // Cleanup - Server herunterfahren
        try {
            Client shutdownClient = new Client(server, dao, true);
            shutdownClient.run();
        } catch (Exception e) {
            System.err.println("Teardown failed: " + e.getMessage());
        }
    }

    public static void main(String[] args) throws RunnerException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String resultsFile = String.format("benchmark_results_%s", timestamp);

        Options opt = new OptionsBuilder()
                .include(ServerBenchmark.class.getSimpleName())
                .resultFormat(ResultFormatType.JSON) // Save as JSON
                .result(resultsFile + ".json") // JSON results
                .build();

        new Runner(opt).run();
        Collection<RunResult> results = new Runner(opt).run();
        // Shutdown server after benchmarks
        System.out.println("Benchmarks complete, shutting down server...");
        System.out.println("Results saved to: " + resultsFile + ".json ");
    }
}
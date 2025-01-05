package org.example.loggerModule;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testklasse für den Logger.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LoggerTest {
    private static final String TEST_CLASS_NAME = "TestClass";
    private static LoggerClass logger;
    private static Path logFilePath;
    private Config config;

    @BeforeEach
    void setUp() throws IOException {
        // Config instance for testing
        config = Config.getInstance();

        // Logger-Instanz zurücksetzen
        LoggerClass.reset();

        // Logger-Instanz für den Test erstellen
        logger = LoggerClass.getLogger(TEST_CLASS_NAME);
        logFilePath = Paths.get(config.getLogFilePath() + TEST_CLASS_NAME + "-" +
                config.getFileNameFormatter().format(java.time.LocalDateTime.now()) + ".log");

        try {
            Files.createDirectories(logFilePath.getParent());
            if (Files.exists(logFilePath)) {
                Files.write(logFilePath, new byte[0], StandardOpenOption.TRUNCATE_EXISTING);
            } else {
                Files.createFile(logFilePath);
            }
        } catch (IOException e) {
            fail("Failed to setup test log file: " + e.getMessage());
        }
    }

    @AfterEach
    void tearDown() {
        // Logger herunterfahren
        logger.shutdown();

        // Datei löschen mit Retry-Mechanismus
        int maxRetries = 3;
        int retryDelay = 100; // milliseconds

        for (int i = 0; i < maxRetries; i++) {
            try {
                Files.deleteIfExists(logFilePath);
                break;
            } catch (IOException e) {
                if (i == maxRetries - 1) {
                    System.err.println("Failed to delete test log file after " + maxRetries + " attempts: " + e.getMessage());
                } else {
                    try {
                        Thread.sleep(retryDelay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
    }

    @Order(1)
    @Test
    void testSingletonInstance() {
        LoggerClass secondLogger = LoggerClass.getLogger(TEST_CLASS_NAME);
        assertSame(logger, secondLogger, "Logger instances should be the same");
    }

    @Order(2)
    @Test
    void testLogLevels() throws IOException, InterruptedException {
        // Verschiedene Log-Level testen
        logger.logInfo("Info message");
        logger.logDebug("Debug message");
        logger.logWarning("Warning message");
        logger.logError("Error message");

        // Warten bis alle Nachrichten geschrieben wurden
        int maxWaitTime = 10; // Sekunden
        int currentWait = 0;
        boolean allMessagesLogged = false;

        while (currentWait < maxWaitTime && !allMessagesLogged) {
            TimeUnit.SECONDS.sleep(1);
            currentWait++;

            List<String> logLines = Files.readAllLines(logFilePath);
            if (logLines.size() == 4) {
                allMessagesLogged = true;

                // Prüfen der Log-Nachrichten
                assertTrue(logLines.get(0).contains("[INFO]"), "First message should be INFO");
                assertTrue(logLines.get(1).contains("[DEBUG]"), "Second message should be DEBUG");
                assertTrue(logLines.get(2).contains("[WARNING]"), "Third message should be WARNING");
                assertTrue(logLines.get(3).contains("[ERROR]"), "Fourth message should be ERROR");
            }
        }

        if (!allMessagesLogged) {
            fail("Not all messages were logged within " + maxWaitTime + " seconds");
        }
    }

    @Order(3)
    @Test
    void testQueueBackpressure() throws InterruptedException {
        // Queue mit mehr Nachrichten füllen als MAX_LOG_SIZE
        int extraMessages = 10;
        for (int i = 0; i < config.getMaxLogSize() + extraMessages; i++) {
            logger.logInfo("Message " + i);
        }

        // Warten, bis der Consumer die Nachrichten verarbeitet hat
        TimeUnit.SECONDS.sleep(config.getMaxConsumerSleepTime() + 1);

        // Prüfen, ob die Log-Datei existiert
        assertTrue(Files.exists(logFilePath), "Log file should exist");
    }

    @Order(4)
    @Test
    void testGracefulShutdown() throws IOException, InterruptedException {
        // Einige Nachrichten loggen
        for (int i = 0; i < 5; i++) {
            logger.logInfo("Shutdown test message " + i);
        }

        // Logger herunterfahren
        logger.shutdown();

        // Kurz warten und dann prüfen, ob alle Nachrichten geschrieben wurden
        TimeUnit.SECONDS.sleep(1);

        List<String> logLines = Files.readAllLines(logFilePath);
        assertEquals(5, logLines.size(), "All messages should be written before shutdown");
    }

    @Order(5)
    @Test
    void testConcurrentLogging() throws InterruptedException {
        int numberOfThreads = 10;
        int messagesPerThread = 100;
        Thread[] threads = new Thread[numberOfThreads];

        // Mehrere Threads erstellen, die gleichzeitig loggen
        for (int i = 0; i < numberOfThreads; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < messagesPerThread; j++) {
                    logger.logInfo("Thread " + threadId + " Message " + j);
                }
            });
            threads[i].start();
        }

        // Auf alle Threads warten
        for (Thread thread : threads) {
            thread.join();
        }

        // Warten, bis der Consumer die Nachrichten verarbeitet hat
        TimeUnit.SECONDS.sleep(config.getMaxConsumerSleepTime() + 1);

        // Prüfen, ob die Log-Datei existiert und Nachrichten enthält
        assertTrue(Files.exists(logFilePath), "Log file should exist after concurrent logging");
    }

    @Order(6)
    @Test
    void testMessageFormat() throws IOException, InterruptedException {
        String testMessage = "Test message";
        logger.logInfo(testMessage);

        // Warten, bis der Consumer die Nachricht geschrieben hat
        TimeUnit.SECONDS.sleep(config.getMaxConsumerSleepTime() + 1);

        List<String> logLines = Files.readAllLines(logFilePath);
        assertEquals(1, logLines.size(), "Should have exactly one log message");

        String logLine = logLines.get(0);
        assertTrue(logLine.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2} \\[INFO\\] TestClass : Test message"),
                "Log message format should match expected pattern");
    }
}

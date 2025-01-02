package org.example.logger;

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
    private static Logger logger;
    private static Path logFilePath;


    @BeforeEach
    void setUp() throws IOException {
        // Logger-Instanz für den Test erstellen
        logger = Logger.getLogger(TEST_CLASS_NAME);
        logFilePath = Paths.get(Config.LOG_FILE_PATH + TEST_CLASS_NAME + "-" +
                Config.FILE_NAME_FORMATTER.format(java.time.LocalDateTime.now()) + ".log");

        Files.createDirectories(logFilePath.getParent());
        if (Files.exists(logFilePath))
            Files.write(logFilePath, new byte[0], StandardOpenOption.TRUNCATE_EXISTING);
        else
            Files.createFile(logFilePath);
    }

    @AfterEach
    void resetEnv() {
        // Logger herunterfahren und Datei löschen
        logger.shutdown();
        try {
            Files.deleteIfExists(logFilePath);
        } catch (IOException e) {
            System.err.println("Failed to delete test log file: " + e.getMessage());
        }
    }

    @Order(1)
    @Test
    void testSingletonInstance() {
        Logger secondLogger = Logger.getLogger(TEST_CLASS_NAME);
        assertSame(logger, secondLogger, "Logger instances should be the same");
    }

    @Order(2)
    @Test
    void testLogLevels() throws IOException, InterruptedException {
        // Verschiedene Log-Level testen
        logger.logInfo("Info message", TEST_CLASS_NAME);
        logger.logDebug("Debug message", TEST_CLASS_NAME);
        logger.logWarning("Warning message", TEST_CLASS_NAME);
        logger.logError("Error message", TEST_CLASS_NAME);

        // Warten, bis der Consumer die Nachrichten geschrieben hat
        TimeUnit.SECONDS.sleep(Config.MAX_CONSUMER_SLEEP_TIME + 1);

        // Log-Datei lesen und prüfen
        List<String> logLines = Files.readAllLines(logFilePath);
        assertEquals(4, logLines.size(), "Should have logged 4 messages");

        assertTrue(logLines.get(0).contains("[INFO]"), "First message should be INFO");
        assertTrue(logLines.get(1).contains("[DEBUG]"), "Second message should be DEBUG");
        assertTrue(logLines.get(2).contains("[WARNING]"), "Third message should be WARNING");
        assertTrue(logLines.get(3).contains("[ERROR]"), "Fourth message should be ERROR");
    }

    @Order(3)
    @Test
    void testQueueBackpressure() throws InterruptedException {
        // Queue mit mehr Nachrichten füllen als MAX_LOG_SIZE
        int extraMessages = 10;
        for (int i = 0; i < Config.MAX_LOG_SIZE + extraMessages; i++) {
            logger.logInfo("Message " + i, TEST_CLASS_NAME);
        }

        // Warten, bis der Consumer die Nachrichten verarbeitet hat
        TimeUnit.SECONDS.sleep(Config.MAX_CONSUMER_SLEEP_TIME + 1);

        // Prüfen, ob die Log-Datei existiert
        assertTrue(Files.exists(logFilePath), "Log file should exist");
    }

    @Order(4)
    @Test
    void testGracefulShutdown() throws IOException, InterruptedException {
        // Einige Nachrichten loggen
        for (int i = 0; i < 5; i++) {
            logger.logInfo("Shutdown test message " + i, TEST_CLASS_NAME);
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
                    logger.logInfo("Thread " + threadId + " Message " + j, TEST_CLASS_NAME);
                }
            });
            threads[i].start();
        }

        // Auf alle Threads warten
        for (Thread thread : threads) {
            thread.join();
        }

        // Warten, bis der Consumer die Nachrichten verarbeitet hat
        TimeUnit.SECONDS.sleep(Config.MAX_CONSUMER_SLEEP_TIME + 1);

        // Prüfen, ob die Log-Datei existiert und Nachrichten enthält
        assertTrue(Files.exists(logFilePath), "Log file should exist after concurrent logging");
    }

    @Order(6)
    @Test
    void testMessageFormat() throws IOException, InterruptedException {
        String testMessage = "Test message";
        logger.logInfo(testMessage, TEST_CLASS_NAME);

        // Warten, bis der Consumer die Nachricht geschrieben hat
        TimeUnit.SECONDS.sleep(Config.MAX_CONSUMER_SLEEP_TIME + 1);

        List<String> logLines = Files.readAllLines(logFilePath);
        assertEquals(1, logLines.size(), "Should have exactly one log message");

        String logLine = logLines.get(0);
        assertTrue(logLine.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2} \\[INFO\\] TestClass : Test message"),
                "Log message format should match expected pattern");
    }

}

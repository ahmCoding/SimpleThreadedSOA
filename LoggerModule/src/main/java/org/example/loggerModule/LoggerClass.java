package org.example.loggerModule;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.concurrent.*;

/**
 * Logger-Klasse, die Log-Nachrichten in eine Datei schreibt und als Producer/Consumer implementiert ist.
 * Die Klasse ist als Singleton implementiert, verwendet einen ThreadPoolExecutor als Consumer und die Producer und Consumer
 * sind durch eine BlockingQueue voneinander entkoppelt.
 */
public class LoggerClass {
    private static volatile LoggerClass instance;
    private static volatile boolean isRunning;
    private final ExecutorService consumer;
    // BlockingQueue für Backpressure
    private final BlockingQueue<LogMessage> queue;
    private final Path logFilePath;
    private final Config config;
    private static String loggingClass;

    private LoggerClass(String className) {
        this.config = Config.getInstance();
        this.logFilePath = Paths.get(config.getLogFilePath() + className + "-" +
                config.getFileNameFormatter().format(LocalDateTime.now()) + ".log");
        queue = new LinkedBlockingQueue<>(config.getMaxLogSize());
        consumer = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "Logger-Consumer");
            thread.setDaemon(true);
            return thread;
        });
        initialize();
        startConsumer();
    }

    private void initialize() {
        try {
            Files.createDirectories(logFilePath.getParent());
            if (Files.exists(logFilePath))
                Files.write(logFilePath, new byte[0], StandardOpenOption.TRUNCATE_EXISTING);
            else
                Files.createFile(logFilePath);

        } catch (IOException e) {
            System.err.println("Failed to initialize log file: " + e.getMessage());
        }
        isRunning = true;
    }

    /**
     * Gibt eine Instanz der Logger-Klasse zurück.
     *
     * @param className Der Name der Klasse, in deren Main-Methode die Logger-Klasse verwendet wird.
     *                  Die Logdatei wird mit diesem Namen erstellt.
     */
    public static LoggerClass getLogger(String className) {
        //Double-Checked Locking
        if (instance == null) {
            synchronized (LoggerClass.class) {
                if (instance == null) {
                    instance = new LoggerClass(className);
                }
            }
        }
        loggingClass = className;
        return instance;
    }


    /**
     * Startet den Consumer, der die Log-Nachrichten aus der Queue liest und in die Datei schreibt.
     */
    private void startConsumer() {
        consumer.submit(() -> {
            while (isRunning) {
                writeLogs();
                try {
                    TimeUnit.MILLISECONDS.sleep(config.getMaxConsumerSleepTime());
                    // wenn der isRunning-Flag gesetzt wir aber Thread schläft
                } catch (InterruptedException e) {
                    System.err.println("Logger: Consumer interrupted while sleeping");
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            // ein letztes Mal schreiben, bevor der Consumer beendet wird
            writeLogs();
        });
    }

    /**
     * Schreibt die Log-Nachrichten in die Datei.
     */
    private void writeLogs() {
        try (BufferedWriter writer = Files.newBufferedWriter(logFilePath, StandardOpenOption.CREATE, StandardOpenOption.APPEND);) {
            LogMessage logMessage = null;
            while ((logMessage = queue.poll()) != null) {
                writer.write(formatMessage(logMessage));
                writer.newLine();
            }
            writer.flush();
        } catch (IOException e) {
            System.err.println("Logger: Failed to write logs to file: " + e.getMessage());
        }
    }


    private String formatMessage(LogMessage logMessage) {
        return String.format("%s [%s] %s : %s", config.getDateFormatter().format(LocalDateTime.now()),
                logMessage.level, logMessage.loggingClass, logMessage.message);
    }

    /**
     * Loggt eine Nachricht mit dem angegebenen LogLevel und der Klasse, die die Nachricht loggt.
     *
     * @param message Die Nachricht, die geloggt werden soll.
     * @param level   Das LogLevel der Nachricht.
     */
    public void log(String message, LogLevel level) {
        if (isRunning) {
            LogMessage logMessage = new LogMessage(message, level, loggingClass);
            try {
                if (!queue.offer(logMessage, config.getMaxOfferTime(), TimeUnit.MILLISECONDS)) {
                    System.err.println("Logger: Queue is full, message is dropped");
                }
            } catch (InterruptedException e) {
                System.err.println("Logger: Interrupted while waiting for space in the queue");
                System.err.println("Dropping message: " + message);
                Thread.currentThread().interrupt();
            }
            return;
        }
        System.err.println("Logger: Logger is not running");
    }

    /**
     * Beendet den Logger und wartet auf die Beendigung des Consumers.
     * Graceful shutdown.
     */
    public void shutdown() {
        isRunning = false;
        consumer.shutdown();
        try {
            if (!consumer.awaitTermination(config.getMaxTimeForExecutorTermination(), TimeUnit.SECONDS)) {
                consumer.shutdownNow();
            }
        } catch (InterruptedException e) {
            System.err.println("Logger: Consumer interrupted while waiting for termination");
            consumer.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Setzt die Logger-Instanz zurück. Nur für Tests.
     */
    public static void reset() {
        if (instance != null) {
            instance.shutdown();
            instance = null;
        }
        isRunning = false;
    }

    /**
     * Loggt eine Nachricht mit dem LogLevel INFO.
     *
     * @param message Die Nachricht, die geloggt werden soll.
     */
    public void logInfo(String message) {
        log(message, LogLevel.INFO);
    }

    /**
     * Loggt eine Nachricht mit dem LogLevel DEBUG.
     *
     * @param message Die Nachricht, die geloggt werden soll.
     */
    public void logDebug(String message) {
        log(message, LogLevel.DEBUG);
    }

    /**
     * Loggt eine Nachricht mit dem LogLevel WARNING.
     *
     * @param message Die Nachricht, die geloggt werden soll.
     */
    public void logWarning(String message) {
        log(message, LogLevel.WARNING);
    }

    /**
     * Loggt eine Nachricht mit dem LogLevel ERROR.
     *
     * @param message Die Nachricht, die geloggt werden soll.
     */
    public void logError(String message) {
        log(message, LogLevel.ERROR);
    }

    /**
     * Gibt an, ob der Logger gerade läuft.
     *
     * @return true, wenn der Logger läuft, false sonst.
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * LogLevel für die Log-Nachrichten.
     */
    public enum LogLevel {
        INFO, DEBUG, WARNING, ERROR
    }

    private static class LogMessage {
        private final String message;
        private final LogLevel level;
        private final String loggingClass;

        public LogMessage(String message, LogLevel level, String className) {
            this.message = message;
            this.level = level;
            this.loggingClass = className;
        }
    }
}

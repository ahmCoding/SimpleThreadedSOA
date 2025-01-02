package org.example.logger;

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
public class Logger {
    private static volatile Logger instance;
    private static volatile boolean isRunning;
    private final ExecutorService consumer;
    // BlockingQueue für Backpressure
    private final BlockingQueue<LogMessage> queue;
    private final Path logFilePath;

    private Logger(String className) {
        this.logFilePath = Paths.get(Config.LOG_FILE_PATH + className + "-" +
                Config.FILE_NAME_FORMATTER.format(LocalDateTime.now()) + ".log");
        queue = new LinkedBlockingQueue<>(Config.MAX_LOG_SIZE);
        consumer = (ExecutorService) Executors.newSingleThreadExecutor(runnable -> {
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
     */
    public static Logger getLogger(String className) {
        //Double-Checked Locking
        if (instance == null) {
            synchronized (Logger.class) {
                if (instance == null) {
                    instance = new Logger(className);
                }
            }
        }
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
                    TimeUnit.SECONDS.sleep(Config.MAX_CONSUMER_SLEEP_TIME);
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
        return String.format("%s [%s] %s : %s", Config.DATE_FORMATTER.format(LocalDateTime.now()),
                logMessage.level, logMessage.loggingClass, logMessage.message);
    }

    /**
     * Loggt eine Nachricht mit dem angegebenen LogLevel und der Klasse, die die Nachricht loggt.
     *
     * @param message      Die Nachricht, die geloggt werden soll.
     * @param level        Das LogLevel der Nachricht.
     * @param loggingClass Der Name der Klasse, die die Nachricht loggt.
     */
    public void log(String message, LogLevel level, String loggingClass) {
        LogMessage logMessage = new LogMessage(message, level, loggingClass);
        try {
            if (!queue.offer(logMessage, Config.MAX_OFFER_TIME, TimeUnit.MILLISECONDS)) {
                System.err.println("Logger: Queue is full, message is dropped");
            }
        } catch (InterruptedException e) {
            System.err.println("Logger: Interrupted while waiting for space in the queue");
            Thread.currentThread().interrupt();
        }

    }

    /**
     * Beendet den Logger und wartet auf die Beendigung des Consumers.
     * Graceful shutdown.
     */
    public void shutdown() {
        isRunning = false;
        consumer.shutdown();
        try {
            if (!consumer.awaitTermination(Config.MAX_TIME_FOR_EXECECUTOR_TERMINATION, TimeUnit.SECONDS)) {
                consumer.shutdownNow();
            }
        } catch (InterruptedException e) {
            System.err.println("Logger: Consumer interrupted while waiting for termination");
            consumer.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Loggt eine Nachricht mit dem LogLevel INFO.
     *
     * @param message      Die Nachricht, die geloggt werden soll.
     * @param loggingClass Der Name der Klasse, die die Nachricht loggt.
     */
    public void logInfo(String message, String loggingClass) {
        log(message, LogLevel.INFO, loggingClass);
    }

    /**
     * Loggt eine Nachricht mit dem LogLevel DEBUG.
     *
     * @param message      Die Nachricht, die geloggt werden soll.
     * @param loggingClass Der Name der Klasse, die die Nachricht loggt.
     */
    public void logDebug(String message, String loggingClass) {
        log(message, LogLevel.DEBUG, loggingClass);
    }

    /**
     * Loggt eine Nachricht mit dem LogLevel WARNING.
     *
     * @param message      Die Nachricht, die geloggt werden soll.
     * @param loggingClass Der Name der Klasse, die die Nachricht loggt.
     */
    public void logWarning(String message, String loggingClass) {
        log(message, LogLevel.WARNING, loggingClass);
    }

    /**
     * Loggt eine Nachricht mit dem LogLevel ERROR.
     *
     * @param message      Die Nachricht, die geloggt werden soll.
     * @param loggingClass Der Name der Klasse, die die Nachricht loggt.
     */
    public void logError(String message, String loggingClass) {
        log(message, LogLevel.ERROR, loggingClass);
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

    /**
     * LogLevel für die Log-Nachrichten.
     */
    public enum LogLevel {
        INFO, DEBUG, WARNING, ERROR
    }
}

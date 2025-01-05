package org.example.loggerModule;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class Config {
    private static final Config INSTANCE = new Config();
    private final Properties properties;

    private final int maxLogSize;
    private final long maxOfferTime;
    private final DateTimeFormatter dateFormatter;
    private final DateTimeFormatter fileNameFormatter;
    private final long maxConsumerSleepTime;
    private final long maxTimeForExecutorTermination;
    private final String logFilePath;

    private Config() {
        properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("logger.properties")) {
            if (input == null) {
                throw new RuntimeException("Unable to find logger.properties");
            }
            properties.load(input);
        } catch (IOException ex) {
            throw new RuntimeException("Error loading logger properties", ex);
        }

        maxLogSize = Integer.parseInt(properties.getProperty("max.log.size"));
        maxOfferTime = Long.parseLong(properties.getProperty("max.offer.time"));
        dateFormatter = DateTimeFormatter.ofPattern(properties.getProperty("date.format"));
        fileNameFormatter = DateTimeFormatter.ofPattern(properties.getProperty("file.name.format"));
        maxConsumerSleepTime = Long.parseLong(properties.getProperty("max.consumer.sleep.time"));
        maxTimeForExecutorTermination = maxConsumerSleepTime * 100;

        String userDir = System.getProperty("user.dir");
        logFilePath = Paths.get(userDir)
                .resolve(properties.getProperty("log.file.path"))
                .toAbsolutePath()
                .toString() + FileSystems.getDefault().getSeparator();
    }

    public static Config getInstance() {
        return INSTANCE;
    }

    public int getMaxLogSize() {
        return maxLogSize;
    }

    public long getMaxOfferTime() {
        return maxOfferTime;
    }

    public DateTimeFormatter getDateFormatter() {
        return dateFormatter;
    }

    public DateTimeFormatter getFileNameFormatter() {
        return fileNameFormatter;
    }

    public long getMaxConsumerSleepTime() {
        return maxConsumerSleepTime;
    }

    public long getMaxTimeForExecutorTermination() {
        return maxTimeForExecutorTermination;
    }

    public String getLogFilePath() {
        return logFilePath;
    }


    // for testing purposes
    public void printLogPath() {
        System.out.println("Current log directory path: " + logFilePath);
        System.out.println("User directory: " + System.getProperty("user.dir"));
    }
}

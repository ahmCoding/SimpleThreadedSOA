package org.example.logger;

import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;

public class Config {
    public static final int MAX_LOG_SIZE = 256;
    public static final long MAX_OFFER_TIME = 100;
    public static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter FILE_NAME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");


    public static final long MAX_CONSUMER_SLEEP_TIME = 1;
    public static final long MAX_TIME_FOR_EXECECUTOR_TERMINATION = MAX_CONSUMER_SLEEP_TIME * 100;
    private static final String USER_DIR = System.getProperty("user.dir");
    public static final String LOG_FILE_PATH = Paths.get(USER_DIR)
            .resolve("logs").toAbsolutePath()
            .toString() + FileSystems.getDefault().getSeparator();

    // for testing purposes
    public static void printLogPath() {
        System.out.println("Current log directory path: " + LOG_FILE_PATH);
        System.out.println("User directory: " + USER_DIR);
    }
}

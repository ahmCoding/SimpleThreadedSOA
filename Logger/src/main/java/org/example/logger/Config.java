package org.example.logger;

import java.time.format.DateTimeFormatter;

public class Config {
    public static final int MAX_LOG_SIZE = 256;
    public static final long MAX_OFFER_TIME = 100;
    public static final String LOG_FILE_PATH = "./logs/";
    public static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter FILE_NAME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");


    public static final long MAX_CONSUMER_SLEEP_TIME = 5;
    public static final long MAX_TIME_FOR_EXCECUTOR_TERMINATION = MAX_CONSUMER_SLEEP_TIME * 10;
}

package org.example.logger;

public class Logger {
    private static volatile Logger instance;


    public enum LogLevel {
        INFO, DEBUG, WARNING, ERROR
    }
}

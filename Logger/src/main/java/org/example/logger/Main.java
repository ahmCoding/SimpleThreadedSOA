package org.example.logger;

public class Main {
    public static void main(String[] args) {
        Logger logger = Logger.getLogger("Main");
        logger.logInfo("Info message", Main.class.getName());
        logger.logDebug("Debug message", Main.class.getName());
        logger.logWarning("Warning message", Main.class.getName());
        logger.logError("Error message", Main.class.getName());
        logger.shutdown();
        System.out.println("Logger has been shut down");
    }
}

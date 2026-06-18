package com.example;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.Level;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Multithreaded application with Log4j 2 logging to console and files
 * Command-line parameter: number of threads (default: 3)
 * Main thread writes to p-<date>.log
 * Worker thread n writes to p-<date>-n.log
 */
public class MultiThreadedApp {
    private static final String LOG_DIR = "logs";
    private static final String LOG_PATTERN = "%d{yyyy-MM-dd HH:mm:ss} [%-5p] [%t] %c{1} - %m%n";
    
    private static Logger mainLogger;
    private static String dateStr;

    public static void main(String[] args) throws InterruptedException {
        // Parse command-line argument for number of threads
        int numThreads = 3; // default
        if (args.length > 0) {
            try {
                numThreads = Integer.parseInt(args[0]);
                if (numThreads < 1) {
                    System.err.println("Number of threads must be >= 1");
                    numThreads = 3;
                }
            } catch (NumberFormatException e) {
                System.err.println("Invalid thread count: " + args[0] + ". Using default: 3");
            }
        }

        // Generate date string for file naming
        dateStr = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());

        // Configure Log4j 2 dynamically
        configureLogging(numThreads, dateStr);

        mainLogger = LogManager.getLogger(MultiThreadedApp.class);

        mainLogger.info("========================================");
        mainLogger.info("Application started at: {}", new Date());
        mainLogger.info("Date prefix: p-{}", dateStr);
        mainLogger.info("Number of worker threads: {}", numThreads);
        mainLogger.info("========================================");

        // Create and start worker threads
        Thread[] threads = new Thread[numThreads];
        for (int i = 1; i <= numThreads; i++) {
            threads[i - 1] = new WorkerThread(i, dateStr);
            threads[i - 1].start();
        }

        // Main thread performs some work
        for (int j = 0; j < 5; j++) {
            mainLogger.info("[MAIN] Processing task {} of 5", j + 1);
            Thread.sleep(1000);
        }

        mainLogger.info("[MAIN] Waiting for worker threads to complete...");

        // Wait for all worker threads to complete
        for (int i = 0; i < numThreads; i++) {
            threads[i].join();
        }

        mainLogger.info("========================================");
        mainLogger.info("[MAIN] All threads completed successfully");
        mainLogger.info("Application ended at: {}", new Date());
        mainLogger.info("========================================");
    }

    /**
     * Configure Log4j 2 appenders dynamically based on thread count
     */
    private static void configureLogging(int numThreads, String dateStr) {
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration config = context.getConfiguration();

        // Create pattern layout
        PatternLayout layout = PatternLayout.newBuilder()
                .withConfiguration(config)
                .withPattern(LOG_PATTERN)
                .build();

        // Create console appender
        ConsoleAppender console = ConsoleAppender.createDefaultAppenderForLayout(layout);
        console.start();
        config.addAppender(console);

        // Create main file appender
        String mainLogFile = LOG_DIR + "/p-" + dateStr + ".log";
        FileAppender mainFile = FileAppender.newBuilder()
                .setConfiguration(config)
                .setName("mainFile")
                .setLayout(layout)
                .withFileName(mainLogFile)
                .withAppend(false)
                .build();
        mainFile.start();
        config.addAppender(mainFile);

        // Update root logger
        LoggerConfig rootConfig = config.getRootLogger();
        rootConfig.setLevel(Level.INFO);
        rootConfig.addAppender(console, Level.INFO, null);
        rootConfig.addAppender(mainFile, Level.INFO, null);

        // Create thread-specific loggers and appenders
        for (int i = 1; i <= numThreads; i++) {
            String loggerName = "Thread-" + i;
            String threadLogFile = LOG_DIR + "/p-" + dateStr + "-" + i + ".log";

            FileAppender threadFile = FileAppender.newBuilder()
                    .setConfiguration(config)
                    .setName("thread" + i + "File")
                    .setLayout(layout)
                    .withFileName(threadLogFile)
                    .withAppend(false)
                    .build();
            threadFile.start();
            config.addAppender(threadFile);

            LoggerConfig threadLoggerConfig = new LoggerConfig(loggerName, Level.INFO, false);
            threadLoggerConfig.addAppender(threadFile, Level.INFO, null);
            config.addLogger(loggerName, threadLoggerConfig);
        }

        context.updateLoggers();
    }

    /**
     * Worker thread that logs to its own file
     */
    static class WorkerThread extends Thread {
        private final int threadNumber;
        private final String dateStr;
        private final Logger threadLogger;

        public WorkerThread(int threadNumber, String dateStr) {
            this.threadNumber = threadNumber;
            this.dateStr = dateStr;
            this.setName("WorkerThread-" + threadNumber);
            // Each thread gets its own logger named Thread-N
            this.threadLogger = LogManager.getLogger("Thread-" + threadNumber);
        }

        @Override
        public void run() {
            threadLogger.info("[THREAD-{}] Worker thread started", threadNumber);
            threadLogger.info("[THREAD-{}] Thread name: {}", threadNumber, Thread.currentThread().getName());

            try {
                // Simulate work with multiple iterations
                for (int i = 1; i <= 5; i++) {
                    threadLogger.info("[THREAD-{}] Performing task {} of 5", threadNumber, i);

                    // Simulate processing
                    long startTime = System.currentTimeMillis();
                    Thread.sleep(500 + (threadNumber * 100)); // Variable sleep time
                    long duration = System.currentTimeMillis() - startTime;

                    threadLogger.info("[THREAD-{}] Task {} completed in {} ms", threadNumber, i, duration);
                }

                threadLogger.info("[THREAD-{}] Worker thread completed successfully", threadNumber);
            } catch (InterruptedException e) {
                threadLogger.error("[THREAD-{}] Thread interrupted", threadNumber, e);
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                threadLogger.error("[THREAD-{}] An error occurred", threadNumber, e);
            }
        }
    }
}

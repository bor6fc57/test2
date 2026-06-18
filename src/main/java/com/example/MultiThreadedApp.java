package com.example;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Multithreaded application with SLF4J logging to console and files
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

        // Configure Log4j dynamically
        configureLogging(numThreads, dateStr);

        mainLogger = Logger.getLogger(MultiThreadedApp.class);

        mainLogger.info("========================================");
        mainLogger.info("Application started at: " + new Date());
        mainLogger.info("Date prefix: p-" + dateStr);
        mainLogger.info("Number of worker threads: " + numThreads);
        mainLogger.info("========================================");

        // Create and start worker threads
        Thread[] threads = new Thread[numThreads];
        for (int i = 1; i <= numThreads; i++) {
            threads[i - 1] = new WorkerThread(i, dateStr);
            threads[i - 1].start();
        }

        // Main thread performs some work
        for (int j = 0; j < 5; j++) {
            mainLogger.info("[MAIN] Processing task " + (j + 1) + " of 5");
            Thread.sleep(1000);
        }

        mainLogger.info("[MAIN] Waiting for worker threads to complete...");

        // Wait for all worker threads to complete
        for (int i = 0; i < numThreads; i++) {
            threads[i].join();
        }

        mainLogger.info("========================================");
        mainLogger.info("[MAIN] All threads completed successfully");
        mainLogger.info("Application ended at: " + new Date());
        mainLogger.info("========================================");
    }

    /**
     * Configure Log4j appenders dynamically based on thread count
     */
    private static void configureLogging(int numThreads, String dateStr) {
        Logger rootLogger = Logger.getRootLogger();
        rootLogger.removeAllAppenders();

        // Pattern layout
        Layout layout = new PatternLayout(LOG_PATTERN);

        // Console appender
        ConsoleAppender console = new ConsoleAppender(layout);
        console.setName("console");
        rootLogger.addAppender(console);

        // Main file appender
        try {
            String mainLogFile = LOG_DIR + "/p-" + dateStr + ".log";
            FileAppender mainFile = new FileAppender(layout, mainLogFile, false);
            mainFile.setName("mainFile");
            rootLogger.addAppender(mainFile);
        } catch (IOException e) {
            System.err.println("Error creating main log file: " + e.getMessage());
        }

        rootLogger.setLevel(org.apache.log4j.Level.INFO);

        // Create thread-specific loggers and appenders
        for (int i = 1; i <= numThreads; i++) {
            String loggerName = "Thread-" + i;
            Logger threadLogger = Logger.getLogger(loggerName);
            threadLogger.setLevel(org.apache.log4j.Level.INFO);
            threadLogger.setAdditivity(false); // Don't propagate to root logger

            try {
                String threadLogFile = LOG_DIR + "/p-" + dateStr + "-" + i + ".log";
                FileAppender threadFile = new FileAppender(layout, threadLogFile, false);
                threadFile.setName("thread" + i + "File");
                threadLogger.addAppender(threadFile);
            } catch (IOException e) {
                System.err.println("Error creating thread " + i + " log file: " + e.getMessage());
            }
        }
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
            this.threadLogger = Logger.getLogger("Thread-" + threadNumber);
        }

        @Override
        public void run() {
            threadLogger.info("[THREAD-" + threadNumber + "] Worker thread started");
            threadLogger.info("[THREAD-" + threadNumber + "] Thread name: " + Thread.currentThread().getName());

            try {
                // Simulate work with multiple iterations
                for (int i = 1; i <= 5; i++) {
                    threadLogger.info("[THREAD-" + threadNumber + "] Performing task " + i + " of 5");

                    // Simulate processing
                    long startTime = System.currentTimeMillis();
                    Thread.sleep(500 + (threadNumber * 100)); // Variable sleep time
                    long duration = System.currentTimeMillis() - startTime;

                    threadLogger.info("[THREAD-" + threadNumber + "] Task " + i + " completed in " + duration + " ms");
                }

                threadLogger.info("[THREAD-" + threadNumber + "] Worker thread completed successfully");
            } catch (InterruptedException e) {
                threadLogger.error("[THREAD-" + threadNumber + "] Thread interrupted", e);
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                threadLogger.error("[THREAD-" + threadNumber + "] An error occurred", e);
            }
        }
    }
}

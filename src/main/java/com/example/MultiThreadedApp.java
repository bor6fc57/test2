package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Multithreaded application with SLF4J logging to console and files
 * Main thread writes to p-<date>.log
 * Worker threads write to p-<date>-n.log (where n is thread number)
 */
public class MultiThreadedApp {
    private static final Logger logger = LoggerFactory.getLogger(MultiThreadedApp.class);
    private static final int NUM_THREADS = 3;
    private static final String LOG_DIR = "logs";

    public static void main(String[] args) throws InterruptedException {
        String dateStr = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        
        logger.info("========================================");
        logger.info("Application started at: {}", new Date());
        logger.info("Date prefix: p-{}", dateStr);
        logger.info("Number of worker threads: {}", NUM_THREADS);
        logger.info("========================================");

        // Create and start worker threads
        Thread[] threads = new Thread[NUM_THREADS];
        for (int i = 1; i <= NUM_THREADS; i++) {
            threads[i - 1] = new WorkerThread(i, dateStr);
            threads[i - 1].start();
        }

        // Main thread performs some work
        for (int j = 0; j < 5; j++) {
            logger.info("[MAIN] Processing task {} of 5", j + 1);
            Thread.sleep(1000);
        }

        logger.info("[MAIN] Waiting for worker threads to complete...");

        // Wait for all worker threads to complete
        for (int i = 0; i < NUM_THREADS; i++) {
            threads[i].join();
        }

        logger.info("========================================");
        logger.info("[MAIN] All threads completed successfully");
        logger.info("Application ended at: {}", new Date());
        logger.info("========================================");
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
            // Each thread gets its own logger named after its thread
            this.threadLogger = LoggerFactory.getLogger("Thread-" + threadNumber);
        }

        @Override
        public void run() {
            threadLogger.info("[THREAD-{}] Worker thread started", threadNumber);
            threadLogger.info("[THREAD-{}] Thread name: {}", threadNumber, Thread.currentThread().getName());

            try {
                // Simulate work with multiple iterations
                for (int i = 1; i <= 5; i++) {
                    threadLogger.info("[THREAD-{}] Performing task {} of 5", threadNumber, i);
                    threadLogger.debug("[THREAD-{}] Debug info - iteration {}", threadNumber, i);
                    
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
# Multithreaded Java Application with SLF4J Logging

This is a multithreaded Java application demonstrating SLF4J logging with Log4j backend, writing to both console and separate log files for each thread.

## Features

- **SLF4J with Log4j**: Using SLF4J API with Log4j 1.x binding
- **Console Logging**: All logs also output to console for real-time monitoring
- **Thread-specific Log Files**: 
  - Main thread logs to: `logs/p-<timestamp>.log`
  - Worker thread N logs to: `logs/p-<timestamp>-N.log`
- **External Configuration**: `log4j.properties` is located outside the resources directory
- **Multiple Worker Threads**: 3 concurrent worker threads performing tasks
- **Proper Thread Management**: Main thread waits for all worker threads to complete

## Project Structure

```
.
├── pom.xml                              # Maven configuration
├── log4j.properties                     # External Log4j configuration (NOT in resources)
├── setup.sh                             # Setup and run script (Linux/Mac)
├── setup.bat                            # Setup and run script (Windows)
├── src/main/java/com/example/
│   └── MultiThreadedApp.java           # Main application with worker threads
└── logs/                                # Log files directory (created at runtime)
```

## Prerequisites

- Java 11 or higher
- Maven 3.6+

## Building

```bash
mvn clean package
```

## Running

### Option 1: Using the setup script (Linux/Mac)

```bash
chmod +x setup.sh
./setup.sh
```

### Option 2: Using the setup script (Windows)

```cmd
setup.bat
```

### Option 3: Manual run (Linux/Mac)

```bash
mkdir -p logs

# Build
mvn clean package -DskipTests

# Run with external log4j properties
java -Dlog4j.configuration=file:./log4j.properties \
     -cp "target/multithreaded-logger-1.0-SNAPSHOT-jar-with-dependencies.jar" \
     com.example.MultiThreadedApp
```

### Option 4: Manual run (Windows)

```cmd
mkdir logs
mvn clean package -DskipTests

java -Dlog4j.configuration=file:./log4j.properties ^
     -cp "target/multithreaded-logger-1.0-SNAPSHOT-jar-with-dependencies.jar" ^
     com.example.MultiThreadedApp
```

## Output

### Console Output
Logs appear in real-time on the console with format:
```
2026-06-18 14:30:45 [INFO ] [main] MultiThreadedApp - ========================================
2026-06-18 14:30:45 [INFO ] [main] MultiThreadedApp - Application started at: ...
2026-06-18 14:30:45 [INFO ] [WorkerThread-1] MultiThreadedApp - [THREAD-1] Worker thread started
```

### Log Files
After running, check the `logs/` directory:
```
logs/
├── p-2026-06-18_14-30-45.log        # Main thread logs
├── p-2026-06-18_14-30-45-1.log      # Worker thread 1 logs
├── p-2026-06-18_14-30-45-2.log      # Worker thread 2 logs
└── p-2026-06-18_14-30-45-3.log      # Worker thread 3 logs
```

## Configuration Details

### log4j.properties

- **Root Logger**: Outputs to both console and main file at INFO level
- **Thread Loggers**: Each thread has a dedicated logger (Thread-1, Thread-2, Thread-3)
- **Additivity**: Disabled for thread loggers to prevent duplicate entries
- **File Rotation**: Rolling file appender with 10MB max size
- **Pattern**: `%d{yyyy-MM-dd HH:mm:ss} [%-5p] [%t] %c{1} - %m%n`
  - Date/Time
  - Log Level (padded to 5 chars)
  - Thread name
  - Logger class name
  - Message

### Important Notes

1. **External log4j.properties**: The configuration file is NOT in the `src/main/resources` directory, but in the project root. This requires the `-Dlog4j.configuration=file:./log4j.properties` system property when running.

2. **Date Format in Filenames**: Each thread independently creates its log files with the timestamp from when the application started. The actual date is determined at runtime.

3. **Thread Safety**: Log4j is thread-safe, allowing concurrent logging from multiple threads without conflicts.

4. **Logger Names**: Each thread uses a logger named `Thread-N` which is configured separately in log4j.properties to route to its own file.

## How It Works

### Main Application (MultiThreadedApp.java)

1. **Initialization**: 
   - Generates a date string in format `yyyy-MM-dd_HH-mm-ss`
   - Creates logger for main thread
   - Logs startup information

2. **Thread Creation**:
   - Creates 3 worker threads
   - Each thread is passed the date string for consistent naming
   - Each thread gets a dedicated logger named `Thread-N`

3. **Main Thread Work**:
   - Performs 5 tasks with 1-second intervals
   - Each task is logged via the main logger

4. **Thread Synchronization**:
   - Main thread waits for all worker threads to complete using `join()`
   - Logs final completion message

### Worker Threads (WorkerThread inner class)

1. **Initialization**:
   - Each thread receives its number (1, 2, 3)
   - Creates its own logger named `Thread-N`
   - Sets thread name to `WorkerThread-N`

2. **Task Execution**:
   - Performs 5 tasks with variable delays (500 + thread_number*100 ms)
   - Logs each task start and completion with timing information

3. **Error Handling**:
   - Catches and logs InterruptedException
   - Catches and logs any other exceptions

### Log4j Configuration (log4j.properties)

**Root Logger**:
- Logs to console (real-time output)
- Logs to main file (`p-<timestamp>.log`)
- Default level: INFO

**Thread-Specific Loggers**:
- Each `Thread-N` logger routes to its own file
- Additivity disabled to prevent duplication in parent (root) logger
- Same formatting pattern as main file

**Pattern Layout**:
```
%d{yyyy-MM-dd HH:mm:ss} [%-5p] [%t] %c{1} - %m%n
```
- `%d`: Timestamp
- `%-5p`: Log level (padded)
- `%t`: Thread name
- `%c{1}`: Logger class name (short form)
- `%m`: Message
- `%n`: Newline

## Modifying Thread Count

To change the number of worker threads, modify the `NUM_THREADS` constant in `MultiThreadedApp.java`:

```java
private static final int NUM_THREADS = 5;  // Change from 3 to 5
```

Then add corresponding logger configuration to `log4j.properties` for each new thread:

```properties
# Thread-4 logger configuration
log4j.logger.Thread-4=INFO, thread4File
log4j.additivity.Thread-4=false
log4j.appender.thread4File=org.apache.log4j.RollingFileAppender
log4j.appender.thread4File.File=logs/p-<date>-4.log
log4j.appender.thread4File.MaxFileSize=10MB
log4j.appender.thread4File.MaxBackupIndex=5
log4j.appender.thread4File.layout=org.apache.log4j.PatternLayout
log4j.appender.thread4File.layout.ConversionPattern=%d{yyyy-MM-dd HH:mm:ss} [%-5p] [%t] %c{1} - %m%n
```

## Troubleshooting

| Issue | Solution |
|-------|----------|
| No logs appearing | Ensure `log4j.properties` is in the project root and the system property is set correctly: `-Dlog4j.configuration=file:./log4j.properties` |
| Logs not in files | Check that the `logs/` directory exists and has write permissions. The script should create it automatically. |
| Missing dependencies | Run `mvn dependency:resolve` to ensure all JAR files are downloaded |
| "Could not find log4j.properties" | Make sure you're running from the project root directory where `log4j.properties` is located |
| Files created in wrong location | Verify the file path in log4j.properties: `log4j.appender.mainFile.File=logs/p-<date>.log` |

## Dependencies

- **slf4j-api-1.7.36**: SLF4J API for logging abstraction
- **slf4j-log4j12-1.7.36**: Binding between SLF4J and Log4j 1.x
- **log4j-1.2.17**: Log4j logging framework

## License

This is a sample project for educational purposes.
# Multithreaded Java Application with SLF4J Logging

This is a multithreaded Java application demonstrating SLF4J logging with Log4j backend, writing to both console and separate log files for each thread.

## Features

- **Command-line Thread Count**: Specify number of threads as parameter (default: 3)
- **Dynamic Log4j Configuration**: Appenders created at runtime based on thread count
- **SLF4J with Log4j**: Using SLF4J API with Log4j 1.x binding
- **Console Logging**: All logs also output to console for real-time monitoring
- **Thread-specific Log Files**: 
  - Main thread logs to: `logs/p-<timestamp>.log`
  - Worker thread N logs to: `logs/p-<timestamp>-N.log`
- **External Configuration**: `log4j.properties` is located outside the resources directory
- **Proper Thread Management**: Main thread waits for all worker threads to complete

## Project Structure

```
.
├── pom.xml                              # Maven configuration
├── log4j.properties                     # Minimal external Log4j config (NOT in resources)
├── setup.sh                             # Setup and run script (Linux/Mac)
├── setup.bat                            # Setup and run script (Windows)
├── src/main/java/com/example/
│   └── MultiThreadedApp.java           # Main application with dynamic log config
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

# Run with default 3 threads
./setup.sh

# Run with custom number of threads
./setup.sh 5
```

### Option 2: Using the setup script (Windows)

```cmd
# Run with default 3 threads
setup.bat

# Run with custom number of threads
setup.bat 5
```

### Option 3: Manual run (Linux/Mac)

```bash
mkdir -p logs

# Build
mvn clean package -DskipTests

# Run with default 3 threads
java -Dlog4j.configuration=file:./log4j.properties \
     -cp "target/multithreaded-logger-1.0-SNAPSHOT-jar-with-dependencies.jar" \
     com.example.MultiThreadedApp

# Run with custom number of threads
java -Dlog4j.configuration=file:./log4j.properties \
     -cp "target/multithreaded-logger-1.0-SNAPSHOT-jar-with-dependencies.jar" \
     com.example.MultiThreadedApp 7
```

### Option 4: Manual run (Windows)

```cmd
mkdir logs
mvn clean package -DskipTests

# Run with default 3 threads
java -Dlog4j.configuration=file:./log4j.properties ^
     -cp "target/multithreaded-logger-1.0-SNAPSHOT-jar-with-dependencies.jar" ^
     com.example.MultiThreadedApp

# Run with custom number of threads
java -Dlog4j.configuration=file:./log4j.properties ^
     -cp "target/multithreaded-logger-1.0-SNAPSHOT-jar-with-dependencies.jar" ^
     com.example.MultiThreadedApp 7
```

## Output Example

### Running with 5 threads
```bash
./setup.sh 5
```

### Console Output
```
2026-06-18 14:30:45 [INFO ] [main] MultiThreadedApp - ========================================
2026-06-18 14:30:45 [INFO ] [main] MultiThreadedApp - Application started at: ...
2026-06-18 14:30:45 [INFO ] [main] MultiThreadedApp - Date prefix: p-2026-06-18_14-30-45
2026-06-18 14:30:45 [INFO ] [main] MultiThreadedApp - Number of worker threads: 5
2026-06-18 14:30:45 [INFO ] [main] MultiThreadedApp - ========================================
2026-06-18 14:30:45 [INFO ] [WorkerThread-1] MultiThreadedApp - [THREAD-1] Worker thread started
2026-06-18 14:30:45 [INFO ] [WorkerThread-2] MultiThreadedApp - [THREAD-2] Worker thread started
```

### Log Files Created
```
logs/
├── p-2026-06-18_14-30-45.log        # Main thread logs
├── p-2026-06-18_14-30-45-1.log      # Worker thread 1 logs
├── p-2026-06-18_14-30-45-2.log      # Worker thread 2 logs
├── p-2026-06-18_14-30-45-3.log      # Worker thread 3 logs
├── p-2026-06-18_14-30-45-4.log      # Worker thread 4 logs
└── p-2026-06-18_14-30-45-5.log      # Worker thread 5 logs
```

## How It Works

### Command-Line Parameter Handling

```java
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
```

### Dynamic Log4j Configuration

The `configureLogging(int numThreads, String dateStr)` method:

1. **Clears existing appenders** from root logger
2. **Creates console appender** for real-time output
3. **Creates main file appender** (`p-<timestamp>.log`)
4. **Dynamically creates thread-specific appenders** in a loop:
   - For each thread from 1 to numThreads
   - Creates logger named `Thread-N`
   - Creates file appender for `p-<timestamp>-N.log`
   - Sets additivity to false to prevent duplication

```java
private static void configureLogging(int numThreads, String dateStr) {
    Logger rootLogger = Logger.getRootLogger();
    rootLogger.removeAllAppenders();

    // ... console and main file appenders ...

    // Create thread-specific loggers and appenders
    for (int i = 1; i <= numThreads; i++) {
        String loggerName = "Thread-" + i;
        Logger threadLogger = Logger.getLogger(loggerName);
        threadLogger.setLevel(org.apache.log4j.Level.INFO);
        threadLogger.setAdditivity(false);

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
```

### Thread Execution

1. **Main thread**:
   - Accepts command-line parameter
   - Configures logging dynamically
   - Creates and starts N worker threads
   - Performs its own tasks
   - Waits for all workers to complete

2. **Worker threads**:
   - Use logger named `Thread-N`
   - Each writes to own file: `p-<timestamp>-N.log`
   - Perform simulated work (5 tasks with variable delays)
   - Log task start and completion with timing

## Configuration Details

### log4j.properties

Now minimal - only sets root logger level:
```properties
log4j.rootLogger=INFO
```

All appender configuration happens programmatically in `MultiThreadedApp.configureLogging()`.

### Pattern Layout

```
%d{yyyy-MM-dd HH:mm:ss} [%-5p] [%t] %c{1} - %m%n
```
- `%d`: Timestamp
- `%-5p`: Log level (left-padded to 5 chars)
- `%t`: Thread name
- `%c{1}`: Logger class name (short form)
- `%m`: Message
- `%n`: Newline

## Key Implementation Details

### Using Log4j Directly (Not SLF4J)

The application uses Log4j directly instead of SLF4J for logging because:
- Log4j provides programmatic appender configuration
- SLF4J is a facade and doesn't support dynamic appender creation
- Using `Logger.getLogger()` instead of `LoggerFactory.getLogger()`

### Thread Safety

Log4j is thread-safe for concurrent logging from multiple threads.

### File Name Generation

Each run creates new files with current timestamp:
- `p-2026-06-18_14-30-45.log` (main)
- `p-2026-06-18_14-30-45-1.log` (thread 1)
- `p-2026-06-18_14-30-45-2.log` (thread 2)
- etc.

Existing log files are never overwritten (FileAppender with `false` flag).

## Validation Examples

### Test with 3 threads (default)
```bash
./setup.sh
# Creates: p-<timestamp>.log, p-<timestamp>-1.log, p-<timestamp>-2.log, p-<timestamp>-3.log
```

### Test with 10 threads
```bash
./setup.sh 10
# Creates: p-<timestamp>.log, p-<timestamp>-1.log through p-<timestamp>-10.log
```

### Test with invalid input
```bash
./setup.sh abc
# Output: Invalid thread count: abc. Using default: 3
# Creates: p-<timestamp>.log, p-<timestamp>-1.log, p-<timestamp>-2.log, p-<timestamp>-3.log
```

### Test with 0 threads
```bash
./setup.sh 0
# Output: Number of threads must be >= 1
# Creates: p-<timestamp>.log only (no worker threads)
```

## Troubleshooting

| Issue | Solution |
|-------|----------|
| "Could not find log4j.properties" | Ensure you're running from project root where `log4j.properties` is located |
| Logs not in files | Check `logs/` directory exists with write permissions. Script creates it automatically. |
| Thread count not applied | Pass argument directly: `./setup.sh 5` or `java ... MultiThreadedApp 5` |
| Wrong number of log files | Verify thread count was accepted. Check console output shows "Number of worker threads: N" |
| No logs appearing at all | Verify `log4j.configuration` system property is set correctly |

## Dependencies

- **slf4j-api-1.7.36**: SLF4J API for logging abstraction
- **slf4j-log4j12-1.7.36**: Binding between SLF4J and Log4j 1.x
- **log4j-1.2.17**: Log4j logging framework

## License

This is a sample project for educational purposes.

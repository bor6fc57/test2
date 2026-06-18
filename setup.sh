#!/bin/bash

# Setup script for multithreaded Java application with SLF4J logging
# Usage: ./setup.sh [number_of_threads]
# Default: 3 threads

NUM_THREADS=${1:-3}

echo "Creating logs directory..."
mkdir -p logs

echo "Building application with Maven..."
mvn clean package -DskipTests

if [ $? -eq 0 ]; then
    echo ""
    echo "Build successful!"
    echo ""
    echo "Running the application with $NUM_THREADS threads..."
    echo ""
    
    # Run with external log4j properties file
    java -Dlog4j.configuration=file:./log4j.properties \
         -cp "target/multithreaded-logger-1.0-SNAPSHOT-jar-with-dependencies.jar" \
         com.example.MultiThreadedApp $NUM_THREADS
else
    echo "Build failed!"
    exit 1
fi

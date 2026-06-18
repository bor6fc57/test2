#!/bin/bash

# Setup script for multithreaded Java application with SLF4J logging

echo "Creating logs directory..."
mkdir -p logs

echo "Building application with Maven..."
mvn clean package -DskipTests

if [ $? -eq 0 ]; then
    echo ""
    echo "Build successful!"
    echo ""
    echo "Running the application..."
    echo ""
    
    # Run with external log4j properties file
    java -Dlog4j.configuration=file:./log4j.properties \
         -cp "target/multithreaded-logger-1.0-SNAPSHOT-jar-with-dependencies.jar" \
         com.example.MultiThreadedApp
else
    echo "Build failed!"
    exit 1
fi
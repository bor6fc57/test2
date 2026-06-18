@echo off
REM Setup script for multithreaded Java application with SLF4J logging

echo Creating logs directory...
if not exist "logs" mkdir logs

echo Building application with Maven...
call mvn clean package -DskipTests

if %ERRORLEVEL% EQU 0 (
    echo.
    echo Build successful!
    echo.
    echo Running the application...
    echo.
    
    REM Run with external log4j properties file
    java -Dlog4j.configuration=file:./log4j.properties ^
         -cp "target/multithreaded-logger-1.0-SNAPSHOT-jar-with-dependencies.jar" ^
         com.example.MultiThreadedApp
) else (
    echo Build failed!
    exit /b 1
)
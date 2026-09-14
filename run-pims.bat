@echo off
REM HealthFirst PIMS launcher (requires Java 21 or later).
java -jar healthfirst-pims-1.0.0.jar
if errorlevel 1 pause

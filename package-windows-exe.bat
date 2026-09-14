@echo off
REM Run this script on a Windows computer with JDK 21 and Maven installed.
REM It creates dist\YourName_pims\YourName_pims.exe plus its required runtime folder.
call mvn clean package
if errorlevel 1 exit /b 1
if not exist dist mkdir dist
jpackage --type app-image --name YourName_pims --input target --main-jar healthfirst-pims-1.0.0.jar --main-class com.healthfirst.pims.PimsApplication --dest dist --java-options "-Dfile.encoding=UTF-8"
if errorlevel 1 pause

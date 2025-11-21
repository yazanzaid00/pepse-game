@echo off
REM Simple build + run helper for Windows users

REM Clean bin
IF EXIST bin rmdir /S /Q bin
mkdir bin

REM Compile sources
dir /S /B pepse\*.java > sources.txt
javac -cp "lib\DanoGameLab.jar" -d bin @sources.txt
del sources.txt

REM Run the game
java -cp "bin;lib\DanoGameLab.jar" pepse.PepseGameManager

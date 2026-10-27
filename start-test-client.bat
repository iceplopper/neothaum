@echo off
rem Builds NeoThaum 4 from the current source and opens a test client at the main menu.
rem Double-click this file, or run it from a command prompt. Close the game window to stop it.
cd /d "%~dp0"
call gradlew.bat runClient --no-daemon
echo.
echo The test client has closed.
pause

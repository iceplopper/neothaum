@echo off
set GOURCE_EXE=%~dp0..\gource\gource.exe
if not exist "%GOURCE_EXE%" set GOURCE_EXE=C:\Users\harri\Documents\Phoenix Code\gource\gource.exe

cd /d "%~dp0"
echo ===================================================
echo  Thaumvibe 4 - Gource Development Visualization
echo ===================================================
echo Launching Gource...
echo.
echo Controls:
echo  - Space: Pause / Resume
echo  - Esc: Exit
echo  - Up/Down: Adjust playback speed
echo  - Left/Right: Fast-forward / Rewind
echo.

"%GOURCE_EXE%" -1280x720 ^
  --auto-skip-seconds 1 ^
  --seconds-per-day 4 ^
  --title "Thaumvibe 4 - Port Timeline (Claude Code & Gemini)" ^
  --file-idle-time 0 ^
  --hide filenames ^
  --highlight-users ^
  --font-size 18 ^
  --camera-mode overview

if errorlevel 1 (
    echo.
    echo Gource exited with code %errorlevel%.
    pause
)

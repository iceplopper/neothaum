@echo off
cd /d "%~dp0\.."
echo ===================================================
echo Pushing NeoThaum to https://github.com/iceplopper/neothaum
echo ===================================================
echo.
git push -u origin main
if %ERRORLEVEL% EQU 0 (
    echo.
    echo [OK] Main branch pushed successfully!
    echo Pushing Claude's working branch...
    git push -u origin claude/tc4-backlog-fixes
)
echo.
pause

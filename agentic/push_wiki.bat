@echo off
setlocal enabledelayedexpansion

echo ============================================================
echo   NeoThaum GitHub Wiki Deployment Tool
echo ============================================================
echo.

set REPO_DIR=%~dp0..
set WIKI_DIR=%REPO_DIR%\wiki
set TEMP_WIKI_GIT=%REPO_DIR%\agentic\tmp_wiki_repo

if not exist "%WIKI_DIR%" (
    echo Error: wiki directory not found at "%WIKI_DIR%"
    pause
    exit /b 1
)

echo Checking if GitHub Wiki is initialized on GitHub...
git ls-remote https://github.com/iceplopper/neothaum.wiki.git >nul 2>&1
if errorlevel 1 (
    echo.
    echo [IMPORTANT] The GitHub Wiki repository does not exist yet!
    echo.
    pause
    exit /b 1
)

echo GitHub Wiki repository detected!
echo Preparing local wiki git cache...

if exist "%TEMP_WIKI_GIT%" rd /s /q "%TEMP_WIKI_GIT%"
mkdir "%TEMP_WIKI_GIT%"

cd /d "%TEMP_WIKI_GIT%"
git clone https://github.com/iceplopper/neothaum.wiki.git .
git config user.name "Ice Plopper"
git config user.email "iceplopper@users.noreply.github.com"

copy "%WIKI_DIR%\*" "%TEMP_WIKI_GIT%\" >nul

git add -A
git commit -m "docs(wiki): sync NeoThaum wiki framework and pages"

echo.
echo Pushing wiki pages to GitHub...
git push origin master
if errorlevel 1 (
    git push origin main
)

echo.
echo ============================================================
echo   Wiki deployment complete!
echo   Visit: https://github.com/iceplopper/neothaum/wiki
echo ============================================================
cd /d "%REPO_DIR%"
if exist "%TEMP_WIKI_GIT%" rd /s /q "%TEMP_WIKI_GIT%"
pause

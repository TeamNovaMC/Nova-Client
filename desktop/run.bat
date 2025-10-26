@echo off
echo NovaClient Desktop Launcher
echo ==========================
echo.

java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Java is not installed or not in PATH
    echo Please install Java 17 or higher from https://adoptium.net/
    pause
    exit /b 1
)

echo Starting NovaClient Desktop...
echo.

cd /d "%~dp0build\libs"
if exist "NovaClient-Desktop-1.9.1.jar" (
    start javaw -jar NovaClient-Desktop-1.9.1.jar
    echo NovaClient Desktop started successfully!
) else (
    echo ERROR: NovaClient-Desktop-1.9.1.jar not found
    echo Please build the project first using: gradlew.bat :desktop:shadowJar
    pause
    exit /b 1
)

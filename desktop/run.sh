#!/bin/bash

echo "NovaClient Desktop Launcher"
echo "=========================="
echo ""

if ! command -v java &> /dev/null; then
    echo "ERROR: Java is not installed or not in PATH"
    echo "Please install Java 17 or higher"
    exit 1
fi

echo "Starting NovaClient Desktop..."
echo ""

cd "$(dirname "$0")/build/libs"
if [ -f "NovaClient-Desktop-1.9.1.jar" ]; then
    java -jar NovaClient-Desktop-1.9.1.jar &
    echo "NovaClient Desktop started successfully!"
else
    echo "ERROR: NovaClient-Desktop-1.9.1.jar not found"
    echo "Please build the project first using: ./gradlew :desktop:shadowJar"
    exit 1
fi

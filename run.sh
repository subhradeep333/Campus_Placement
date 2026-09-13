#!/bin/bash
set -e

if [ ! -d "bin" ]; then
    echo "Bin directory not found. Building project..."
    ./build.sh
fi

echo "🚀 Launching Campus Recruitment & Placement Portal (SQL Database Mode)..."
java -cp "lib/*:bin" Main

#!/bin/bash
set -e

echo "🔨 Compiling Java sources with SQLite JDBC library..."
mkdir -p bin
javac -cp "lib/*" -d bin $(find src -name "*.java")
echo "✅ Compilation successful! Executables built into ./bin"

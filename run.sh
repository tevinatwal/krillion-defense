#!/bin/bash
# Compiles and runs the game without Maven (just needs Java 17+)
cd "$(dirname "$0")"
rm -rf out
mkdir -p out
javac -d out $(find src/main/java -name "*.java") || exit 1
cp src/main/resources/* out/
java -cp out krilliondefense.ui.Main

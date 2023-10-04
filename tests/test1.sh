#!/bin/bash

#
# TEST 1
# 1 CONTENT SERVER & 1 GET CLIENT
#

# Enters the main directory containing all the files needed to run the codebase
cd ..

# Compiles the Java files
echo Compiling Files
pkill -f "java"
javac *.java

# Runs the Aggregation Server
echo Starting Aggregation Server
java AggregationServer &
sleep 1;

# Runs Content Server
echo Starting Content Server
java ContentServer baseline input.txt localhost:4567 &
sleep 1;

# Runs GET Client
echo Starting GET Client
java GETClient localhost:4567
sleep 1;

# Informs user that the test has completed
echo Test Completed

# Moves the excess .class files into their own directory to clean the code-space
mv *.class excess

# Kills all process containing "java"
pkill -f "java"
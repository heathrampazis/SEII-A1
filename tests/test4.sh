#!/bin/bash

#
# TEST 4
# MULTIPLE CONTENT SERVERS & MULTIPLE GET CLIENT
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

# Runs Content Server 1
echo Starting Content Server 2
java ContentServer baseline input.txt localhost:4567 &
sleep 1;

# Runs Content Server 2
echo Starting Content Server
java ContentServer baseline input.txt localhost:4567 &
sleep 1;

# Runs Content Server 3
echo Starting Content Server 3
java ContentServer baseline input.txt localhost:4567 &
sleep 1;

# Runs GET Client 1
echo Starting GET Client 1
java GETClient localhost:4567
sleep 1;

# Runs GET Client 2
echo Starting GET Client 2
java GETClient localhost:4567
sleep 1;

# Runs GET Client 3
echo Starting GET Client 3
java GETClient localhost:4567
sleep 1;

# Informs user that the test has completed
echo Test Completed

# Moves the excess .class files into their own directory to clean the code-space
mv *.class excess

# Kills all process containing "java"
pkill -f "java"
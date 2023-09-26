#!/bin/bash

echo Compiling files...
pkill -f "java"
javac *.java

mv *.class excess/

echo Starting aggregation server...
java AggregationServer &
sleep 1

echo Testing PUT from multiple content servers

echo Starting content server 1...
java ContentServer baseline basic.txt localhost:4567 &
sleep 1

echo Starting content server 2...
java ContentServer host2 basic.txt localhost:4567 &
sleep 1

echo Starting content server 3...
java ContentServer host3 basic.txt localhost:4567 &
sleep 1

echo Testing GET from one client

echo Starting GET client...
java GETClient localhost:4567
sleep 1

echo Test completed

pkill -f "java"
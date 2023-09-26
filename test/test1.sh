#!/bin/bash

cd ..

echo Compiling
pkill -f "java"
javac *.java

echo Starting aggregation server
java AggregationServer &

echo Starting Content Server
java ContentServer baseline basic.txt localhost:4567 &

echo Starting GET client
java GETClient localhost:4567

echo Test completed

mv *.class excess

pkill -f "java"



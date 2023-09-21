##!/usr/bin.env bash
echo "Starting Content Server..." && cd ..
java ContentServer << EOF & > output/PUTTestOutput.txt
baseline
basic.txt
localhost:4567
EOF
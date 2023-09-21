##!/usr/bin/env bash
echo "Testing basic connction" && cd ..
echo "Starting ContentServer..."
java ContentServer << EOF & > output/connectionTestOutput.txt
baseline
basic.txt
localhost:4567
EOF
sleep 1 && kill -s 2 9
echo "Starting GETClient..."
java GETClient << EOF & > output/connectionTestOutput.txt
localhost:4567
EOF

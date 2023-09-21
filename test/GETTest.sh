##!/usr/bin/env bash
echo "Starting GET Client..." && cd ..
java GETClient << EOF & > output/GETTestOutput.txt
localhost:4567
EOF
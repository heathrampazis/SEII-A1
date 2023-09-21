Steps to run : 

1. Compile ./run.sh

2. Run Aggregation Server:

java AggregationServer

Custom port? (y/n): 
n

3. Run Content Server

java ContentServer

Enter ID and Input File: 
baseline
basic.txt
Enter the Client Name and Port Number: 
localhost:4567

4. Run GET Client

java GETClient

Enter the Server Name and Port Number: 
localhost:4567
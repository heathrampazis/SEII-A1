//
//  LAMPORT CLOCK
//  Description : Handles all lamport clock timestamp manahement for each class.
//
public class LamportClock {

    // Variable to store the Lamport Clock time stamp
    public int currentTimeStamp;

    // Constructor for the Lamport Clock
    public LamportClock(int timeStamp) {
        currentTimeStamp = timeStamp;
    }

    // Increments the lamport clock time stamp based on the request
    public int increment(int requestTimeStamp) {
        currentTimeStamp = Integer.max(currentTimeStamp, requestTimeStamp);
        currentTimeStamp++;

        return currentTimeStamp;
    }

}
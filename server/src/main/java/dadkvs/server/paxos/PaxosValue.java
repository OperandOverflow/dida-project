package dadkvs.server.paxos;

public class PaxosValue {
    public int requestId;
    public int sequenceNumber;

    public PaxosValue(int requestId, int sequenceNumber) {
        this.requestId = requestId;
        this.sequenceNumber = sequenceNumber;
    }

    public int getRequestId() {
        return requestId;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }
}
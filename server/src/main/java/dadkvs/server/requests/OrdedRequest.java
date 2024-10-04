package dadkvs.server.requests;

/**
 * This class represent an ordered request by a sequence number
 */
public class OrdedRequest {

    /**
     * The sequence number of the request
     */
    public int sequenceNumber;

    /**
     * The request
     */
    public int requestId;

    public OrdedRequest() {
        this.sequenceNumber = -1;
        this.requestId = -1;
    }

    public OrdedRequest(int requestSeq, int requestId) {
        this.sequenceNumber = requestSeq;
        this.requestId = requestId;
    }

    public int getRequestSeq() {
        return sequenceNumber;
    }

    public int getRequestId() {
        return requestId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrdedRequest)) return false;

        OrdedRequest that = (OrdedRequest) o;

        if (sequenceNumber != that.sequenceNumber) return false;
        return requestId == that.requestId;
    }
}

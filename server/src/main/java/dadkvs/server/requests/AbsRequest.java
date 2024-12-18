package dadkvs.server.requests;

/**
 * An abstract class to represent a client request.
 */
public abstract class AbsRequest {

    protected int requestId;

    public AbsRequest(int reqid) {
        this.requestId = reqid;
    }

    /**
     * Get the request id.
     * @return The request id.
     */
    public int getRequestId() {
        return this.requestId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!AbsRequest.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        final AbsRequest other = (AbsRequest) obj;
        return this.requestId == other.requestId;
    }
}

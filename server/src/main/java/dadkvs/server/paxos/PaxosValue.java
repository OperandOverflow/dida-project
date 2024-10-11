package dadkvs.server.paxos;

import dadkvs.server.requests.OrdedRequest;

public class PaxosValue {
    private final OrdedRequest request;

    public PaxosValue(OrdedRequest request) {
        this.request = request;
    }

    public OrdedRequest getValue() {
        return request;
    }

    public int getValueId() {
        return request.getRequestId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PaxosValue that)) return false;

        return request.equals(that.request);
    }
}
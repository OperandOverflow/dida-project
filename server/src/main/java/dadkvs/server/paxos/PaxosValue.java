package dadkvs.server.paxos;

import dadkvs.server.requests.OrdedRequest;

public class PaxosValue {
    private OrdedRequest request;

    public PaxosValue(OrdedRequest request) {
        this.request = request;
    }

    public OrdedRequest getValue() {
        return request;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PaxosValue)) return false;

        PaxosValue that = (PaxosValue) o;

        return request.equals(that.request);
    }
}
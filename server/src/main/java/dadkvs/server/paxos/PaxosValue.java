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
}
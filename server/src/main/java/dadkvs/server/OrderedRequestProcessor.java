package dadkvs.server;

/**
 * This class processes the requests in the order received from the leader.
 */
public class OrderedRequestProcessor {

    private DadkvsServerState   server_state;

    public OrderedRequestProcessor(DadkvsServerState state) {
        this.server_state = state;
    }

    public VersionedValue processRead() {
        return null;
    }

    public boolean processCommit() {
        return false;
    }
}

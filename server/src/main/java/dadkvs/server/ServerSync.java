package dadkvs.server;

import dadkvs.server.paxos.PaxosValue;
import dadkvs.server.requests.OrdedRequest;

import java.util.List;

/**
 * This class handles the synchronization of requests between servers.
 */
public class ServerSync {

    private final ServerState server_state;

    private int sequence_number;

    public ServerSync(ServerState state) {
        this.server_state = state;
    }

    public synchronized void setStopSync(boolean stop) {
        server_state.paxos.setStop(stop);
    }

    /**
     * The leader sends the order of requests to other servers using
     * this method.
     * This method will automatically assign a sequence number to the
     * request and send it to all the servers.
     *
     * @param requestId The request id
     */
    public synchronized void sendReqOrder(int requestId) {
        this.sequence_number++;

        OrdedRequest ord_request = new OrdedRequest(this.sequence_number, requestId);
        PaxosValue value_proposed = new PaxosValue(ord_request);
        System.out.println("[Paxos] Starting Paxos");
        server_state.paxos.propose(value_proposed);
        System.out.println("[Paxos] Paxos Completed");
    }

    /**
     * This method receives the ordered requests from the leader
     * and adds them to the ordered request processor.
     * @param orderedRequests The list of ordered requests
     */
    public synchronized void receiveReqOrder(List<OrdedRequest> orderedRequests) {
        server_state.request_handler.addOrderedRequestList(orderedRequests);
    }
}

package dadkvs.server;

import dadkvs.server.requests.*;

import java.util.List;

public class RequestHandler {

    private final ServerState server_state;

    private final RequestQueue request_queue;

    private final OrderedRequestProcessor ordered_request_processor;

    public RequestHandler(ServerState state) {
        this.server_state = state;
        this.request_queue = new RequestQueue();
        this.ordered_request_processor = new OrderedRequestProcessor(state);
    }

    /**
     * Process a read request.
     * @param request The read request to process.
     * @return The value associated with the key or null if the key is not found.
     */
    public VersionedValue handleReadRequest(ReadRequest request) {
        this.server_state.i_am_leader_lock.readLock().lock();
        try {
            if (this.server_state.i_am_leader) {
                this.server_state.server_sync.sendReqOrder(request.getRequestId());
            }
        } finally {
            this.server_state.i_am_leader_lock.readLock().unlock();
        }

        this.request_queue.addRequest(request);
        VersionedValue value = this.ordered_request_processor.read(request);
        this.request_queue.removeRequest(request);
        return value;
    }

    /**
     * Process a commit request.
     * @param request The commit request to process.
     * @return True if the transaction commit was successfully processed, false otherwise.
     */
    public boolean handleCommitRequest(CommitRequest request) {
        this.server_state.i_am_leader_lock.readLock().lock();
        try {
            if (this.server_state.i_am_leader) {
                this.server_state.server_sync.sendReqOrder(request.getRequestId());
            }
        } finally {
            this.server_state.i_am_leader_lock.readLock().unlock();
        }

        this.request_queue.addRequest(request);
        boolean result = this.ordered_request_processor.committx(request);
        this.request_queue.removeRequest(request);
        return result;
    }

    /**
     * Tell the handler the order by which the requests should be processed.
     * @param ordedRequest The sequenced request.
     */
    public void addOrderedRequest(OrdedRequest ordedRequest) {
        this.ordered_request_processor.addReqOrder(ordedRequest);
    }

    /**
     * Tell the handler the order by which the requests should be processed.
     * @param orderedRequests The list of sequenced requests.
     */
    public void addOrderedRequestList(List<OrdedRequest> orderedRequests) {
        this.ordered_request_processor.addReqOrderList(orderedRequests);
    }

    public List<AbsRequest> getPendingRequests() {
        return this.request_queue.getAllRequests();
    }

    /**
     * This method serializes and sends all pending requests to other servers.
     * It's intended to remove the necessity to have ServerSync in ServerState.
     * This method is called when the server becomes the leader.
     */
    public void orderAllPendingRequests() {
        List<AbsRequest> pendingRequests = this.getPendingRequests();
        this.server_state.server_sync.setStopSync(false);
        for (AbsRequest request : pendingRequests) {
            this.server_state.server_sync.sendReqOrder(request.getRequestId());
        }
    }

    public void stopOrderRequests() {
        this.server_state.server_sync.setStopSync(true);
    }
}

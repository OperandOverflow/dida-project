package dadkvs.server;

import dadkvs.server.entities.AbsRequest;

import java.util.HashMap;

/**
 * This class is used to store requests until they are
 * processed in the correct order.
 * This class is not used in the current implementation.
 */
public class RequestQueue {

    /** The read request queue. */
    private final HashMap<Integer, AbsRequest> queue;

    public RequestQueue() {
        this.queue = new HashMap<Integer, AbsRequest>();
    }

    /**
     * Add a request to the queue.
     * @param request The request to add.
     */
    public void addRequest(AbsRequest request) {
        this.queue.put(request.getReqid(), request);
    }

    /**
     * Get a request from the queue.
     * @param reqid The request id.
     * @return The request.
     */
    public AbsRequest getRequest(int reqid) {
        return this.queue.get(reqid);
    }

    /**
     * Remove a request from the queue.
     * @param reqid The request id.
     */
    public void removeRequest(int reqid) {
        this.queue.remove(reqid);
    }

    /**
     * Check if the queue contains a request.
     * @param reqid The request id.
     * @return True if the queue contains the request, false otherwise.
     */
    public boolean containsRequest(int reqid) {
        return this.queue.containsKey(reqid);
    }
}

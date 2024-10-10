package dadkvs.server.requests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * This class is used to store requests until they are
 * processed in the correct order.
 * All methods are thread-safe.
 */
public class RequestQueue {

    /** The read request queue. */
    private final HashMap<Integer, AbsRequest> queue;

    /** The read write lock for the queue. */
    private final ReadWriteLock lock;

    public RequestQueue() {
        this.queue = new HashMap<Integer, AbsRequest>();
        this.lock = new ReentrantReadWriteLock();
    }

    /**
     * Add a request to the queue.
     * @param request The request to add.
     */
    public void addRequest(AbsRequest request) {
        this.lock.writeLock().lock();
        try {
            this.queue.put(request.getReqid(), request);
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    /**
     * Get a request from the queue without removing it.
     * @param requestId The request id.
     * @return The request.
     */
    public AbsRequest getRequest(int requestId) {
        this.lock.readLock().lock();
        AbsRequest request;
        try {
            request = this.queue.get(requestId);
        } finally {
            this.lock.readLock().unlock();
        }
        return request;
    }

    /**
     * Remove a request from the queue.
     * @param requestId The request id.
     */
    public void removeRequest(int requestId) {
        this.lock.writeLock().lock();
        try {
            this.queue.remove(requestId);
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    /**
     * Remove a request from the queue.
     * @param request The request.
     */
    public void removeRequest(AbsRequest request) {
        this.lock.writeLock().lock();
        try {
            this.queue.remove(request.getReqid());
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    /**
     * Check if the queue contains a request.
     * @param requestId The request id.
     * @return True if the queue contains the request, false otherwise.
     */
    public boolean containsRequest(int requestId) {
        this.lock.readLock().lock();
        boolean contains;
        try {
            contains = this.queue.containsKey(requestId);
        } finally {
            this.lock.readLock().unlock();
        }
        return contains;
    }

    /**
     * Get all requests in the queue.
     * @return The list of requests.
     */
    public List<AbsRequest> getAllRequests() {
        this.lock.readLock().lock();
        List<AbsRequest> requests;
        try {
            requests = new ArrayList<AbsRequest>(this.queue.values());
        } finally {
            this.lock.readLock().unlock();
        }
        return requests;
    }
}

package dadkvs.server;

import dadkvs.server.requests.CommitRequest;
import dadkvs.server.requests.ReadRequest;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * This class processes the requests in the order received from the leader.
 */
public class OrderedRequestProcessor {

    final ServerState server_state;

    /** The timestamp of when last write was done */
    private int timestamp;

    private final Queue<Integer> request_order;

    private final HashSet<Integer> request_order_dup_checker;

    private final Object queue_lock = new Object();

    /** The read-write lock for the key store */
    private final ReadWriteLock ks_rwlock;

    public OrderedRequestProcessor(ServerState state) {
        this.server_state = state;
        this.timestamp = 0;
        this.request_order_dup_checker = new HashSet<>();
        this.request_order = new LinkedList<>();
        this.ks_rwlock = new ReentrantReadWriteLock();
    }

    // ==============================================================================
    //                               Request Handling
    // ==============================================================================

    /**
     * Process the read request by the order received from the leader.
     * This method may block the thread if the leader doesn't
     * specify any order.
     * @param request The read request
     * @return The value associated with the key or null if the key is not found
     */
    public VersionedValue read(ReadRequest request) {
        // For debug purposes
        //System.out.printf("Read request %d entered the queue\n", request.getRequestId());

        // If the request is the next in the order, process it
        VersionedValue value;
        this.ks_rwlock.readLock().lock();
        try {
            value = processRead(request);
        } finally {
            this.ks_rwlock.readLock().unlock();
        }

        // Notify the next request in the order
        notifyOrderChange();

        // For debug purposes
        //System.out.printf("Read request %d processed\n", request.getRequestId());

        return value;
    }

    /**
     * This method processes the read request.
     * @param request The read request
     * @return The value associated with the key or null if the key is not found
     */
    private VersionedValue processRead(ReadRequest request) {
        int key = request.getKey();
        return this.server_state.store.read(key);
    }

    public boolean committx(CommitRequest request) {
        // For debug purposes
        //System.out.printf("[ORP] Commit request %d entered the queue\n", request.getRequestId());

        // Check if the request is the next in the order
        Integer nextRequest;
        synchronized (this.request_order) {
            nextRequest = this.request_order.peek();
        }
        while (nextRequest == null || nextRequest != request.getRequestId()) {
            // For debug purposes
            //System.out.printf("[ORP] Commit request %d waiting\n", request.getRequestId());

            // If not the next in the order, wait for the order to change
            waitForOrder();
            synchronized (this.request_order) {
                nextRequest = this.request_order.peek();
            }
        }

        // If the request is the next in the order, process it
        boolean result;
        this.ks_rwlock.writeLock().lock();
        try {
            result = processCommit(request);
        } finally {
            this.ks_rwlock.writeLock().unlock();
        }

        // Remove the request from the order queue
        synchronized (this.request_order) {
            this.request_order.poll();
        }

        // Notify the next request in the order
        notifyOrderChange();

        // For debug purposes
        //System.out.printf("[ORP] Commit request %d processed\n", request.getRequestId());

        return result;
    }

    private boolean processCommit(CommitRequest request) {
        int key1 = request.getKey1();
        int version1 = request.getVersion1();
        int key2 = request.getKey2();
        int version2 = request.getVersion2();
        int writekey = request.getWriteKey();
        int writeval = request.getWriteValue();

        this.timestamp++;
        TransactionRecord txrecord = new TransactionRecord(key1, version1, key2, version2, writekey, writeval, this.timestamp);
        return this.server_state.store.commit(txrecord);
    }

    // ==============================================================================
    //                                Synchronization
    // ==============================================================================

    public void addReqOrder(int requestId) {
        System.out.println("[ORP] Received order " + requestId);
        // Add the ordered request to the order list
        synchronized (this.request_order) {
            if (this.request_order_dup_checker.contains(requestId)) {
                System.out.println("[ORP] Duplicate order " + requestId);
                return;
            }
            this.request_order.add(requestId);
            this.request_order_dup_checker.add(requestId);
            System.out.println("[ORP] Next in queue: " + this.request_order.peek());
        }
        notifyOrderChange();
    }

    // ==============================================================================
    //                              Concurrency Control
    // ==============================================================================

    /**
     * This method is for threads to wait
     * for changes in the order list
     */
    private void waitForOrder() {
        synchronized (this.queue_lock) {
            try {
                this.queue_lock.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This method is called by thread when finished
     * processing the current request to wake up the
     * next thread waiting for the order list
     */
    private void notifyOrderChange() {
        synchronized (this.queue_lock) {
            this.queue_lock.notifyAll();
        }
    }
}

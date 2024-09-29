package dadkvs.server;

import dadkvs.server.entities.CommitRequest;
import dadkvs.server.entities.ReadRequest;
import dadkvs.DadkvsServerSync;

import java.util.List;
import java.util.PriorityQueue;

/**
 * This class processes the requests in the order received from the leader.
 */
public class OrderedRequestProcessor {

    final DadkvsServerState     server_state;

    /** The timestamp of when last write was done */
    private int                 timestamp;

    private final PriorityQueue<DadkvsServerSync.SequencedRequest> request_order;

    private final Object        order_lock = new Object();

    public OrderedRequestProcessor(DadkvsServerState state) {
        this.server_state = state;
        this.timestamp = 0;
        this.request_order = new PriorityQueue<DadkvsServerSync.SequencedRequest>(
                (o1, o2) -> o1.getRequestseq() - o2.getRequestseq()
        );
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
        System.out.printf("Read request %d entered the queue\n", request.getReqid());

        // Check if the request is the next in the order
        DadkvsServerSync.SequencedRequest nextRequest;
        synchronized (this.request_order) {
            nextRequest = this.request_order.peek();
        }
        while (nextRequest == null || nextRequest.getRequestid() != request.getReqid()) {
            // For debug purposes
            System.out.printf("Read request %d waiting\n", request.getReqid());

            // If not the next in the order, wait for the order to change
            waitForOrder();
            synchronized (this.request_order) {
                nextRequest = this.request_order.peek();
            }
        }

        // If the request is the next in the order, process it
        VersionedValue value = processRead(request);

        // Remove the request from the order queue
        synchronized (this.request_order) {
            this.request_order.remove(nextRequest);
        }

        // Notify the next request in the order
        notifyOrderChange();

        // For debug purposes
        System.out.printf("Read request %d processed\n", request.getReqid());

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
        System.out.printf("Commit request %d entered the queue\n", request.getReqid());

        // Check if the request is the next in the order
        DadkvsServerSync.SequencedRequest nextRequest;
        synchronized (this.request_order) {
            nextRequest = this.request_order.peek();
        }
        while (nextRequest == null || nextRequest.getRequestid() != request.getReqid()) {
            // For debug purposes
            System.out.printf("Commit request %d waiting\n", request.getReqid());

            // If not the next in the order, wait for the order to change
            waitForOrder();
            synchronized (this.request_order) {
                nextRequest = this.request_order.peek();
            }
        }

        // If the request is the next in the order, process it
        boolean result = processCommit(request);

        // Remove the request from the order queue
        synchronized (this.request_order) {
            this.request_order.remove(nextRequest);
        }

        // Notify the next request in the order
        notifyOrderChange();

        // For debug purposes
        System.out.printf("Commit request %d processed\n", request.getReqid());

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
        TransactionRecord txrecord = new TransactionRecord (key1, version1, key2, version2, writekey, writeval, this.timestamp);
        boolean result = this.server_state.store.commit (txrecord);
        return result;
    }

    // ==============================================================================
    //                                Synchronization
    // ==============================================================================

    public void addReqOrderList(List<DadkvsServerSync.SequencedRequest> orderedRequests) {
        // Add the ordered requests to the order list
        synchronized (this.request_order) {
            // TODO: verify if the request is already in the queue before adding
            this.request_order.addAll(orderedRequests);
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
        synchronized (this.order_lock) {
            try {
                this.order_lock.wait();
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
        synchronized (this.order_lock) {
            this.order_lock.notifyAll();
        }
    }
}

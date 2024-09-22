package dadkvs.server;

import dadkvs.DadkvsServerSync;
import dadkvs.DadkvsServerSyncServiceGrpc;

import java.util.List;

/**
 * This class implements the gRPC service for the server-to-server
 * synchronization of request orders.
 */
public class DadkvsServerSyncServiceImpl extends DadkvsServerSyncServiceGrpc.DadkvsServerSyncServiceImplBase {

    DadkvsServerState   server_state;

    /** The sequence number of the request order. */
    int                 sequence_number;

    public DadkvsServerSyncServiceImpl(DadkvsServerState state) {
        this.server_state = state;
        this.sequence_number = 0;
    }

    /**
     * This method receives the request order from the leader.
     * @param request The request order message
     * @param responseObserver The response observer
     */
    @Override
    public void receiveReqOrder(DadkvsServerSync.RequestOrder request, io.grpc.stub.StreamObserver<DadkvsServerSync.Empty> responseObserver) {

        // Obtain the list of sequenced requests from the message
        List<DadkvsServerSync.SequencedRequest> orderedRequests = request.getOrderedrequestsList();

        // Notify the request processor about incoming sequenced requests
        // TODO

        // Send empty response to the leader
        DadkvsServerSync.Empty response = DadkvsServerSync.Empty.getDefaultInstance();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * The leader sends the order of requests to other servers using
     * this method.
     */
    public void sendReqOrder() {

    }
}

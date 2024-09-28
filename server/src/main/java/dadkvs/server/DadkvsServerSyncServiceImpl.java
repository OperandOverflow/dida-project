package dadkvs.server;

import com.google.rpc.context.AttributeContext;
import dadkvs.DadkvsMain;
import dadkvs.DadkvsMainServiceGrpc;
import dadkvs.DadkvsServerSync;
import dadkvs.DadkvsServerSyncServiceGrpc;
import io.grpc.stub.StreamObserver;

import dadkvs.util.GenericResponseCollector;
import dadkvs.util.CollectorStreamObserver;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.*;

/**
 * This class implements the gRPC service for the server-to-server
 * synchronization of request orders.
 */
public class DadkvsServerSyncServiceImpl extends DadkvsServerSyncServiceGrpc.DadkvsServerSyncServiceImplBase {

    DadkvsServerState   server_state;

    /** The sequence number of the request order. */
    int sequence_number;
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
    public void receiveReqOrder(DadkvsServerSync.RequestOrder request, StreamObserver<DadkvsServerSync.Empty> responseObserver) {

        // Obtain the list of sequenced requests from the message
        List<DadkvsServerSync.SequencedRequest> orderedRequests = request.getOrderedrequestsList();

        // Notify the request processor about incoming sequenced requests
        this.server_state.ordered_request_processor.addReqOrderList(orderedRequests);

        // Send empty response to the leader
        DadkvsServerSync.Empty response = DadkvsServerSync.Empty.getDefaultInstance();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * The leader sends the order of requests to other servers using
     * this method.
     */
    public void sendReqOrder(int reqid) {
        this.sequence_number++;

        // Create the sequenced request
        DadkvsServerSync.SequencedRequest.Builder sequencedRequestBuilder = DadkvsServerSync.SequencedRequest.newBuilder();
        sequencedRequestBuilder.setRequestid(reqid).setRequestseq(this.sequence_number);

        List<DadkvsServerSync.SequencedRequest> sequencedRequests = new ArrayList<DadkvsServerSync.SequencedRequest>();
        sequencedRequests.add(sequencedRequestBuilder.build());

        DadkvsServerSync.RequestOrder.Builder requestOrderBuilder = DadkvsServerSync.RequestOrder.newBuilder();
        requestOrderBuilder.addAllOrderedrequests(sequencedRequests);

        DadkvsServerSync.RequestOrder requestOrder = requestOrderBuilder.build();

        ArrayList<DadkvsServerSync.Empty> responseList = new ArrayList<DadkvsServerSync.Empty>();
        GenericResponseCollector<DadkvsServerSync.Empty> responseCollector = new GenericResponseCollector<DadkvsServerSync.Empty>(responseList, 4);
        int replica_port = server_state.base_port + 1;
        String localhost = "localhost";
        for(int i = 1; i < 5; i++) {
            int port = replica_port + i;

            ManagedChannel channel = ManagedChannelBuilder.forAddress(localhost, port).usePlaintext().build();
            DadkvsServerSyncServiceGrpc.DadkvsServerSyncServiceStub stub = DadkvsServerSyncServiceGrpc.newStub(channel);
            StreamObserver<DadkvsServerSync.RequestOrder> reqOrder_observer = new CollectorStreamObserver<>(responseCollector);
            stub.receiveReqOrder(requestOrder, reqOrder_observer);

        }
    }
}

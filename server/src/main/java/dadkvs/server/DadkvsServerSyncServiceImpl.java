package dadkvs.server;

import com.google.rpc.context.AttributeContext;
import dadkvs.DadkvsMain;
import dadkvs.DadkvsMainServiceGrpc;
import dadkvs.DadkvsServerSync;
import dadkvs.DadkvsServerSyncServiceGrpc;
import dadkvs.DadkvsServerSyncServiceGrpc.DadkvsServerSyncServiceStub;
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

    /** Broadcast control variables */
    private final int   n_servers = 5;
    private ManagedChannel[] channels;
    private DadkvsServerSyncServiceStub[] async_stubs;

    public DadkvsServerSyncServiceImpl(DadkvsServerState state) {
        this.server_state = state;
        this.sequence_number = 0;
        initiate();
    }

    /**
     * This method initializes the gRPC channels and stubs for
     * the server-to-server communication.
     */
    private void initiate() {
        this.channels = new ManagedChannel[n_servers];
        this.async_stubs = new DadkvsServerSyncServiceGrpc.DadkvsServerSyncServiceStub[n_servers];
        String localhost = "localhost";
        for(int i = 0; i < n_servers; i++) {
            int port = this.server_state.base_port + i;
            this.channels[i] = ManagedChannelBuilder.forAddress(localhost, port).usePlaintext().build();
            this.async_stubs[i] = DadkvsServerSyncServiceGrpc.newStub(this.channels[i]);
        }
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
     * This method will automatically assign a sequence number to the
     * request and send it to all the servers.
     * @param reqid The request id
     */
    public void sendReqOrder(int reqid) {
        this.sequence_number++;

        // Create the sequenced request
        DadkvsServerSync.SequencedRequest.Builder sequencedRequestBuilder = DadkvsServerSync.SequencedRequest.newBuilder();
        sequencedRequestBuilder.setRequestid(reqid).setRequestseq(this.sequence_number);

        // Add the sequenced request to a list
        List<DadkvsServerSync.SequencedRequest> sequencedRequests = new ArrayList<DadkvsServerSync.SequencedRequest>();
        sequencedRequests.add(sequencedRequestBuilder.build());

        // Create the request order and add the list of sequenced requests
        DadkvsServerSync.RequestOrder.Builder requestOrderBuilder = DadkvsServerSync.RequestOrder.newBuilder();
        requestOrderBuilder.addAllOrderedrequests(sequencedRequests);

        DadkvsServerSync.RequestOrder requestOrder = requestOrderBuilder.build();

        // Create an empty list to collect the responses
        ArrayList<DadkvsServerSync.Empty> responseList = new ArrayList<DadkvsServerSync.Empty>();
        GenericResponseCollector<DadkvsServerSync.Empty> responseCollector = new GenericResponseCollector<DadkvsServerSync.Empty>(responseList, n_servers);

        // Broadcast the order to all the servers
        for (int i = 0; i < n_servers; i++) {
            DadkvsServerSyncServiceStub stub = this.async_stubs[i];
            StreamObserver<DadkvsServerSync.Empty> reqOrder_observer = new CollectorStreamObserver<>(responseCollector);
            stub.receiveReqOrder(requestOrder, reqOrder_observer);
        }

        // Wait for all the responses
        // TODO: this may need to be changed to a timeout or to a majority
        responseCollector.waitForTarget(n_servers);
        if (responseList.size() >= n_servers) {
            System.out.println("Request order sent to all servers");
        } else {
            System.out.println("Request order not sent to all servers");
        }
    }
}

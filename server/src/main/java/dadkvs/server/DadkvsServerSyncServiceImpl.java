package dadkvs.server;

import dadkvs.*;
import dadkvs.DadkvsServerSyncServiceGrpc.DadkvsServerSyncServiceStub;
import dadkvs.server.paxos.PaxosValue;
import dadkvs.server.requests.OrdedRequest;
import io.grpc.stub.StreamObserver;

import dadkvs.util.GenericResponseCollector;
import dadkvs.util.CollectorStreamObserver;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import dadkvs.server.paxos.SimplePaxosImpl;

/**
 * This class implements the gRPC service for the server-to-server
 * synchronization of request orders.
 */
public class DadkvsServerSyncServiceImpl extends DadkvsServerSyncServiceGrpc.DadkvsServerSyncServiceImplBase {


    DadkvsServerState server_state;

    /**
     * The sequence number of the request order.
     */
    int sequence_number;

    /**
     * The paxos round number
     */
    int paxos_round;

    SimplePaxosImpl paxos;
    /**
     * Broadcast control variables
     */
    private final int n_servers = 5;
    private ManagedChannel[] channels;
    private DadkvsPaxosServiceGrpc.DadkvsPaxosServiceStub[] paxos_server_sync_stub;

    public DadkvsServerSyncServiceImpl(DadkvsServerState state) {
        this.server_state = state;
        this.sequence_number = 0;
        this.paxos_round = 0;
        this.paxos = new SimplePaxosImpl(this.server_state);
        initiate();
    }

    /**
     * This method initializes the gRPC channels and stubs for
     * the server-to-server communication.
     */
    private void initiate() {
        this.channels = new ManagedChannel[n_servers];
        this.paxos_server_sync_stub = new DadkvsPaxosServiceGrpc.DadkvsPaxosServiceStub[n_servers];
        String localhost = "localhost";


        for (int i = 0; i < n_servers; i++) {
            int port = this.server_state.base_port + i;
            this.channels[i] = ManagedChannelBuilder.forAddress(localhost, port).usePlaintext().build();
            this.paxos_server_sync_stub[i] = DadkvsPaxosServiceGrpc.newStub(this.channels[i]); //Paxos stubs
        }
    }

    /**
     * This method receives the request order from the leader.
     *
     * @param request          The request order message
     * @param responseObserver The response observer
     */
    @Override
    public void receiveReqOrder(DadkvsServerSync.RequestOrder request, StreamObserver<DadkvsServerSync.Empty> responseObserver) {

        // Obtain the list of sequenced requests from the message
        List<DadkvsServerSync.SequencedRequest> sequencedRequests = request.getOrderedrequestsList();

        List<OrdedRequest> orderedRequests = new ArrayList<>();
        for (DadkvsServerSync.SequencedRequest sequencedRequest : sequencedRequests) {
            OrdedRequest orderedRequest = new OrdedRequest(sequencedRequest.getRequestseq(), sequencedRequest.getRequestid());
            orderedRequests.add(orderedRequest);
        }

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
     *
     * @param reqid The request id
     */
    public void sendReqOrder(int reqid) {
        this.sequence_number++;

        //------------------------Preparing for Performing Paxos-----------------------------//
        OrdedRequest ord_request = new OrdedRequest(this.sequence_number, reqid);
        PaxosValue value_proposed = new PaxosValue(ord_request);
        System.out.println("[Paxos] Starting Paxos");
        paxos.propose(value_proposed);
        System.out.println("[Paxos] Paxos Completed");

    }
}
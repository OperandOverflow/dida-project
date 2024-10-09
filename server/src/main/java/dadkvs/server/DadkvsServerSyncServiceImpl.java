package dadkvs.server;

import dadkvs.*;
import dadkvs.server.paxos.PaxosValue;
import dadkvs.server.requests.OrdedRequest;
import io.grpc.stub.StreamObserver;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.*;

import dadkvs.server.paxos.SimplePaxosImpl;

/**
 * This class implements the gRPC service for the server-to-server
 * synchronization of request orders.
 */
public class DadkvsServerSyncServiceImpl extends DadkvsServerSyncServiceGrpc.DadkvsServerSyncServiceImplBase {


    ServerState server_state;

    public DadkvsServerSyncServiceImpl(ServerState state) {
        this.server_state = state;
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
        this.server_state.serverSync.receiveReqOrder(orderedRequests);

        // Send empty response to the leader
        DadkvsServerSync.Empty response = DadkvsServerSync.Empty.getDefaultInstance();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
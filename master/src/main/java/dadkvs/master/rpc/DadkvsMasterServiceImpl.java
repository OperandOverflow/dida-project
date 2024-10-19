package dadkvs.master.rpc;

import dadkvs.DadkvsPaxos;
import dadkvs.DadkvsPaxosServiceGrpc;
import dadkvs.server.ServerState;
import dadkvs.util.CollectorStreamObserver;
import dadkvs.util.GenericResponseCollector;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class DadkvsMasterServiceImpl {

    private final ServerState server_state;
    public DadkvsPaxosServiceGrpc.DadkvsPaxosServiceStub[] paxos_stubs;



    public DadkvsMasterServiceImpl(ServerState server_state) {
        this.server_state = server_state;
        ManagedChannel[] channels = new ManagedChannel[server_state.n_servers];
        this.paxos_stubs = new DadkvsPaxosServiceGrpc.DadkvsPaxosServiceStub[server_state.n_servers];

        for(int i = 0; i < server_state.n_servers; i++) {
            int port = this.server_state.base_port + i;
            channels[i] = ManagedChannelBuilder.forAddress(server_state.ip_address, port).usePlaintext().build();
            this.paxos_stubs[i] = DadkvsPaxosServiceGrpc.newStub(channels[i]);
        }
    }



    //TODO: have not implemented message types yet - *CHANGE SIGNATURE*
    public void invokeNewBallot(int newBallot, int newConfig, int oldConfig) {
        DadkvsPaxos.NewBallotRequest nb_requests = DadkvsPaxos.NewBallotRequest.newBuilder()
                .setNewBallot(newBallot)
                .setNewConfig(newConfig)
                .setNewConfig(oldConfig)
                .build();
        ArrayList<DadkvsPaxos.NewBallotReply> nb_replies = new ArrayList<>();
        GenericResponseCollector<DadkvsPaxos.NewBallotReply> responseCollector = new GenericResponseCollector<>(nb_replies, server_state.n_servers);

        CountDownLatch latch = new CountDownLatch(1);

        for (int i = 0; i < server_state.n_servers; i++) {
            StreamObserver<DadkvsPaxos.NewBallotReply> newBallotObserver = new CollectorStreamObserver<>(responseCollector) {
                @Override
                public void onNext(DadkvsPaxos.NewBallotReply reply) {
                    super.onNext(reply); // Call the original onNext in the collector
                    //This is going to stop the process when one reply is received, since the only server that is going to respond is the leader
                    if (latch.getCount() > 0) { // Check if we have already received the first reply
                        latch.countDown(); // Signal that we have received the first reply
                    }
                }
            };
            this.paxos_stubs[i].newBallot(nb_requests, newBallotObserver);
        }
        // Wait until the latch is counted down (i.e., first reply is received)
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

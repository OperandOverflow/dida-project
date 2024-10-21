package dadkvs.master.rpc;

import dadkvs.*;
import dadkvs.util.CollectorStreamObserver;
import dadkvs.util.GenericResponseCollector;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;

public class MasterRPC {

    private final DadkvsMasterServiceGrpc.DadkvsMasterServiceStub[] master_stubs;
    private final DadkvsPaxosServiceGrpc.DadkvsPaxosServiceStub[] paxos_stubs;
    private final DadkvsConsoleServiceGrpc.DadkvsConsoleServiceStub[] console_stubs;

    private final int n_servers = 5;

    public MasterRPC() {
        ManagedChannel[] channels = new ManagedChannel[n_servers];
        this.master_stubs = new DadkvsMasterServiceGrpc.DadkvsMasterServiceStub[n_servers];
        this.paxos_stubs = new DadkvsPaxosServiceGrpc.DadkvsPaxosServiceStub[n_servers];
        this.console_stubs = new DadkvsConsoleServiceGrpc.DadkvsConsoleServiceStub[n_servers];

        for(int i = 0; i < n_servers; i++) {
            int port = 8080 + i;
            channels[i] = ManagedChannelBuilder.forAddress("localhost", port).usePlaintext().build();
            this.master_stubs[i] = DadkvsMasterServiceGrpc.newStub(channels[i]);
            this.paxos_stubs[i] = DadkvsPaxosServiceGrpc.newStub(channels[i]);
            this.console_stubs[i] = DadkvsConsoleServiceGrpc.newStub(channels[i]);
        }
    }

    /**
     * This method is used to tell a leader to start a new ballot with the configuration and ballot number.
     * @param ballotNum The ballot number to be used by the leader
     * @param newConfig The new configuration to be used in future operations
     * @param oldConfig The previous configuration
     * @param leaderId The id of the leader to be contacted
     * @return True if the leader acknowledges the request, false otherwise
     */
    public boolean invokeNewBallot(int ballotNum, int newConfig, int oldConfig, int leaderId) {
        DadkvsPaxos.NewBallotRequest nb_requests = DadkvsPaxos.NewBallotRequest.newBuilder()
                                                                                .setBallotnum(ballotNum)
                                                                                .setNewconfig(newConfig)
                                                                                .setOldconfig(oldConfig)
                                                                                .build();

        ArrayList<DadkvsPaxos.NewBallotReply> nb_replies = new ArrayList<>();
        GenericResponseCollector<DadkvsPaxos.NewBallotReply> responseCollector = new GenericResponseCollector<>(nb_replies, 1);

        StreamObserver<DadkvsPaxos.NewBallotReply> nbObserver = new CollectorStreamObserver<>(responseCollector);
        this.paxos_stubs[leaderId].newBallot(nb_requests, nbObserver);

        responseCollector.waitForTarget(1);

        if (nb_replies.isEmpty())
            return false;

        return nb_replies.getFirst().getAck();
    }

    /**
     * This method changes the leader status of a server.
     * @param isLeader True if the server is the new leader, false otherwise
     * @param leaderId The id of the server to be contacted
     * @return True if the server acknowledges the request, false otherwise
     */
    public boolean invokeSetLeader(boolean isLeader, int leaderId) {
        DadkvsConsole.SetLeaderRequest sl_request = DadkvsConsole.SetLeaderRequest.newBuilder()
                                                                                    .setIsleader(isLeader)
                                                                                    .build();

        ArrayList<DadkvsConsole.SetLeaderReply> sl_replies = new ArrayList<>();
        GenericResponseCollector<DadkvsConsole.SetLeaderReply> responseCollector = new GenericResponseCollector<>(sl_replies, 1);

        StreamObserver<DadkvsConsole.SetLeaderReply> slObserver = new CollectorStreamObserver<>(responseCollector);
        this.console_stubs[leaderId].setleader(sl_request, slObserver);

        responseCollector.waitForTarget(1);

        if (sl_replies.isEmpty())
            return false;

        return sl_replies.getFirst().getIsleaderack();
    }
}

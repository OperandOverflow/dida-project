package dadkvs.server.rpc;

import dadkvs.DadkvsPaxos;
import dadkvs.DadkvsPaxosServiceGrpc;
import dadkvs.server.ServerState;
import dadkvs.server.paxos.messages.AcceptedMsg;
import dadkvs.server.paxos.messages.LearnedMsg;
import dadkvs.server.paxos.messages.PromiseMsg;
import dadkvs.util.CollectorStreamObserver;
import dadkvs.util.GenericResponseCollector;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is dedicated to invoke gRPC methods for Paxos
 */
public class PaxosRPC {

    private final ServerState server_state;

    public DadkvsPaxosServiceGrpc.DadkvsPaxosServiceStub[] paxos_stubs;

    public PaxosRPC(ServerState state) {
        this.server_state = state;

        ManagedChannel[] channels = new ManagedChannel[server_state.n_servers];
        this.paxos_stubs = new DadkvsPaxosServiceGrpc.DadkvsPaxosServiceStub[server_state.n_servers];

        for(int i = 0; i < server_state.n_servers; i++) {
            int port = this.server_state.base_port + i;
            channels[i] = ManagedChannelBuilder.forAddress(server_state.ip_address, port).usePlaintext().build();
            this.paxos_stubs[i] = DadkvsPaxosServiceGrpc.newStub(channels[i]);
        }
    }

    public List<PromiseMsg> invokePrepare(int consensusNumber, int roundNumber, int config) {
        DadkvsPaxos.PhaseOneRequest phaseOneRequest = DadkvsPaxos.PhaseOneRequest.newBuilder()
                                                                .setPhase1Config(config)
                                                                .setPhase1Index(consensusNumber)
                                                                .setPhase1Timestamp(roundNumber)
                                                                .build();
        ArrayList<DadkvsPaxos.PhaseOneReply> phaseOneReplies = new ArrayList<>();
        GenericResponseCollector<DadkvsPaxos.PhaseOneReply> responseCollector = new GenericResponseCollector<>(phaseOneReplies, server_state.n_servers);
        for (int i = 0; i < server_state.n_servers; i++) {
            StreamObserver<DadkvsPaxos.PhaseOneReply> phaseOneObserver = new CollectorStreamObserver<>(responseCollector);
            this.paxos_stubs[i].phaseone(phaseOneRequest, phaseOneObserver);
        }
        responseCollector.waitForTarget(server_state.n_servers);
        List<PromiseMsg> promises = new ArrayList<>();
        for (DadkvsPaxos.PhaseOneReply reply : phaseOneReplies) {
            PromiseMsg promise = new PromiseMsg(
                                reply.getPhase1Timestamp(),
                                reply.getPhase1Index(),
                                reply.getPhase1Config(),
                                reply.getPhase1Accepted(),
                                reply.getPhase1Value());
            promises.add(promise);
        }
        return promises;
    }

    public List<AcceptedMsg> invokeAccept(int consensusNumber, int roundNumber, int config, int value) {
        DadkvsPaxos.PhaseTwoRequest phaseTwoRequest = DadkvsPaxos.PhaseTwoRequest.newBuilder()
                                                                .setPhase2Config(config)
                                                                .setPhase2Index(consensusNumber)
                                                                .setPhase2Timestamp(roundNumber)
                                                                .setPhase2Value(value)
                                                                .build();
        ArrayList<DadkvsPaxos.PhaseTwoReply> phaseTwoReplies = new ArrayList<>();
        GenericResponseCollector<DadkvsPaxos.PhaseTwoReply> responseCollector = new GenericResponseCollector<>(phaseTwoReplies, server_state.n_servers);
        for (int i = 0; i < server_state.n_servers; i++) {
            StreamObserver<DadkvsPaxos.PhaseTwoReply> phaseTwoObserver = new CollectorStreamObserver<>(responseCollector);
            this.paxos_stubs[i].phasetwo(phaseTwoRequest, phaseTwoObserver);
        }
        responseCollector.waitForTarget(server_state.n_servers);
        List<AcceptedMsg> accepted = new ArrayList<>();
        for (DadkvsPaxos.PhaseTwoReply reply : phaseTwoReplies) {
            AcceptedMsg accept = new AcceptedMsg(
                                reply.getPhase2Index(),
                                reply.getPhase2Config(),
                                reply.getPhase2Accepted());
            accepted.add(accept);
        }
        return accepted;
    }

    public List<LearnedMsg> invokeLearn(int consensusNumber, int roundNumber, int config, int value) {
        DadkvsPaxos.LearnRequest learnRequest = DadkvsPaxos.LearnRequest.newBuilder()
                                                .setLearnconfig(config)
                                                .setLearntimestamp(roundNumber)
                                                .setLearnindex(consensusNumber)
                                                .setLearnvalue(value)
                                                .build();

        ArrayList<DadkvsPaxos.LearnReply> learnReplies = new ArrayList<>();
        GenericResponseCollector<DadkvsPaxos.LearnReply> responseCollector = new GenericResponseCollector<>(learnReplies, server_state.n_servers);
        for (int i = 0; i < server_state.n_servers; i++) {
            StreamObserver<DadkvsPaxos.LearnReply> learnObserver = new CollectorStreamObserver<>(responseCollector);
            this.paxos_stubs[i].learn(learnRequest, learnObserver);
        }
        responseCollector.waitForTarget(server_state.n_servers);
        List<LearnedMsg> learned = new ArrayList<>();
        for (DadkvsPaxos.LearnReply reply : learnReplies) {
            LearnedMsg learn = new LearnedMsg(
                                    reply.getLearnindex(),
                                    reply.getLearnconfig(),
                                    reply.getLearnaccepted());
            learned.add(learn);
        }
        return learned;
    }
}

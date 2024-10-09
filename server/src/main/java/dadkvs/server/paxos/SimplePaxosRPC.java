package dadkvs.server.paxos;

import dadkvs.DadkvsPaxos;
import dadkvs.server.ServerState;
import dadkvs.server.paxos.messages.AcceptedMsg;
import dadkvs.server.paxos.messages.LearnedMsg;
import dadkvs.server.paxos.messages.PromiseMsg;
import dadkvs.server.requests.OrdedRequest;
import dadkvs.util.CollectorStreamObserver;
import dadkvs.util.GenericResponseCollector;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is dedicated to invoke gRPC methods for Paxos
 */
public class SimplePaxosRPC {

    private final ServerState server_state;

    public SimplePaxosRPC(ServerState state) {
        this.server_state = state;
    }

    public List<PromiseMsg> invokePrepare(int transactionNumber,int roundNumber, int leaderId, int config) {
        DadkvsPaxos.PhaseOneRequest phaseOneRequest = DadkvsPaxos.PhaseOneRequest.newBuilder()
                                                                .setPhase1Txid(transactionNumber)
                                                                .setPhase1Config(config)
                                                                .setPhase1Index(leaderId)
                                                                .setPhase1Timestamp(roundNumber)
                                                                .build();
        ArrayList<DadkvsPaxos.PhaseOneReply> phaseOneReplies = new ArrayList<>();
        GenericResponseCollector<DadkvsPaxos.PhaseOneReply> responseCollector = new GenericResponseCollector<>(phaseOneReplies, server_state.n_servers);
        for (int i = 0; i < server_state.n_servers; i++) {
            StreamObserver<DadkvsPaxos.PhaseOneReply> phaseOneObserver = new CollectorStreamObserver<>(responseCollector);
            server_state.rpc_stubs.paxos_stubs[i].phaseone(phaseOneRequest, phaseOneObserver);
        }
        responseCollector.waitForTarget(server_state.n_servers);
        List<PromiseMsg> promises = new ArrayList<>();
        for (DadkvsPaxos.PhaseOneReply reply : phaseOneReplies) {
            PromiseMsg promise = convertToPromiseMsg(reply);
            promises.add(promise);
        }
        return promises;
    }

    public List<AcceptedMsg> invokeAccept(int transactionNumber, int roundNumber, int leaderId, int config, PaxosValue value) {
        DadkvsPaxos.PhaseTwoRequest phaseTwoRequest = DadkvsPaxos.PhaseTwoRequest.newBuilder()
                                                                .setPhase2Txid(transactionNumber)
                                                                .setPhase2Config(config)
                                                                .setPhase2Index(leaderId) //paxos round
                                                                .setPhase2Timestamp(roundNumber) //leader
                                                                .setPhase2Value(DadkvsPaxos.PaxosValue.newBuilder()
                                                                        .setRequestid(value.getValue().getRequestId())
                                                                        .setRequestseq(value.getValue().getRequestSeq())
                                                                        .build())
                                                                .build();
        ArrayList<DadkvsPaxos.PhaseTwoReply> phaseTwoReplies = new ArrayList<>();
        GenericResponseCollector<DadkvsPaxos.PhaseTwoReply> responseCollector = new GenericResponseCollector<>(phaseTwoReplies, server_state.n_servers);
        for (int i = 0; i < server_state.n_servers; i++) {
            StreamObserver<DadkvsPaxos.PhaseTwoReply> phaseTwoObserver = new CollectorStreamObserver<>(responseCollector);
            server_state.rpc_stubs.paxos_stubs[i].phasetwo(phaseTwoRequest, phaseTwoObserver);
        }
        responseCollector.waitForTarget(server_state.n_servers);
        List<AcceptedMsg> accepted = new ArrayList<>();
        for (DadkvsPaxos.PhaseTwoReply reply : phaseTwoReplies) {
            AcceptedMsg accept = convertToAcceptedMsg(reply);
            accepted.add(accept);
        }
        return accepted;
    }

    public List<LearnedMsg> invokeLearn(int transactionNumber, int roundNumber, int leaderId, int config, PaxosValue value) {
        DadkvsPaxos.LearnRequest learnRequest = DadkvsPaxos.LearnRequest.newBuilder()
                                                .setLearntxid(transactionNumber)
                                                .setLearnconfig(config)
                                                .setLearnindex(leaderId) //paxos round
                                                .setLearntimestamp(roundNumber) //leader
                                                .setLearnvalue(DadkvsPaxos.PaxosValue.newBuilder()
                                                        .setRequestid(value.getValue().getRequestId())
                                                        .setRequestseq(value.getValue().getRequestSeq())
                                                        .build())
                                                .build();

        ArrayList<DadkvsPaxos.LearnReply> learnReplies = new ArrayList<>();
        GenericResponseCollector<DadkvsPaxos.LearnReply> responseCollector = new GenericResponseCollector<>(learnReplies, server_state.n_servers);
        for (int i = 0; i < server_state.n_servers; i++) {
            StreamObserver<DadkvsPaxos.LearnReply> learnObserver = new CollectorStreamObserver<>(responseCollector);
            server_state.rpc_stubs.paxos_stubs[i].learn(learnRequest, learnObserver);
        }
        responseCollector.waitForTarget(server_state.n_servers);
        List<LearnedMsg> learned = new ArrayList<>();
        for (DadkvsPaxos.LearnReply reply : learnReplies) {
            LearnedMsg learn = new LearnedMsg(reply.getLearnindex(), reply.getLearnconfig(), reply.getLearnaccepted());
            learned.add(learn);
        }
        return learned;
    }

    private AcceptedMsg convertToAcceptedMsg(DadkvsPaxos.PhaseTwoReply reply) {
        return new AcceptedMsg(reply.getPhase2Index(), reply.getPhase2Config(), reply.getPhase2Accepted());
    }

    private PromiseMsg convertToPromiseMsg(DadkvsPaxos.PhaseOneReply reply) {
        int reqSeq = reply.getPhase1Value().getRequestseq();
        int reqId = reply.getPhase1Value().getRequestid();

        if (reqId <= -1 || reqSeq <= -1) {
            return new PromiseMsg(reply.getPhase1Index(), reply.getPhase1Timestamp(),
                    reply.getPhase1Config(), reply.getPhase1Accepted(), null);
        } else {
            OrdedRequest request = new OrdedRequest(reply.getPhase1Value().getRequestseq(),
                                        reply.getPhase1Value().getRequestid());
            PaxosValue value = new PaxosValue(request);
            return new PromiseMsg(reply.getPhase1Index(), reply.getPhase1Timestamp(),
                    reply.getPhase1Config(), reply.getPhase1Accepted(), value);
        }
    }
}

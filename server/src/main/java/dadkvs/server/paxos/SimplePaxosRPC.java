package dadkvs.server.paxos;

import dadkvs.DadkvsPaxos;
import dadkvs.server.DadkvsServerState;
import dadkvs.server.paxos.messages.AcceptedMsg;
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

    private final DadkvsServerState server_state;

    public SimplePaxosRPC(DadkvsServerState state) {
        this.server_state = state;
    }

    public List<PromiseMsg> invokePrepare(int roundNumber, int leaderId, int config) {
        DadkvsPaxos.PhaseOneRequest phaseOneRequest = DadkvsPaxos.PhaseOneRequest.newBuilder()
                                                                .setPhase1Config(config)
                                                                .setPhase1Index(roundNumber) //paxos round
                                                                .setPhase1Timestamp(leaderId) //leader
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

    public List<AcceptedMsg> invokeAccept(int roundNumber, int leaderId, int config, PaxosValue value) {
        return null;
    }
}

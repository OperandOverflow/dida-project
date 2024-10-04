package dadkvs.server.paxos;

import dadkvs.server.DadkvsServerState;
import dadkvs.server.paxos.messages.*;
import dadkvs.server.requests.OrdedRequest;

import java.util.*;

public class SimplePaxosImpl implements Paxos{

    private final DadkvsServerState server_state;

    private final int MAJORITY;

    private final SimplePaxosRPC rpc;

    //============================================
    //             Leaders variables
    //============================================
    /** The round number of the current leader */
    private int roundNumber;

    //============================================
    //            Replica variables
    //============================================
    /** The highest round number seen so far (-1 if uninitialized) */
    private int highestSeenRoundNumber;

    /** The round number when this replica has accepted (NULL if uninitialized) */
    private int highestAcceptedRoundNumber;

    /** The value that this replica has accepted in previous rounds(-1 if uninitialized) */
    private PaxosValue acceptedValue;

    /** The number of learn messages received by the learner */
    private List<LearnMsg> learnerMsgCount;
    private boolean isMajorityReached;

    public SimplePaxosImpl(DadkvsServerState state) {
        this.server_state = state;
        this.MAJORITY = server_state.n_servers / 2 + 1;
        this.rpc = new SimplePaxosRPC(state);
        this.roundNumber = 0;
        this.highestSeenRoundNumber = -1;
        this.highestAcceptedRoundNumber = -1;
        this.acceptedValue = null;
        this.learnerMsgCount = new ArrayList<LearnMsg>();
        this.isMajorityReached = false;
    }


    @Override
    public synchronized boolean propose(PaxosValue value) {
        boolean isPrepared = false;
        while (!isPrepared) {
            // Loop while has no majority of promises
            this.roundNumber++;
            int roundID = this.roundNumber*10 + server_state.my_id;
            // TODO: adapt this to different configurations
            List<PromiseMsg> promises = rpc.invokePrepare(roundID, server_state.my_id, 0);
            int acceptedCount = 0;
            for (PromiseMsg promise : promises) {
                if (promise.accepted)
                    acceptedCount++;
            }
            if (acceptedCount >= MAJORITY) {
                isPrepared = true;
            }
        }

        // TODO: process the accepted values

        // TODO: invoke accept
        return false;
    }



    @Override
    public PromiseMsg prepare(PrepareMsg prepareMsg) {
        PromiseMsg promiseMsg = new PromiseMsg();
        promiseMsg.leaderId = prepareMsg.leaderId;
        promiseMsg.configNumber = prepareMsg.configNumber;

        if (prepareMsg.roundNumber > this.highestSeenRoundNumber) {
            this.highestSeenRoundNumber = prepareMsg.roundNumber;
            this.learnerMsgCount = new ArrayList<LearnMsg>();
            this.isMajorityReached = false;

            promiseMsg.accepted = true;
            promiseMsg.prevAcceptedRoundNumber = this.highestAcceptedRoundNumber;
            promiseMsg.prevAcceptedValue = this.acceptedValue;
            return promiseMsg;
        }
        promiseMsg.accepted = false;
        return promiseMsg;
    }

    @Override
    public AcceptedMsg accept(AcceptMsg acceptMsg) {
        AcceptedMsg acceptedMsg = new AcceptedMsg();
        acceptedMsg.leaderId = acceptMsg.leaderId;
        acceptedMsg.configNumber = acceptMsg.configNumber;

        if (acceptMsg.roundNumber >= this.highestSeenRoundNumber) {
            this.highestSeenRoundNumber = acceptMsg.roundNumber;
            this.highestAcceptedRoundNumber = acceptMsg.roundNumber;
            this.acceptedValue = acceptMsg.proposedValue;

            acceptedMsg.accepted = true;
            return acceptedMsg;
        }
        acceptedMsg.accepted = false;
        return acceptedMsg;
    }

    @Override
    public LearnedMsg learn(LearnMsg learnMsg) {
        LearnedMsg learnedMsg = new LearnedMsg();
        learnedMsg.leaderId = learnMsg.leaderId;
        learnedMsg.configNumber = learnMsg.configNumber;

        if (learnMsg.roundNumber >= this.highestSeenRoundNumber) {
            this.learnerMsgCount.add(learnMsg);
            if (this.learnerMsgCount.size() >= this.MAJORITY && !this.isMajorityReached) {
                this.isMajorityReached = true;

                OrdedRequest ordedRequest = new OrdedRequest(
                            learnMsg.learnedValue.getValue().getRequestSeq(),
                            learnMsg.learnedValue.getValue().getRequestId());
                this.server_state.ordered_request_processor.addReqOrder(ordedRequest);
            }

            learnedMsg.accepted = true;
            return learnedMsg;
        }
        learnedMsg.accepted = false;
        return learnedMsg;
    }
}

package dadkvs.server.paxos;

import dadkvs.server.DadkvsServerState;
import dadkvs.server.MestreAndre;
import dadkvs.server.paxos.messages.*;

import java.util.*;

public class SimplePaxosImpl implements Paxos{

    private final DadkvsServerState server_state;

    private final int MAJORITY;

    private final SimplePaxosRPC rpc;

    //============================================
    //             Leaders variables
    //============================================
    /** The number of the current transaction */
    private int transactionNumber;

    /** The round number of the current leader */
    private int roundNumber;

    //============================================
    //            Replica variables
    //============================================
    private Hashtable<Integer, PaxosTxData> paxosTxData;

    public SimplePaxosImpl(DadkvsServerState state) {
        this.server_state = state;
        this.MAJORITY = server_state.n_servers / 2 + 1;
        this.rpc = new SimplePaxosRPC(state);
        this.roundNumber = 0;
        this.transactionNumber = 0;
        this.paxosTxData = new Hashtable<>();
    }

    @Override
    public synchronized boolean propose(PaxosValue value) {
        //MestreAndre andre = new MestreAndre();
        //String blessing = andre.getBlessingsFromMestreAndre();
        //System.out.println(blessing);
        boolean isMostRecentTx = false;
        while (!isMostRecentTx) {
            this.transactionNumber++;
            isMostRecentTx = phaseOne(value);
        }
        return true;
    }

    private boolean phaseOne(PaxosValue value) {
        boolean isMostRecentRound = false;
        while (!isMostRecentRound) {
            this.roundNumber++;
            System.out.println("[Paxos] Starting phase 1");
            List<PromiseMsg> promises = rpc.invokePrepare(transactionNumber, roundNumber, server_state.my_id, 0);

            // Count the accepted promises
            int acceptedCount = 0;
            for (PromiseMsg promise : promises) {
                if (promise.accepted)
                    acceptedCount++;
            }
            PaxosValue mostRecentValue = selectMostRecentValue(promises);

            // If the majority accepted the prepare message
            if (acceptedCount >= MAJORITY) {
                System.out.println("[Paxos] Got majority, moving to phase two");
                // Move to phase two
                isMostRecentRound = phaseTwo(promises, value);
                System.out.println("[Paxos] Phase two finished");
                continue;
            }

            // If there is no majority of promises, check if the most recent value has majority
            // Count the amount of replicas have accepted that value
            int acceptedValueCount = 0;
            for (PromiseMsg promise : promises) {
                if (promise.prevAcceptedValue != null && promise.prevAcceptedValue.equals(mostRecentValue))
                    acceptedValueCount++;
            }
            // If the value has majority, i.e., tx is finished
            if (acceptedValueCount >= MAJORITY) {
                commitedValue(mostRecentValue);
                return false;
            }
            // The transaction is not finished, retry with higher round number
        }

        return true;
    }

    private boolean phaseTwo(List<PromiseMsg> promiseMsgList, PaxosValue value) {
        System.out.println("[Paxos] Phase two");
        PaxosValue mostRecentValue = selectMostRecentValue(promiseMsgList);

        List<AcceptedMsg> accepted = null;
        if (mostRecentValue == null)
            accepted = rpc.invokeAccept(transactionNumber, roundNumber, server_state.my_id, 0, value);
        else
            accepted = rpc.invokeAccept(transactionNumber, roundNumber, server_state.my_id, 0, mostRecentValue);

        int acceptedCount = 0;
        for (AcceptedMsg accept : accepted) {
            if (accept.accepted)
                acceptedCount++;
        }
        return acceptedCount >= MAJORITY;
    }

    private PaxosValue selectMostRecentValue(List<PromiseMsg> promises) {
        PaxosValue mostRecentValue = null;
        int highestRoundNumber = -1;
        for (PromiseMsg promise : promises) {
            if (promise.prevAcceptedRoundNumber > highestRoundNumber) {
                highestRoundNumber = promise.prevAcceptedRoundNumber;
                mostRecentValue = promise.prevAcceptedValue;
            }
        }
        return mostRecentValue;
    }


    @Override
    public synchronized PromiseMsg prepare(PrepareMsg prepareMsg) {
        PromiseMsg promiseMsg = new PromiseMsg();
        promiseMsg.leaderId = prepareMsg.roundNumber;
        promiseMsg.configNumber = prepareMsg.configNumber;

        PaxosTxData round = paxosTxData.get(prepareMsg.transactionNumber);
        // If the transaction doesn't exist
        if (round == null) {
            // Create a new round structure
            round = new PaxosTxData();
            paxosTxData.put(prepareMsg.roundNumber, round);

            round.highestSeenRoundNumber = prepareMsg.roundNumber;
            round.leaderId = prepareMsg.leaderId;

            promiseMsg.accepted = true;
            promiseMsg.prevAcceptedRoundNumber = -1;
            promiseMsg.prevAcceptedValue = null;
            return promiseMsg;
        }

        // *The transaction exists*
        promiseMsg.prevAcceptedValue = round.acceptedValue;
        promiseMsg.prevAcceptedRoundNumber = round.highestAcceptedRoundNumber;

        // If the round number is lower than
        if (prepareMsg.roundNumber <= round.highestSeenRoundNumber) {
            // Respond with the data of the existing round
            promiseMsg.accepted = false;
            return promiseMsg;
        }

        // Respond with the data of the existing round
        round.highestSeenRoundNumber = prepareMsg.roundNumber;
        promiseMsg.accepted = true;
        return promiseMsg;
    }

    @Override
    public synchronized AcceptedMsg accept(AcceptMsg acceptMsg) {
        AcceptedMsg acceptedMsg = new AcceptedMsg();
        acceptedMsg.leaderId = acceptMsg.leaderId;
        acceptedMsg.configNumber = acceptMsg.configNumber;

        PaxosTxData round = paxosTxData.get(acceptMsg.transactionNumber);
        // If the transaction doesn't exist
        if (round == null) {
            // Create a new round structure
            round = new PaxosTxData();
            paxosTxData.put(acceptMsg.roundNumber, round);

            round.highestSeenRoundNumber = acceptMsg.roundNumber;
            round.leaderId = acceptMsg.roundNumber;
            round.highestAcceptedRoundNumber = acceptMsg.roundNumber;
            round.acceptedValue = acceptMsg.proposedValue;

            acceptedMsg.accepted = true;
            rpc.invokeLearn(acceptMsg.transactionNumber ,acceptMsg.roundNumber,
                    acceptMsg.roundNumber, acceptMsg.configNumber, acceptMsg.proposedValue);
        }

        // *The transaction exists*
        // If the round number is lower than
        if (acceptMsg.roundNumber < round.highestSeenRoundNumber) {
            // Respond with the data of the existing round
            acceptedMsg.accepted = false;
            return acceptedMsg;
        }

        // Update the round data
        round.highestSeenRoundNumber = acceptMsg.roundNumber;
        round.acceptedValue = acceptMsg.proposedValue;
        round.highestAcceptedRoundNumber = acceptMsg.roundNumber;

        acceptedMsg.accepted = true;
        rpc.invokeLearn(acceptMsg.transactionNumber, acceptMsg.roundNumber, acceptMsg.roundNumber,
                    acceptMsg.configNumber, acceptMsg.proposedValue);

        return acceptedMsg;
    }

    @Override
    public synchronized LearnedMsg learn(LearnMsg learnMsg) {
        System.out.println("[Paxos] Learning");
        LearnedMsg learnedMsg = new LearnedMsg();
        learnedMsg.leaderId = learnMsg.roundNumber;
        learnedMsg.configNumber = learnMsg.configNumber;

        PaxosTxData round = paxosTxData.get(learnMsg.transactionNumber);
        // If the transaction doesn't exist
        if (round == null) {
            learnedMsg.accepted = false;
            return learnedMsg;
        }

        // If the round number is lower than
        if (learnMsg.roundNumber < round.highestSeenRoundNumber) {
            learnedMsg.accepted = false;
            return learnedMsg;
        }

        round.learnerMsgCount.add(learnMsg);
        if (round.learnerMsgCount.size() >= MAJORITY && !round.isMajorityReached) {
            round.isMajorityReached = true;

            System.out.println("[Paxos] Reached majority, delivering value");
            round.learnedValue = learnMsg.learnedValue;
            commitedValue(learnMsg.learnedValue);
        }
        learnedMsg.accepted = true;
        return learnedMsg;
    }

    /**
     * Process the commited value
     * @param value
     */
    private void commitedValue(PaxosValue value) {
        this.server_state.ordered_request_processor.addReqOrder(value.getValue());
    }
}

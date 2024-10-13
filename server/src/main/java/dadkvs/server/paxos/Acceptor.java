package dadkvs.server.paxos;

import dadkvs.server.ServerState;
import dadkvs.server.paxos.messages.*;

import java.util.Hashtable;

public class Acceptor {

    private final ServerState serverState;

    private final int MAJORITY;

    private final SimplePaxosRPC rpc;

    private final Hashtable<Integer, AcceptorData> acceptorRecord;

    public Acceptor(ServerState serverState) {
        this.serverState = serverState;
        this.MAJORITY = serverState.n_servers / 2 + 1;
        this.rpc = new SimplePaxosRPC(serverState);
        this.acceptorRecord = new Hashtable<>();
    }

    public synchronized PromiseMsg promise(PrepareMsg prepareMsg) {
        int consensusIndex = prepareMsg.consensusNumber;
        int roundNumber = prepareMsg.roundNumber;
        int config = prepareMsg.configNumber;

        PromiseMsg promiseMsg = new PromiseMsg();
        promiseMsg.consensusNumber = consensusIndex;
        promiseMsg.configNumber = config;

        // Search in the table for the data of this consensus
        AcceptorData acceptorData = this.acceptorRecord.get(consensusIndex);

        // If the consensus data is not found, i.e. it's a new consensus
        if (acceptorData == null) {
            // Create a new structure to hold the data
            acceptorData = new AcceptorData();
            acceptorData.highestPromisedRoundNumber = -1;
            acceptorData.acceptedRoundNumber = -1;
            acceptorData.acceptedValue = -1;
            this.acceptorRecord.put(consensusIndex, acceptorData);

            // Reply with a affirmative promise
            promiseMsg.accepted = true;
            promiseMsg.prevAcceptedValue = -1;
            promiseMsg.prevAcceptedRoundNumber = -1;
            return promiseMsg;
        }

        // If the consensus already exists
        // If the round number is lower than the previously seen
        if (roundNumber < acceptorData.highestPromisedRoundNumber) {
            // Reply with a negative promise
            promiseMsg.accepted = false;
            promiseMsg.prevAcceptedValue = -1;
            promiseMsg.prevAcceptedRoundNumber = -1;
            return promiseMsg;
        }

        // If the round number is higher than the previously seen
        // Update the highest promised round number
        acceptorData.highestPromisedRoundNumber = roundNumber;
        // Reply with a affirmative promise
        promiseMsg.accepted = true;
        promiseMsg.prevAcceptedValue = acceptorData.acceptedValue;
        promiseMsg.prevAcceptedRoundNumber = acceptorData.acceptedRoundNumber;
        return promiseMsg;
    }

    public synchronized AcceptedMsg accept(AcceptMsg acceptMsg) {
        int consensusIndex = acceptMsg.consensusNumber;
        int roundNumber = acceptMsg.roundNumber;
        int config = acceptMsg.configNumber;
        int value = acceptMsg.proposedValue;

        AcceptedMsg acceptedMsg = new AcceptedMsg();
        acceptedMsg.consensusNumber = consensusIndex;
        acceptedMsg.configNumber = config;

        // Search in the table for the data of this consensus
        AcceptorData acceptorData = this.acceptorRecord.get(consensusIndex);

        // If the consensus data is not found, i.e. didn't receive prepare for this consensus
        if (acceptorData == null) {
            // Reply with a negative accept
            acceptedMsg.accepted = false;
            return acceptedMsg;
        }

        // If the consensus already exists
        // If the round number is lower than the previously seen
        if (roundNumber < acceptorData.highestPromisedRoundNumber) {
            // Reply with a negative accept
            acceptedMsg.accepted = false;
            return acceptedMsg;
        }

        // If the round number is higher than the previously seen
        acceptorData.highestPromisedRoundNumber = roundNumber;
        // Update the accepted round number and value
        acceptorData.acceptedRoundNumber = roundNumber;
        acceptorData.acceptedValue = value;
        // Reply with a affirmative accept
        acceptedMsg.accepted = true;
        // Send learn message to all learners
        //rpc.invokeLearn(consensusIndex, roundNumber, value);
        return acceptedMsg;
    }


    /**
     * This class holds the data for an acceptor
     */
    private static class AcceptorData {

        public int highestPromisedRoundNumber;

        public int acceptedRoundNumber;

        public int acceptedValue;
    }
}

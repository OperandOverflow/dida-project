package dadkvs.server.paxos;

import dadkvs.server.ServerState;
import dadkvs.server.paxos.messages.*;

import java.util.Hashtable;

public class Acceptor {

    private final ServerState serverState;

    private final Hashtable<Integer, AcceptorData> acceptorRecord;

    public Acceptor(ServerState serverState) {
        this.serverState = serverState;
        this.acceptorRecord = new Hashtable<>();
    }

    public synchronized PromiseMsg prepare(PrepareMsg prepareMsg) {
        int consensusIndex = prepareMsg.consensusNumber;
        int roundNumber = prepareMsg.roundNumber;
        int config = prepareMsg.configNumber;

        if (serverState.consensusNumber.get() < consensusIndex) {
            serverState.consensusNumber.set(consensusIndex);
        }

        PromiseMsg promiseMsg = new PromiseMsg();
        promiseMsg.consensusNumber = consensusIndex;
        promiseMsg.configNumber = config;

        // Search in the table for the data of this consensus
        AcceptorData acceptorData = this.acceptorRecord.get(consensusIndex);

        // If the consensus data is not found, i.e. it's a new consensus
        if (acceptorData == null) {
            System.out.println("[Acce] Creating a new record for: " + consensusIndex);
            // Create a new structure to hold the data
            AcceptorData newAcceptorData = new AcceptorData();
            newAcceptorData.highestPromisedRoundNumber = roundNumber;
            this.acceptorRecord.put(consensusIndex, newAcceptorData);

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
            System.out.println("[Acce] Current round number lower than the highest seen");
            System.out.println("       Round number: " + roundNumber + " Seen number: " + acceptorData.highestPromisedRoundNumber);
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

        if (serverState.consensusNumber.get() < consensusIndex) {
            serverState.consensusNumber.set(consensusIndex);
        }

        AcceptedMsg acceptedMsg = new AcceptedMsg();
        acceptedMsg.consensusNumber = consensusIndex;
        acceptedMsg.configNumber = config;

        // Search in the table for the data of this consensus
        AcceptorData acceptorData = this.acceptorRecord.get(consensusIndex);

        // If the consensus data is not found, i.e. didn't receive prepare for this consensus
        if (acceptorData == null) {
            // Reply with a negative accept
            acceptedMsg.accepted = false;
            System.out.println("[Acc] No prepare received for consensus: " + consensusIndex);
            return acceptedMsg;
        }

        // If the consensus already exists
        // If the round number is lower than the previously seen
        if (roundNumber < acceptorData.highestPromisedRoundNumber) {
            // Reply with a negative accept
            acceptedMsg.accepted = false;
            System.out.println("[Acce] Current round number lower than the highest seen");
            System.out.println("       Round number: " + roundNumber + " Seen number: " + acceptorData.highestPromisedRoundNumber);
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
        // TODO: add config number
        serverState.paxos_rpc.invokeLearn(consensusIndex, roundNumber, 0, value);
        return acceptedMsg;
    }


    /**
     * This class holds the data for an acceptor
     */
    private static class AcceptorData {

        public int highestPromisedRoundNumber;

        public int acceptedRoundNumber;

        public int acceptedValue;

        public AcceptorData() {
            this.highestPromisedRoundNumber = -1;
            this.acceptedRoundNumber = -1;
            this.acceptedValue = -1;
        }
    }
}

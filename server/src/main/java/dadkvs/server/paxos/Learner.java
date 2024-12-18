package dadkvs.server.paxos;

import dadkvs.server.ServerState;
import dadkvs.server.paxos.messages.*;

import java.util.HashMap;
import java.util.Hashtable;

public class Learner {

    private final ServerState serverState;

    private final int MAJORITY;

    private final HashMap<Integer, LearnerData> learnerRecord;

    public Learner(ServerState serverState) {
        this.serverState = serverState;
        this.MAJORITY = serverState.n_servers / 2 + 1;
        this.learnerRecord = new HashMap<>();
    }

    public synchronized LearnedMsg learn(LearnMsg learnMsg) {
        int consensusIndex = learnMsg.consensusNumber;
        int roundNumber = learnMsg.roundNumber;
        int config = learnMsg.configNumber;
        int value = learnMsg.learnedValue;
        System.out.println("[Lear] Received learn for consensus: " + consensusIndex + " ballot: " + roundNumber);

        LearnedMsg learnedMsg = new LearnedMsg();
        learnedMsg.consensusNumber = consensusIndex;
        learnedMsg.configNumber = config;

        // Search in the table for the data of this consensus
        LearnerData learnerData = this.learnerRecord.get(consensusIndex);

        // If the consensus data is not found, i.e. it's a new consensus
        if (learnerData == null) {
            // Create a new structure to hold the data
            learnerData = new LearnerData();
            learnerData.highestReceivedRoundNumber = roundNumber;
            learnerData.receivedValues.put(value, 1);
            this.learnerRecord.put(consensusIndex, learnerData);

            // Reply with a affirmative learned
            learnedMsg.accepted = true;
            return learnedMsg;
        }

        // If the consensus already exists
        // If the round number is lower than the previously seen
        if (roundNumber < learnerData.highestReceivedRoundNumber) {
            // Reply with a negative learned
            learnedMsg.accepted = false;
            System.out.println("[Lear] Current round number lower than the highest seen");
            System.out.println("       Round number: " + roundNumber + " Seen number: " + learnerData.highestReceivedRoundNumber);
            return learnedMsg;
        }

        // If the round number is higher than the previously seen
        // Update the highest round number
        learnerData.highestReceivedRoundNumber = roundNumber;

        // Update the received values
        int counter = learnerData.receivedValues.getOrDefault(value, 0);
        learnerData.receivedValues.put(value, counter + 1);

        // If the majority of the acceptors have sent the same value for the first time
        if (counter + 1 >= this.MAJORITY && counter < this.MAJORITY) {
            // Deliver the value to the application
            serverState.request_handler.addReqToQueue(value);
            System.out.println("[Lear] Delivering value: " + value + " for consensus: " + consensusIndex + " with ballot: " + roundNumber);
        }
        // Reply with a affirmative learned
        learnedMsg.accepted = true;
        return learnedMsg;
    }


    private static class LearnerData {

        public int highestReceivedRoundNumber;

        // Value -> Count
        public Hashtable<Integer, Integer> receivedValues;

        public LearnerData() {
            this.receivedValues = new Hashtable<>();
        }
    }
}

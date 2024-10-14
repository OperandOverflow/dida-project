package dadkvs.server.paxos;

import dadkvs.server.ServerState;
import dadkvs.server.paxos.messages.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;

public class Proposer {

    private final ServerState serverState;

    private final int MAJORITY;

    private final Hashtable<Integer, ProposerData> proposerRecord;

    private int consensusNumber = 0;

    public Proposer(ServerState serverState) {
        this.serverState = serverState;
        this.MAJORITY = serverState.n_servers / 2 + 1;
        this.proposerRecord = new Hashtable<>();
    }

    //propose function
    public synchronized void propose(int client_value){
        // Increment the consensus number
        this.consensusNumber++;

        // Create a new structure to hold the data
        ProposerData proposerData = new ProposerData();
        proposerData.roundNumber = this.serverState.my_id;
        proposerData.proposedValue = client_value;
        this.proposerRecord.put(this.consensusNumber, proposerData);

        boolean finished = false;
        // Begin loop while not accepted
        while (!finished) {
            // Increment the round number
            proposerData.roundNumber += this.serverState.n_servers;

            // Send the Prepare message to all acceptors
            // TODO: add config number
            List<PromiseMsg> prepare_resp = serverState.paxos_rpc.invokePrepare(
                                                                    this.consensusNumber,
                                                                    proposerData.roundNumber,
                                                                    0);

            // Count the number of affirmative promises
            List<PromiseMsg> promises = prepare_resp.stream()
                                                    .filter(promise -> promise.accepted)
                                                    .toList();
            // If the number of promises is smaller than the majority
            if (promises.size() < MAJORITY) {
                // Retry with higher round number
                continue;
            }

            // If the number of promises is equal or greater than the majority
            // Find the accepted value with the highest round number among the promises
            int mostRecentValue = promises.stream()
                                          .max(Comparator.comparingInt(p -> p.prevAcceptedRoundNumber))
                                          .map(promise -> promise.prevAcceptedValue)
                                          .orElse(-1);

            // If there is an accepted value with a higher round number
            if (mostRecentValue != -1) {
                // Propose the most recent value
                proposerData.proposedValue = mostRecentValue;
            } else {
                // Propose the client value
                proposerData.proposedValue = client_value;
            }

            // Send the Accept message to all acceptors
            List<AcceptedMsg> accept_resp = serverState.paxos_rpc.invokeAccept(
                                                                    this.consensusNumber,
                                                                    proposerData.roundNumber,
                                                                    0,
                                                                    proposerData.proposedValue);

            // Count the number of affirmative accepts
            List<AcceptedMsg> accepts = accept_resp.stream()
                    .filter(accepted -> accepted.accepted)
                    .toList();
            // If the number of accepts is smaller than the majority
            if (accepts.size() < MAJORITY) {
                // Retry with higher round number
                continue;
            }

            // If the number of accepts is equal or greater than the majority
            // The value is accepted
            finished = true;
        }
    }

    private static class ProposerData {

        public int roundNumber;

        public int proposedValue;

        public List<PromiseMsg> promises;

        public List<AcceptedMsg> accepted;

        public ProposerData() {
            this.proposedValue = -1;
            this.roundNumber = -1;
            this.promises = new ArrayList<>();
            this.accepted = new ArrayList<>();
        }
    }
}

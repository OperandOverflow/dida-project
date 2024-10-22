package dadkvs.server.paxos;

import dadkvs.server.ServerState;
import dadkvs.server.paxos.messages.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Proposer {

    private final ServerState serverState;

    private final int MAJORITY;

    private final Hashtable<Integer, ProposerData> proposerRecord;

    private AtomicInteger ballotNumber;

    private int currentConfig;

    public Proposer(ServerState serverState) {
        this.serverState = serverState;
        this.MAJORITY = serverState.n_servers / 2 + 1;
        this.proposerRecord = new Hashtable<>();
        this.ballotNumber = new AtomicInteger(0);
        this.currentConfig = -1; //no config has been set
    }

    //propose function
    public synchronized void propose(int client_value){
        if(this.currentConfig == -1){
            System.out.println("[Prop] The system has no configuration currently - no value processed");
            return;
        }
        boolean valueCommited = false;
        // Creating new consensus while the client value is not committed
        while (!valueCommited && serverState.i_am_leader.get()) {

            // Increment the consensus number
            //this.consensusNumber++;
            int consensusNumber = this.serverState.consensusNumber.incrementAndGet();

            // Create a new structure to hold the data
            ProposerData proposerData = new ProposerData();
            proposerData.roundNumber = this.serverState.my_id;
            proposerData.proposedValue = client_value;
            this.proposerRecord.put(consensusNumber, proposerData);

            boolean finished = false;
            // Begin loop while not accepted and while I am the leader
            while (!finished && serverState.i_am_leader.get()) {
                // Increment the round number
                //*THE ROUND NUMBER IS OUR BALLOT NUMBER RIGHT????* IM SO CONFUSED WITH NAMES HOLY FUCK
                //cause in the new ballot function we use the invokePrepare with the ballot number but in here
                //we use the round number sooooo aaarggggh. Well. i did this.
                proposerData.roundNumber = this.ballotNumber.get();
                //proposerData.roundNumber += this.serverState.n_servers;

                // Send the Prepare message to all acceptors
                List<PromiseMsg> prepare_resp = serverState.paxos_rpc.invokePrepare(
                                                                        consensusNumber,
                                                                        proposerData.roundNumber,
                                                                        this.currentConfig);

                // Count the number of affirmative promises
                List<PromiseMsg> promises = prepare_resp.stream()
                                                        .filter(promise -> promise.accepted)
                                                        .filter(promise -> promise.consensusNumber == consensusNumber)
                                                        .toList();
                // If the number of promises is smaller than the majority
                if (promises.size() < MAJORITY) {
                    System.out.println("[Prop] Not enough promises: Received " + promises.size() + " promises, expected " + MAJORITY);
                    // Retry with higher round number
                    try{
                        wait();
                    }catch(InterruptedException e){
                        System.out.println("[Prop] Interrupted Exception - waiting for newBallot form Master");
                    }
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
                                                                        consensusNumber,
                                                                        proposerData.roundNumber,
                                                                        this.currentConfig,
                                                                        proposerData.proposedValue);

                // Count the number of affirmative accepts
                List<AcceptedMsg> accepts = accept_resp.stream()
                        .filter(accepted -> accepted.accepted)
                        .filter(accepted -> accepted.consensusNumber == consensusNumber)
                        .toList();
                // If the number of accepts is smaller than the majority
                if (accepts.size() < MAJORITY) {
                    System.out.println("[Prop] Not enough accepts: Received " + accepts.size() + " accepts, expected " + MAJORITY);
                    try{
                        wait();
                    }catch(InterruptedException e){
                        System.out.println("[Prop] Interrupted Exception - waiting for newBallot form Master");
                    }
                    continue;
                }

                // If the number of accepts is equal or greater than the majority
                // The value is accepted
                finished = true;
                valueCommited = proposerData.proposedValue == client_value;
            }
        }

    }

    public boolean newBallot(int ballotNumber, int newConfig, int prevConfig) {
        // Stop all incoming operations
        serverState.consoleConfig.setDebug(2);

        int consensusNumber = serverState.consensusNumber.get();

        // Perform the phase 1 of Paxos on the previous configuration to read the potential accepted value
        // Send the Prepare message to servers of the previous configuration
        List<PromiseMsg> prepare_resp = serverState.paxos_rpc.invokePrepare(
                                                                consensusNumber,
                                                                ballotNumber,
                                                                prevConfig);

        List<PromiseMsg> promises = prepare_resp.stream()
                                                .filter(promise -> promise.accepted)
                                                .filter(promise -> promise.consensusNumber == consensusNumber)
                                                .toList();

        if (promises.size() < MAJORITY) {
            System.out.println("[Prop] Not enough promises: Received " + promises.size() + " promises, expected " + MAJORITY);
            return false;
        }

        // Find the accepted value with the highest round number among the promises
        int mostRecentValue = promises.stream()
                                      .max(Comparator.comparingInt(p -> p.prevAcceptedRoundNumber))
                                      .map(promise -> promise.prevAcceptedValue)
                                      .orElse(-1);

        // If there is an accepted value
        if (mostRecentValue != -1) {
            // Propose the most recent value
            ProposerData proposerData = new ProposerData();
            proposerData.roundNumber = ballotNumber;
            proposerData.proposedValue = mostRecentValue;
            this.proposerRecord.put(consensusNumber, proposerData);
            List<AcceptedMsg> accept_resp = this.serverState.paxos_rpc.invokeAccept(
                                                                        consensusNumber,
                                                                        ballotNumber,
                                                                        newConfig,
                                                                        mostRecentValue);

            List<AcceptedMsg> accepts = accept_resp.stream()
                                                    .filter(accepted -> accepted.accepted)
                                                    .filter(accepted -> accepted.consensusNumber == consensusNumber)
                                                    .toList();

            if (accepts.size() < MAJORITY) {
                System.out.println("[Prop] Not enough accepts: Received " + accepts.size() + " accepts, expected " + MAJORITY);
                return false;
            }

        }

        boolean result = serverState.paxos_rpc.invokeComplete(ballotNumber);
        if (result)
            this.ballotNumber.set(ballotNumber);

        this.currentConfig = newConfig;
        // Tell the master that the ballot is completed
        return result;
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

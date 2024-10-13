package dadkvs.server.paxos;

import dadkvs.server.ServerState;
import dadkvs.server.paxos.messages.*;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import static java.lang.Math.ceil;

public class Proposer {

    private final ServerState serverState;

    private final int MAJORITY;

    private final SimplePaxosRPC rpc;

    private final Hashtable<Integer, ProposerData> proposerRecord;

    private int consensusNumber = 0;

    private int proposal_number = 0;

    public Proposer(ServerState serverState) {
        this.serverState = serverState;
        this.MAJORITY = serverState.n_servers / 2 + 1;
        this.rpc = new SimplePaxosRPC(serverState);
        this.proposerRecord = new Hashtable<>();
    }

    //propose function
    public synchronized PrepareMsg propose(int client_value){
        this.consensusNumber++;

        ProposerData proposerData = new ProposerData();

        this.proposal_number++;
        this.proposed_value = client_value;
        List<PromiseMsg> potential_promise = new ArrayList<>();
        potential_promise = rpc.invokePrepare();


        //after receiving the promises
        for(int i = 0; i < potential_promise.size(); i++){
            if(potential_promise.get(i).consensusNumber != proposal_number){
                potential_promise.remove(i); //To check the message is from the same consensus, cause messages can be delayed
            }
        }
        promises = potential_promise;
        if(promises.size() == ceil(MAJORITY)){
            //I want to check the max value of the promise
            int maxPrevAcceptedValue = getMaxPrevAcceptedValue(promises);
            if (maxPrevAcceptedValue != -1) {
                // Do something with maxPrevAcceptedValue, or propose it
                // as the value to be accepted.
                this.proposed_value = maxPrevAcceptedValue;
            } else {
                // No previous accepted value, propose client_value as the value
            }

        }
        rpc.invokeAccept();

    }

    // New helper function to find the max prevAcceptedValue
    private int getMaxPrevAcceptedValue(List<PromiseMsg> promises) {
        int maxValue = -1;
        int maxRoundNumber = -1;

        for (PromiseMsg promise : promises) {
            // Check if the promise had an accepted value
            if (promise.accepted && promise.prevAcceptedValue != -1) {
                // Compare based on prevAcceptedRoundNumber
                if (promise.prevAcceptedRoundNumber > maxRoundNumber) {
                    maxRoundNumber = promise.prevAcceptedRoundNumber;
                    maxValue = promise.prevAcceptedValue;
                }
            }
        }

        return maxValue;
    }


    //commit function
    public synchronized AcceptMsg accept(){

        AcceptMsg msg = new AcceptMsg(proposal_number, acceptor_acks, MAJORITY, proposed_value);

         // SEND THE ACCEPT MESSAGE TO ACCEPTORS
        List<AcceptedMsg> acks = rpc.invokeAccept();

        // hOW MANY ACCEPTORS ACCEPTED THE VALUE
        acceptor_acks = 0;
        for (AcceptedMsg ack : acks) {
            if (ack.accepted) {
                acceptor_acks++;
            }
        }

        if (acceptor_acks >= MAJORITY) {
            System.out.println("Proposal accepted by the majority!");
        } else {
            System.out.println("Proposal not accepted by the majority. Retry or choose another value.");
        }
    
        return msg;
    }

    private class ProposerData {

        public int proposedValue;

        public int roundNumber;

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

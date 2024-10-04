package dadkvs.server.paxos.messages;

import dadkvs.server.paxos.PaxosValue;

/**
 * This class is equivalent to the PhaseTwoRequest message in DadkvsPaxos.proto
 */
public class AcceptMsg {

    /**
     * phase2index
     */
    public int leaderId;

    /**
     * phase2timestamp
     */
    public int roundNumber;

    /**
     * phase2config
     */
    public int configNumber;

    /**
     * phase2value
     */
    public PaxosValue proposedValue;

    public int transactionNumber;

    public AcceptMsg(int roundNumber, int leaderId, int configNumber, PaxosValue proposedValue, int transactionNumber) {
        this.roundNumber = roundNumber;
        this.leaderId = leaderId;
        this.configNumber = configNumber;
        this.proposedValue = proposedValue;
        this.transactionNumber = transactionNumber;
    }

    public AcceptMsg() {
        this.leaderId = -1;
        this.roundNumber = -1;
        this.configNumber = -1;
        this.proposedValue = null;
        this.transactionNumber = -1;
    }
}

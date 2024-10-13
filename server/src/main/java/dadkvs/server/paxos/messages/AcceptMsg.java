package dadkvs.server.paxos.messages;

/**
 * This class is equivalent to the PhaseTwoRequest message in DadkvsPaxos.proto
 */
public class AcceptMsg {

    /**
     * phase2index
     */
    public int consensusNumber;

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
    public int proposedValue;

    public AcceptMsg(int roundNumber, int consensusNumber, int configNumber, int proposedValue) {
        this.roundNumber = roundNumber;
        this.consensusNumber = consensusNumber;
        this.configNumber = configNumber;
        this.proposedValue = proposedValue;
    }

    public AcceptMsg() {
        this.consensusNumber = -1;
        this.roundNumber = -1;
        this.configNumber = -1;
        this.proposedValue = -1;
    }
}

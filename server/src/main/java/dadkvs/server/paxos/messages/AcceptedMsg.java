package dadkvs.server.paxos.messages;

/**
 * This class is equivalent to the PhaseTwoReply message in DadkvsPaxos.proto
 */
public class AcceptedMsg {

    /**
     * phase2index
     */
    public int roundNumber;

    /**
     * phase2timestamp
     */
    public int leaderId;

    /**
     * phase2config
     */
    public int configNumber;

    /**
     * phase2accepted
     */
    public boolean accepted;

    public AcceptedMsg(int roundNumber, int leaderId, int configNumber, boolean accepted) {
        this.roundNumber = roundNumber;
        this.leaderId = leaderId;
        this.configNumber = configNumber;
        this.accepted = accepted;
    }

    public AcceptedMsg() {
        this.roundNumber = -1;
        this.leaderId = -1;
        this.configNumber = -1;
        this.accepted = false;
    }
}
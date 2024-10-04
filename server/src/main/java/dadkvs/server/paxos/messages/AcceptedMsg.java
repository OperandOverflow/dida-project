package dadkvs.server.paxos.messages;

/**
 * This class is equivalent to the PhaseTwoReply message in DadkvsPaxos.proto
 */
public class AcceptedMsg {

    /**
     * phase2index
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

    public AcceptedMsg(int leaderId, int configNumber, boolean accepted) {
        this.leaderId = leaderId;
        this.configNumber = configNumber;
        this.accepted = accepted;
    }

    public AcceptedMsg() {
        this.leaderId = -1;
        this.configNumber = -1;
        this.accepted = false;
    }
}
package dadkvs.server.paxos.messages;

/**
 * This class is equivalent to the PhaseTwoReply message in DadkvsPaxos.proto
 */
public class AcceptedMsg {

    /**
     * phase2index
     */
    public int consensusNumber;

    /**
     * phase2config
     */
    public int configNumber;

    /**
     * phase2accepted
     */
    public boolean accepted;

    public AcceptedMsg(int consensusNumber, int configNumber, boolean accepted) {
        this.consensusNumber = consensusNumber;
        this.configNumber = configNumber;
        this.accepted = accepted;
    }

    public AcceptedMsg() {
        this.consensusNumber = -1;
        this.configNumber = -1;
        this.accepted = false;
    }
}
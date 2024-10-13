package dadkvs.server.paxos.messages;

/**
 * This class is equivalent to the LearnReply message in DadkvsPaxos.proto
 */
public class LearnedMsg {

    /**
     * learnindex
     */
    public int consensusNumber;

    /**
     * learnconfig
     */
    public int configNumber;

    /**
     * learnaccepted
     */
    public boolean accepted;

    public LearnedMsg() {
        this.consensusNumber = -1;
        this.configNumber = -1;
        this.accepted = false;
    }

    public LearnedMsg(int consensusNumber, int configNumber, boolean accepted) {
        this.consensusNumber = consensusNumber;
        this.configNumber = configNumber;
        this.accepted = accepted;
    }
}

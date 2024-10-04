package dadkvs.server.paxos.messages;

/**
 * This class is equivalent to the LearnReply message in DadkvsPaxos.proto
 */
public class LearnedMsg {

    /**
     * learnindex
     */
    public int leaderId;

    /**
     * learnconfig
     */
    public int configNumber;

    /**
     * learnaccepted
     */
    public boolean accepted;

    public LearnedMsg() {
        this.leaderId = -1;
        this.configNumber = -1;
        this.accepted = false;
    }

    public LearnedMsg(int leaderId, int configNumber, boolean accepted) {
        this.leaderId = leaderId;
        this.configNumber = configNumber;
        this.accepted = accepted;
    }
}

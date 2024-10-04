package dadkvs.server.paxos.messages;

/**
 * This class is equivalent to the LearnReply message in DadkvsPaxos.proto
 */
public class LearnedMsg {

    /**
     * learnindex
     */
    public int roundNumber;

    /**
     * learntimestamp
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
        this.roundNumber = -1;
        this.leaderId = -1;
        this.configNumber = -1;
        this.accepted = false;
    }

    public LearnedMsg(int roundNumber, int leaderId, int configNumber, boolean accepted) {
        this.roundNumber = roundNumber;
        this.leaderId = leaderId;
        this.configNumber = configNumber;
        this.accepted = accepted;
    }
}

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
}

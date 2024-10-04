package dadkvs.server.paxos.messages;

import dadkvs.server.paxos.PaxosValue;

/**
 * This class is equivalent to the LearnRequest message in DadkvsPaxos.proto
 */
public class LearnMsg {

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
     * learnvalue
     */
    public PaxosValue learnedValue;
}

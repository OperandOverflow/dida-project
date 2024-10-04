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

    public LearnMsg() {
        this.roundNumber = -1;
        this.leaderId = -1;
        this.configNumber = -1;
        this.learnedValue = null;
    }

    public LearnMsg(int roundNumber, int leaderId, int configNumber, PaxosValue learnedValue) {
        this.roundNumber = roundNumber;
        this.leaderId = leaderId;
        this.configNumber = configNumber;
        this.learnedValue = learnedValue;
    }
}

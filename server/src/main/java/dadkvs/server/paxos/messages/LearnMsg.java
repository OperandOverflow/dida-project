package dadkvs.server.paxos.messages;

import dadkvs.server.paxos.PaxosValue;

/**
 * This class is equivalent to the LearnRequest message in DadkvsPaxos.proto
 */
public class LearnMsg {

    /**
     * learnindex
     */
    public int leaderId;

    /**
     * learntimestamp
     */
    public int roundNumber;

    /**
     * learnconfig
     */
    public int configNumber;

    /**
     * learnvalue
     */
    public PaxosValue learnedValue;

    public int transactionNumber;

    public LearnMsg() {
        this.leaderId = -1;
        this.roundNumber = -1;
        this.configNumber = -1;
        this.learnedValue = null;
        this.transactionNumber = -1;
    }

    public LearnMsg(int roundNumber, int leaderId, int configNumber, PaxosValue learnedValue, int transactionNumber) {
        this.leaderId = leaderId;
        this.roundNumber = roundNumber;
        this.configNumber = configNumber;
        this.learnedValue = learnedValue;
        this.transactionNumber = transactionNumber;
    }
}

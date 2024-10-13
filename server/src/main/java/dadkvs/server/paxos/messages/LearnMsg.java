package dadkvs.server.paxos.messages;

/**
 * This class is equivalent to the LearnRequest message in DadkvsPaxos.proto
 */
public class LearnMsg {

    /**
     * learnindex
     */
    public int consensusNumber;

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
    public int learnedValue;

    public LearnMsg() {
        this.consensusNumber = -1;
        this.roundNumber = -1;
        this.configNumber = -1;
        this.learnedValue = -1;
    }

    public LearnMsg(int roundNumber, int consensusNumber, int configNumber, int learnedValue) {
        this.consensusNumber = consensusNumber;
        this.roundNumber = roundNumber;
        this.configNumber = configNumber;
        this.learnedValue = learnedValue;
    }
}

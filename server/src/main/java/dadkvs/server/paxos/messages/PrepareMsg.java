package dadkvs.server.paxos.messages;

/**
 * This class is equivalent to the PhaseOneRequest message in DadkvsPaxos.proto
 */
public class PrepareMsg {

    /** phase1index */
    public int roundNumber;

    /** phas1ConsensusNumber aka leaderId */
    public int consensusNumber;
    /** phase1config */
    public int configNumber;

    public PrepareMsg() {
        this.consensusNumber = -1;
        this.roundNumber = -1;
        this.configNumber = -1;
    }

    public PrepareMsg(int consensusNumber, int roundNumber, int configNumber) {
        this.consensusNumber = consensusNumber;
        this.roundNumber = roundNumber;
        this.configNumber = configNumber;
    }
}

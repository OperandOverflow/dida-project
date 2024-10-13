package dadkvs.server.paxos.messages;

/**
 * This class is equivalent to the PhaseOneRequest message in DadkvsPaxos.proto
 */
public class PrepareMsg {

    /** phase1index */
    public int consensusNumber;

    /** phase1timestamp */
    public int roundNumber;

    /** phase1config */
    public int configNumber;

    public PrepareMsg() {
        this.consensusNumber = -1;
        this.roundNumber = -1;
        this.configNumber = -1;
    }

    public PrepareMsg(int roundNumber, int leaderId, int configNumber) {
        this.consensusNumber = roundNumber;
        this.roundNumber = leaderId;
        this.configNumber = configNumber;
    }
}

package dadkvs.server.paxos.messages;

/**
 * This class is equivalent to the PhaseOneRequest message in DadkvsPaxos.proto
 */
public class PrepareMsg {

    /** phase1index */
    public int roundNumber;

    /** phase1timestamp */
    public int leaderId;

    /** phase1config */
    public int configNumber;

    public PrepareMsg() {
        this.roundNumber = -1;
        this.leaderId = -1;
        this.configNumber = -1;
    }

    public PrepareMsg(int roundNumber, int leaderId, int configNumber) {
        this.roundNumber = roundNumber;
        this.leaderId = leaderId;
        this.configNumber = configNumber;
    }
}
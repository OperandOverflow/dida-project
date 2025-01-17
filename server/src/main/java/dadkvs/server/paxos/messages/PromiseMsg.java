package dadkvs.server.paxos.messages;

/**
 * This class is equivalent to the PhaseOneReply message in DadkvsPaxos.proto
 */
public class PromiseMsg {

    /** phase1index */
    public int consensusNumber;

    /** phase1config */
    public int configNumber;

    /** phase1accepted */
    public boolean accepted;

    /** phase1timestamp */
    public int prevAcceptedRoundNumber;

    /** phase1value */
    public int prevAcceptedValue;

    public PromiseMsg(int prevRoundNumber, int consensusNumber, int configNumber, boolean accepted, int prevAcceptedValue) {
        this.prevAcceptedRoundNumber = prevRoundNumber;
        this.consensusNumber = consensusNumber;
        this.configNumber = configNumber;
        this.accepted = accepted;
        this.prevAcceptedValue = prevAcceptedValue;
    }

    public PromiseMsg() {
        this.prevAcceptedRoundNumber = -1;
        this.consensusNumber = -1;
        this.configNumber = -1;
        this.accepted = false;
        this.prevAcceptedValue = -1;
    }
}

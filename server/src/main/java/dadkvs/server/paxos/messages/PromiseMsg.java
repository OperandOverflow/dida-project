package dadkvs.server.paxos.messages;

import dadkvs.server.paxos.PaxosValue;

/**
 * This class is equivalent to the PhaseOneReply message in DadkvsPaxos.proto
 */
public class PromiseMsg {

    /** phase1timestamp */
    public int leaderId;

    /** phase1config */
    public int configNumber;

    /** phase1accepted */
    public boolean accepted;

    /** phase1index */
    public int prevAcceptedRoundNumber;

    /** phase1value */
    public PaxosValue prevAcceptedValue;

    public PromiseMsg(int prevRoundNumber, int leaderId, int configNumber, boolean accepted, PaxosValue prevAcceptedValue) {
        this.prevAcceptedRoundNumber = prevRoundNumber;
        this.leaderId = leaderId;
        this.configNumber = configNumber;
        this.accepted = accepted;
        this.prevAcceptedValue = prevAcceptedValue;
    }

    public PromiseMsg() {
        this.prevAcceptedRoundNumber = -1;
        this.leaderId = -1;
        this.configNumber = -1;
        this.accepted = false;
        this.prevAcceptedValue = null;
    }
}

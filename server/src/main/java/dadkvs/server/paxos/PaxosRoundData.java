package dadkvs.server.paxos;

import dadkvs.server.paxos.messages.LearnMsg;

import java.util.List;

/**
 * This class holds the data for a paxos round
 */
public class PaxosRoundData {

    /** The highest round number seen so far (-1 if uninitialized) */
    public int roundNumber;

    /** The leader id of the current round (-1 if uninitialized) */
    public int leaderId;

    /** The round number when this replica has accepted (NULL if uninitialized) */
    public int highestAcceptedRoundNumber;

    /** The value that this replica has accepted in previous rounds(-1 if uninitialized) */
    public PaxosValue acceptedValue;

    /** The number of learn messages received by the learner */
    public List<LearnMsg> learnerMsgCount;

    /** Flag to indicate received LearnMsg from the majority */
    public boolean isMajorityReached;

    /** Final commited value in this round */
    public PaxosValue learnedValue;


    public PaxosRoundData() {
        this.roundNumber = -1;
        this.leaderId = -1;
        this.highestAcceptedRoundNumber = -1;
        this.acceptedValue = null;
        this.learnerMsgCount = null;
        this.isMajorityReached = false;
        this.learnedValue = null;
    }
}

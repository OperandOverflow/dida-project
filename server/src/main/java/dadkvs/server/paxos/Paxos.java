package dadkvs.server.paxos;

import dadkvs.server.paxos.messages.*;

public interface Paxos {

    //================================================================
    //                        Leader methods
    //================================================================

    /**
     * Propose a value to the other replicas
     * This method is used by the leader.
     * @param value
     * @return
     */
    public boolean propose(PaxosValue value);



    //================================================================
    //                        Replica methods
    //================================================================

    /**
     * Replica receives a prepare message from the leader
     * to try to start a new round of Paxos.
     * @param prepareMsg The prepare message from the leader
     * @return The promise message to send back to the leader
     */
    public PromiseMsg prepare(PrepareMsg prepareMsg);

    /**
     * Replica receives an accept message from the leader
     * to try to accept a value in the current round of Paxos.
     * @param acceptMsg The accept message from the leader
     * @return The accepted message to send back to the leader
     */
    public AcceptedMsg accept(AcceptMsg acceptMsg);

    /**
     * Replica receives a learn message from the accepter
     * to learn a value that has been accepted.
     * @param learnMsg The learn message from the accepter
     * @return The learned message to send back to the accepter
     */
    public LearnedMsg learn(LearnMsg learnMsg);
}

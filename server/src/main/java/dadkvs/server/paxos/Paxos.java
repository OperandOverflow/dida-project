package dadkvs.server.paxos;

import dadkvs.server.paxos.messages.*;

public interface Paxos {

    //================================================================
    //                        Leader methods
    //================================================================

    /**
     * Propose a value to the other replicas
     * This method is used by the leader.
     * @param value The value to propose
     * @return True if the value has been proposed, false otherwise
     */
    boolean propose(PaxosValue value);

    /**
     * Stop the paxos algorithm, aborting the current round if any
     * and preventing starting any new round.
     * @param stop True to stop the paxos algorithm, false otherwise
     */
    void setStop(boolean stop);



    //================================================================
    //                        Replica methods
    //================================================================

    /**
     * Replica receives a prepare message from the leader
     * to try to start a new round of Paxos.
     * @param prepareMsg The prepare message from the leader
     * @return The promise message to send back to the leader
     */
    PromiseMsg prepare(PrepareMsg prepareMsg);

    /**
     * Replica receives an accept message from the leader
     * to try to accept a value in the current round of Paxos.
     * @param acceptMsg The accept message from the leader
     * @return The accepted message to send back to the leader
     */
    AcceptedMsg accept(AcceptMsg acceptMsg);

    /**
     * Replica receives a learn message from the accepter
     * to learn a value that has been accepted.
     * @param learnMsg The learn message from the accepter
     * @return The learned message to send back to the accepter
     */
    LearnedMsg learn(LearnMsg learnMsg);
}

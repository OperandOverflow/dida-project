package dadkvs.server.paxos;

import dadkvs.server.ServerState;
import dadkvs.server.paxos.messages.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SimplePaxosImpl implements Paxos{

    private final ServerState server_state;

    private final int MAJORITY;

    private final SimplePaxosRPC rpc;

    //============================================
    //             Leaders variables
    //============================================
    /** The round number of the current leader */
    private int roundNumber;

    private final AtomicBoolean stopped;

    //============================================
    //            Replica variables
    //============================================
    /** The data of all transactions */
    private final Hashtable<Integer, PaxosTxData> paxosTxData;
    private final ReadWriteLock paxosTxDataLock;


    // Locks for functions
    private final Lock prepare_lock;
    private final Lock accept_lock;
    private final Lock learn_lock;

    public SimplePaxosImpl(ServerState state) {
        this.server_state = state;
        this.MAJORITY = server_state.n_servers / 2 + 1;
        this.rpc = new SimplePaxosRPC(state);

        this.roundNumber = 0;
        this.stopped = new AtomicBoolean(false);

        this.paxosTxData = new Hashtable<>();
        this.paxosTxDataLock = new ReentrantReadWriteLock();
        this.prepare_lock = new ReentrantLock();
        this.accept_lock = new ReentrantLock();
        this.learn_lock = new ReentrantLock();
    }

    @Override
    public boolean propose(PaxosValue value) {
        if (stopped.get())
            return false;
        phaseOne(value);
        return true;
    }

    @Override
    public void setStop(boolean stop) {
        this.stopped.set(stop);
    }

    private boolean phaseOne(PaxosValue value) {
        boolean isMostRecentRound = false;
        while (!isMostRecentRound && !stopped.get()) {
            this.roundNumber++;
            System.out.println("[Paxos] Starting phase 1");
            // The value Id is the id for the transaction
            List<PromiseMsg> promises = rpc.invokePrepare(value.getValueId(), roundNumber, server_state.my_id, 0);

            System.out.println("[Paxos] Counting promises");
            // Count the accepted promises
            int acceptedCount = 0;
            for (PromiseMsg promise : promises) {
                if (promise.accepted)
                    acceptedCount++;
            }
            System.out.println("[Paxos] Selecting most recent value");
            PaxosValue mostRecentValue = selectMostRecentValue(promises);

            // If the majority accepted the prepare message
            if (acceptedCount >= MAJORITY) {
                System.out.println("[Paxos] Got majority, moving to phase two");
                // Move to phase two
                isMostRecentRound = phaseTwo(promises, value);
                System.out.println("[Paxos] Phase two finished");
                continue;
            }

            System.out.println("[Paxos] Didn't get majority, checking if most recent value has majority");
            // If there is no majority of promises, check if the most recent value has majority
            // Count the amount of replicas have accepted that value
            int acceptedValueCount = 0;
            for (PromiseMsg promise : promises) {
                if (promise.prevAcceptedValue != null && promise.prevAcceptedValue.equals(mostRecentValue))
                    acceptedValueCount++;
            }
            // If the value has majority, i.e., tx is finished
            if (acceptedValueCount >= MAJORITY) {
                commitedValue(mostRecentValue);
                return true;
            }
            // The transaction is not finished, retry with higher round number
            System.out.println("[Paxos] Retrying with higher round number");
        }

        return true;
    }

    private boolean phaseTwo(List<PromiseMsg> promiseMsgList, PaxosValue value) {
        System.out.println("[Paxos] Phase two");
        PaxosValue mostRecentValue = selectMostRecentValue(promiseMsgList);

        List<AcceptedMsg> accepted;
        accepted = rpc.invokeAccept(
                value.getValueId(),
                roundNumber,
                server_state.my_id,
                0,
                Objects.requireNonNullElse(mostRecentValue, value)
        );

        int acceptedCount = 0;
        for (AcceptedMsg accept : accepted) {
            if (accept.accepted)
                acceptedCount++;
        }
        return acceptedCount >= MAJORITY;
    }

    private PaxosValue selectMostRecentValue(List<PromiseMsg> promises) {
        PaxosValue mostRecentValue = null;
        int highestRoundNumber = -1;
        for (PromiseMsg promise : promises) {
            if (promise.prevAcceptedRoundNumber > highestRoundNumber) {
                highestRoundNumber = promise.prevAcceptedRoundNumber;
                mostRecentValue = promise.prevAcceptedValue;
            }
        }
        return mostRecentValue;
    }


    @Override
    public synchronized PromiseMsg prepare(PrepareMsg prepareMsg) {
        this.prepare_lock.lock();
        System.out.println("[Paxos] Prepare for " + prepareMsg.transactionNumber);
        try {
            PromiseMsg promiseMsg = new PromiseMsg();
            promiseMsg.leaderId = prepareMsg.roundNumber;
            promiseMsg.configNumber = prepareMsg.configNumber;

            PaxosTxData round;
            this.paxosTxDataLock.readLock().lock();
            try {
                round = paxosTxData.get(prepareMsg.transactionNumber);
            } finally {
                this.paxosTxDataLock.readLock().unlock();
            }
            // If the transaction doesn't exist
            if (round == null) {
                // Create a new round structure
                round = new PaxosTxData();

                round.highestSeenRoundNumber = prepareMsg.roundNumber;
                round.leaderId = prepareMsg.leaderId;

                this.paxosTxDataLock.writeLock().lock();
                try {
                    paxosTxData.put(prepareMsg.transactionNumber, round);
                } finally {
                    this.paxosTxDataLock.writeLock().unlock();
                }

                promiseMsg.accepted = true;
                promiseMsg.prevAcceptedRoundNumber = -1;
                promiseMsg.prevAcceptedValue = null;
                System.out.println("[Paxos] Prepare " + prepareMsg.transactionNumber + " accepted");
                return promiseMsg;
            }

            // *The transaction exists*
            round.beginRead();
            promiseMsg.prevAcceptedValue = round.acceptedValue;
            promiseMsg.prevAcceptedRoundNumber = round.highestAcceptedRoundNumber;

            // If the round number is lower than
            if (prepareMsg.roundNumber <= round.highestSeenRoundNumber) {
                round.endRead();
                // Respond with the data of the existing round
                promiseMsg.accepted = false;
                System.out.println("[Paxos] Prepare " + prepareMsg.transactionNumber + " NOT accepted: round number lower");
                System.out.println("                Existing round number: " + round.highestSeenRoundNumber);
                System.out.println("                Received round number: " + prepareMsg.roundNumber);
                return promiseMsg;
            }
            round.endRead();

            // Respond with the data of the existing round
            round.beginWrite();
            round.highestSeenRoundNumber = prepareMsg.roundNumber;
            round.endWrite();
            promiseMsg.accepted = true;
            System.out.println("[Paxos] Prepare " + prepareMsg.transactionNumber + " accepted");
            return promiseMsg;
        } finally {
            this.prepare_lock.unlock();
        }
    }

    @Override
    public AcceptedMsg accept(AcceptMsg acceptMsg) {
        this.accept_lock.lock();
        System.out.println("[Paxos] Accept for " + acceptMsg.transactionNumber);
        try {
            AcceptedMsg acceptedMsg = new AcceptedMsg();
            acceptedMsg.leaderId = acceptMsg.leaderId;
            acceptedMsg.configNumber = acceptMsg.configNumber;

            PaxosTxData round;
            this.paxosTxDataLock.readLock().lock();
            try {
                round = paxosTxData.get(acceptMsg.transactionNumber);
            } finally {
                this.paxosTxDataLock.readLock().unlock();
            }
            // If the transaction doesn't exist
            if (round == null) {
                // Create a new round structure
                round = new PaxosTxData();

                round.highestSeenRoundNumber = acceptMsg.roundNumber;
                round.leaderId = acceptMsg.roundNumber;
                round.highestAcceptedRoundNumber = acceptMsg.roundNumber;
                round.acceptedValue = acceptMsg.proposedValue;

                this.paxosTxDataLock.writeLock().lock();
                try {
                    paxosTxData.put(acceptMsg.transactionNumber, round);
                } finally {
                    this.paxosTxDataLock.writeLock().unlock();
                }

                acceptedMsg.accepted = true;
                rpc.invokeLearn(acceptMsg.transactionNumber ,acceptMsg.roundNumber,
                        acceptMsg.roundNumber, acceptMsg.configNumber, acceptMsg.proposedValue);
                System.out.println("[Paxos] Accept " + acceptMsg.transactionNumber + " accepted");
                return acceptedMsg;
            }

            // *The transaction exists*
            // If the round number is lower than
            round.beginRead();
            if (acceptMsg.roundNumber < round.highestSeenRoundNumber) {
                round.endRead();
                // Respond with the data of the existing round
                acceptedMsg.accepted = false;
                System.out.println("[Paxos] Accept " + acceptMsg.transactionNumber + " NOT accepted: round number lower");
                System.out.println("                Existing round number: " + round.highestSeenRoundNumber);
                System.out.println("                Received round number: " + acceptMsg.roundNumber);
                return acceptedMsg;
            }
            round.endRead();

            // Update the round data
            round.beginWrite();
            round.highestSeenRoundNumber = acceptMsg.roundNumber;
            round.acceptedValue = acceptMsg.proposedValue;
            round.highestAcceptedRoundNumber = acceptMsg.roundNumber;
            round.endWrite();

            acceptedMsg.accepted = true;
            rpc.invokeLearn(acceptMsg.transactionNumber, acceptMsg.roundNumber, acceptMsg.roundNumber,
                        acceptMsg.configNumber, acceptMsg.proposedValue);
            System.out.println("[Paxos] Accept " + acceptMsg.transactionNumber + " accepted");
            return acceptedMsg;
        } finally {
            this.accept_lock.unlock();
        }
    }

    @Override
    public LearnedMsg learn(LearnMsg learnMsg) {
        this.learn_lock.lock();
        System.out.println("[Paxos] Learn for " + learnMsg.transactionNumber);
        try {
            LearnedMsg learnedMsg = new LearnedMsg();
            learnedMsg.leaderId = learnMsg.roundNumber;
            learnedMsg.configNumber = learnMsg.configNumber;

            PaxosTxData round;
            this.paxosTxDataLock.readLock().lock();
            try {
                round = paxosTxData.get(learnMsg.transactionNumber);
            } finally {
                this.paxosTxDataLock.readLock().unlock();
            }
            // If the transaction doesn't exist
            if (round == null) {
                learnedMsg.accepted = false;
                System.out.println("[Paxos] Learn " + learnMsg.transactionNumber + " NOT accepted: transaction doesn't exist");
                return learnedMsg;
            }

            // If the round number is lower than
            if (learnMsg.roundNumber < round.highestSeenRoundNumber) {
                learnedMsg.accepted = false;
                System.out.println("[Paxos] Learn " + learnMsg.transactionNumber + " NOT accepted: round number lower");
                System.out.println("                Existing round number: " + round.highestSeenRoundNumber);
                System.out.println("                Received round number: " + learnMsg.roundNumber);
                return learnedMsg;
            }

            round.beginWrite();
            round.learnerMsgCount.add(learnMsg);
            if (round.learnerMsgCount.size() >= MAJORITY && !round.isMajorityReached) {
                round.isMajorityReached = true;

                System.out.println("[Paxos] Learn reached majority, delivering value");
                round.learnedValue = learnMsg.learnedValue;
                commitedValue(learnMsg.learnedValue);
            }
            round.endWrite();
            System.out.println("[Paxos] Learn " + learnMsg.transactionNumber + " accepted");
            learnedMsg.accepted = true;
            return learnedMsg;
        } finally {
            this.learn_lock.unlock();
        }
    }

    /**
     * Process the commited value
     * @param value The value to be commited
     */
    private void commitedValue(PaxosValue value) {
        this.server_state.request_handler.addOrderedRequest(value.getValue());
    }
}
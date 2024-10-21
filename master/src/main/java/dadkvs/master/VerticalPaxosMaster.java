package dadkvs.master;

import dadkvs.master.rpc.MasterRPC;

import java.util.LinkedHashSet;

public class VerticalPaxosMaster {

    private MasterRPC rpc;

    /*
    * The master creates b => a new number added to the subset of ballot numbers (largestCompleteBallotNumber + 1)
    * this means that the new configuration is *ACTIVATED* and that proposers can start writing to it.
    */
    private int largestCompleteBallotNumber; //current ballot number
    /** Ballot number waiting for COMPLETED message*/
    private int pendingBallotNumber;
    private int nextBallotNumber;

    private int prevConfig;
    private int currentConfig;

    private final LinkedHashSet<Integer> activeLeaders;
    private int currentLeader;

    public VerticalPaxosMaster() {
        this.rpc = new MasterRPC();

        this.largestCompleteBallotNumber = 0;
        this.pendingBallotNumber = -1;
        this.nextBallotNumber = largestCompleteBallotNumber + 1;

        this.prevConfig = 0;
        this.currentConfig = 0;

        // TODO: ask teacher about whether it's necessary to save the list of active leaders
        this.activeLeaders = new LinkedHashSet<>();
        this.currentLeader = -1;
    }

    public boolean setLeader(boolean isLeader, int leaderId){
        if (isLeader) {
            activeLeaders.add(leaderId);
            currentLeader = leaderId;
        } else {
            activeLeaders.remove(leaderId);
            currentLeader = activeLeaders.getLast() != null ? activeLeaders.getLast() : -1;
        }

        boolean serverReply = rpc.invokeSetLeader(isLeader, leaderId);
        if (!serverReply)
            return false;

        if (!isLeader)
            return true;

        rpc.invokeNewBallot(nextBallotNumber, currentConfig, currentConfig, leaderId);
        pendingBallotNumber = nextBallotNumber;
        nextBallotNumber++;

        return true;
    }

    public void reconfig(int config){
        if (config == currentConfig)
            return;

        prevConfig = currentConfig;
        currentConfig = config;

        rpc.invokeNewBallot(nextBallotNumber, currentConfig, prevConfig, currentLeader);
        pendingBallotNumber = nextBallotNumber;
        nextBallotNumber++;
    }

    public synchronized boolean completed(int ballotNum){
        if (ballotNum != pendingBallotNumber)
            return false;

        largestCompleteBallotNumber = pendingBallotNumber;
        pendingBallotNumber = -1;

        return true;
    }
    /*
    * b => represents the new ballot number that is going to be *ACTIVATED*
    * previousBallotNumber => this.largestCompleteBallotNumber < b
    */
    public void newBallot(int b, int newConfig, int prevConfig){
        //TODO: invoke rpc for sending newBallot
    }

}

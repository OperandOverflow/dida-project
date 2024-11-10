package dadkvs.master;

import dadkvs.master.rpc.MasterRPC;

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

    private final int[][] configs = {
            { 0,  1,  2, -1, -1},
            {-1,  1,  2,  3, -1},
            {-1, -1,  2,  3,  4}
    };

    private int pendingConfig;
    private int completedConfig;

    private int currentLeader;

    public VerticalPaxosMaster() {
        this.rpc = new MasterRPC();

        this.largestCompleteBallotNumber = 0;
        this.pendingBallotNumber = -1;
        this.nextBallotNumber = largestCompleteBallotNumber + 1;

        this.completedConfig = 0;
        this.pendingConfig = -1;

        this.currentLeader = -1;
    }

    /**
     * Activates a leader in the current configuration
     * @param isLeader true if the leader is being activated, false if it's being deactivated
     * @param leaderId the id of the leader to be activated
     * @return true if the leader was activated, false otherwise
     */
    public boolean setLeader(boolean isLeader, int leaderId){
        System.out.println("[Info] Activating leader with id: " + leaderId);
        // Vertical Paxos only allows leader activation
        if (!isLeader)
            return false;

        if (leaderId >= configs[completedConfig].length || leaderId < 0) {
            System.out.println("[Error] Invalid leader id: " + leaderId);
            return false;
        }

        // If the leader doesn't belong to the current configuration, it can't be the leader
        if (configs[completedConfig][leaderId] == -1){
            System.out.println("[Error] Leader doesn't belong to the current configuration");
            System.out.println("            leader id: " + currentLeader + " at configuration: " + this.completedConfig);
            return false;
        }

        pendingBallotNumber = nextBallotNumber;
        nextBallotNumber++;
        boolean result = rpc.invokeNewBallot(pendingBallotNumber, completedConfig, completedConfig, leaderId);
        if (result)
            currentLeader = leaderId;

        System.out.println("[Debug] Pending ballot number: " + pendingBallotNumber);
        System.out.println("[Info] New Ballot response: " + result);

        return result;
    }

    /**
     * Reconfigures the system to a new configuration
     * @param config the new configuration to be set
     * @return true if the reconfiguration was successful, false otherwise
     */
    public boolean reconfig(int config){
        System.out.println("[Info] Reconfiguring to: " + config);
        if (config == completedConfig)
            return true;

        if (config >= configs.length || config < 0) {
            System.out.println("[Error] Invalid configuration: " + config);
            return false;
        }

        // If the current leader doesn't belong to the new configuration, it can't be the leader
        if (configs[config][currentLeader] == -1) {
            System.out.println("[Error] Current leader doesn't belong to the new configuration");
            System.out.println("            leader id: " + currentLeader + " at configuration: " + config);
            return false;
        }

        pendingBallotNumber = nextBallotNumber;
        nextBallotNumber++;
        //tavamos a mandar invokeNewBallot(pendingBallotNumber, currentConfig,...)
        boolean result = rpc.invokeNewBallot(pendingBallotNumber, config, completedConfig, currentLeader);
        if (result) {
            pendingConfig = config;
        }
        System.out.println("[Debug] Pending ballot number: " + pendingBallotNumber);
        System.out.println("[Info] New Ballot response: " + result);

        return result;
    }

    public boolean reconfigChangeLeader(int leaderId, int config) {
        System.out.println("[Info] Reconfiguring to: " + config + " with leader: " + leaderId);
        if (config >= configs.length || config < 0) {
            System.out.println("[Error] Invalid configuration: " + config);
            return false;
        }

        if (leaderId >= configs[config].length || leaderId < 0) {
            System.out.println("[Error] Invalid leader id: " + leaderId);
            return false;
        }

        if (configs[config][leaderId] == -1) {
            System.out.println("[Error] Current leader doesn't belong to the new configuration");
            System.out.println("            leader id: " + currentLeader + " at configuration: " + config);
            return false;
        }

        pendingBallotNumber = nextBallotNumber;
        nextBallotNumber++;
        boolean result = rpc.invokeNewBallot(pendingBallotNumber, config, completedConfig, leaderId);
        if (result) {
            pendingConfig = config;
            currentLeader = leaderId;
        }
        System.out.println("[Debug] Pending ballot number: " + pendingBallotNumber);
        System.out.println("[Info] New Ballot response: " + result);
        return result;
    }

    public synchronized boolean completed(int ballotNum){
        System.out.println("[Info] Completed: " + ballotNum);
        if (ballotNum != pendingBallotNumber) {
            System.out.println("[Error] Pending ballot number doesn't match");
            System.out.println("            expected: " + pendingBallotNumber);
            System.out.println("            received: " + ballotNum);
            return false;
        }

        largestCompleteBallotNumber = pendingBallotNumber;
        pendingBallotNumber = -1;

        if (pendingConfig != -1) {
            completedConfig = pendingConfig;
            pendingConfig = -1;
        }

        System.out.println("[Info] Accepted: " + ballotNum);
        return true;
    }
}

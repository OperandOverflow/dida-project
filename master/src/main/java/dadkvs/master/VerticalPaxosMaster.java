package dadkvs.master;

public class VerticalPaxosMaster {

    /*
    * The master creates b => a new number added to the subset of ballot numbers (largestCompleteBallotNumber + 1)
    * this means that the new configuration is *ACTIVATED* and that proposers can start writing to it.
    */
    private int largestCompleteBallotNumber; //current ballot number
    private int nextBallotNumber;



    public VerticalPaxosMaster() {
        this.largestCompleteBallotNumber = 0;
        this.nextBallotNumber = largestCompleteBallotNumber + 1;
    }

    public void ReceiveReconfigReq(){

    }

    public int getLargestCompleteBallotNumber() {
        return this.largestCompleteBallotNumber;
    }

    /*
    * b => represents the new ballot number that is going to be *ACTIVATED*
    * previousBallotNumber => this.largestCompleteBallotNumber < b
    */
    public void newBallot(int b, int newConfig, int prevConfig){
        //TODO: invoke rpc for sending newBallot
    }

    public void CompleteAndActivate(){
        //TODO: Receive complete messages

        //TODO: Activate complete messages
    }
}

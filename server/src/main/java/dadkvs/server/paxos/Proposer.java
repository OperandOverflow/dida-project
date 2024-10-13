package dadkvs.server.paxos;

import dadkvs.server.ServerState;
import dadkvs.server.paxos.messages.*;

import java.util.Hashtable;

public class Proposer {

    private final ServerState serverState;

    private final int MAJORITY;

    private final SimplePaxosRPC rpc;

    public Proposer(ServerState serverState) {
        this.serverState = serverState;
        this.MAJORITY = serverState.n_servers / 2 + 1;
        this.rpc = new SimplePaxosRPC(serverState);
    }

    //propose function
    public synchronized PrepareMsg propose(){
        PrepareMsg msg = new PrepareMsg();
        return msg;
    }


    //commit function
    public synchronized AcceptMsg accept(){
        AcceptMsg msg = new AcceptMsg();
        return msg;
    }
}

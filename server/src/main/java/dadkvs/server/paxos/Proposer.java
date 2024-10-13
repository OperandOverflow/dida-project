package dadkvs.server.paxos;

import dadkvs.server.ServerState;

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
}

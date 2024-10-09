package dadkvs.server;

import dadkvs.DadkvsPaxosServiceGrpc;
import dadkvs.DadkvsServerSyncServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

/**
 * This class initializes the gRPC channels and stubs for the server-to-server communication.⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
 */
public class ServerRpcStubs {

    private final ServerState server_state;

    private ManagedChannel[] channels;

    public DadkvsServerSyncServiceGrpc.DadkvsServerSyncServiceStub[] server_sync_stubs;
    public DadkvsPaxosServiceGrpc.DadkvsPaxosServiceStub[] paxos_stubs;

    public ServerRpcStubs(ServerState state) {
        this.server_state = state;
        initiate();
    }

    private void initiate() {
        this.channels = new ManagedChannel[server_state.n_servers];

        this.server_sync_stubs = new DadkvsServerSyncServiceGrpc.DadkvsServerSyncServiceStub[server_state.n_servers];
        this.paxos_stubs = new DadkvsPaxosServiceGrpc.DadkvsPaxosServiceStub[server_state.n_servers];

        for(int i = 0; i < server_state.n_servers; i++) {
            int port = this.server_state.base_port + i;
            this.channels[i] = ManagedChannelBuilder.forAddress(server_state.ip_address, port).usePlaintext().build();
            this.server_sync_stubs[i] = DadkvsServerSyncServiceGrpc.newStub(this.channels[i]);
            this.paxos_stubs[i] = DadkvsPaxosServiceGrpc.newStub(this.channels[i]);
        }

    }

    public void shutdown() {
        for (ManagedChannel channel : channels) {
            channel.shutdown();
        }
    }


}

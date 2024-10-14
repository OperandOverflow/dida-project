package dadkvs.server;

import dadkvs.server.paxos.*;
import dadkvs.server.rpc.PaxosRPC;

import java.util.concurrent.atomic.AtomicBoolean;

public class ServerState {
    public AtomicBoolean   i_am_leader;
    public int             debug_mode;
    public int             base_port;
    public int             my_id;
    public int             store_size;
    public int             n_servers;
    public String          ip_address;
    public KeyValueStore   store;
    public MainLoop        main_loop;
    public Thread          main_loop_worker;

    public RequestHandler  request_handler;
    public ConsoleConfig   consoleConfig;

    public PaxosRPC        paxos_rpc;
    public Proposer        proposer;
    public Acceptor        acceptor;
    public Learner         learner;

    
    public ServerState(int kv_size, int port, int myself) {
        base_port = port;
        my_id = myself;
        i_am_leader = new AtomicBoolean(false);
        debug_mode = 0;
        store_size = kv_size;
        n_servers = 5;
        ip_address = "localhost";
        store = new KeyValueStore(kv_size);
        main_loop = new MainLoop(this);
        main_loop_worker = new Thread (main_loop);
        main_loop_worker.start();

        request_handler = new RequestHandler(this);
        consoleConfig = new ConsoleConfig(this);

        paxos_rpc = new PaxosRPC(this);
        proposer = new Proposer(this);
        acceptor = new Acceptor(this);
        learner = new Learner(this);
    }
}

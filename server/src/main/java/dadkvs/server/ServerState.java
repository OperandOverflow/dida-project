package dadkvs.server;

import dadkvs.server.paxos.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ServerState {
    public Lock            i_am_leader_lock;
    public boolean         i_am_leader;
    public int             debug_mode;
    public int             base_port;
    public int             my_id;
    public int             store_size;
    public int             n_servers;
    public String          ip_address;
    public KeyValueStore   store;
    public MainLoop        main_loop;
    public Thread          main_loop_worker;
    public Paxos           paxos;
    public OrderedRequestProcessor ordered_request_processor;
    public ServerRpcStubs  rpc_stubs;
    public ServerSync      serverSync;

    
    public ServerState(int kv_size, int port, int myself) {
        base_port = port;
        my_id = myself;
        i_am_leader_lock = new ReentrantLock();
        i_am_leader = false;
        debug_mode = 0;
        store_size = kv_size;
        n_servers = 5;
        ip_address = "localhost";
        store = new KeyValueStore(kv_size);
        main_loop = new MainLoop(this);
        main_loop_worker = new Thread (main_loop);
        main_loop_worker.start();
        paxos = new SimplePaxosImpl(this);
        ordered_request_processor = new OrderedRequestProcessor(this);
        rpc_stubs = new ServerRpcStubs(this);
        serverSync = new ServerSync(this);
    }
}

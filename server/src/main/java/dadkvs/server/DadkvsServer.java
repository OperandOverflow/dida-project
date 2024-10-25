package dadkvs.server;

import dadkvs.server.rpc.DadkvsConsoleServiceImpl;
import dadkvs.server.rpc.DadkvsMainServiceImpl;
import dadkvs.server.rpc.DadkvsPaxosServiceImpl;
import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;


public class DadkvsServer {

    static ServerState server_state;
								    
    /** Server host port. */
    private static int port;
    
    public static void main(String[] args) throws Exception {
		final int kvsize = 1000;

		System.out.println(DadkvsServer.class.getSimpleName());

		// Print received arguments.
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

		// Check arguments.
		if (args.length < 2) {
			System.err.println("Argument(s) missing!");
			System.err.printf("Usage: java %s baseport replica-id%n", Server.class.getName());
			return;
		}

		int base_port = Integer.valueOf(args[0]);
		int my_id     = Integer.valueOf(args[1]);

		server_state = new ServerState(kvsize, base_port, my_id);

		port = base_port + my_id;

		final BindableService service_impl = new DadkvsMainServiceImpl(server_state);
		final BindableService console_impl = new DadkvsConsoleServiceImpl(server_state);
		final BindableService paxos_impl   = new DadkvsPaxosServiceImpl(server_state);

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			System.out.println("Shutting down server");
			server_state.main_loop_worker.interrupt();
            try {
                server_state.paxos_rpc.shutdown();
            } catch (InterruptedException e) {
            }
        }));

		// Create a new server to listen on port.
		Server server = ServerBuilder.forPort(port)
									 .addService(service_impl)
				                 	 .addService(console_impl)
									 .addService(paxos_impl)
				  					 .build();
		// Start the server.
		server.start();
		// Server threads are running in the background.
		System.out.println("Server started");

		// Do not exit the main thread. Wait until server is terminated.
		server.awaitTermination();
    }
}

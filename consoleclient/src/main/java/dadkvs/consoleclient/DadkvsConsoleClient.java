package dadkvs.consoleclient;

import java.util.ArrayList;
import java.util.Iterator;

/* these imported classes are generated by the contract */
import dadkvs.*;

import dadkvs.util.GenericResponseCollector;
import dadkvs.util.CollectorStreamObserver;
import java.util.Scanner;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class DadkvsConsoleClient {

	static final String LINE_SEPARATOR = System.lineSeparator();

	static int n_servers = 5;

	static String host;
	static int port;
	static int master_port = 8090;
	static String[] targets;

	static ManagedChannel[] channels;
	static ManagedChannel master_channel;
	static DadkvsMainServiceGrpc.DadkvsMainServiceStub[] main_async_stubs;
	static DadkvsConsoleServiceGrpc.DadkvsConsoleServiceStub[] console_async_stubs;
	static DadkvsMasterServiceGrpc.DadkvsMasterServiceStub master_async_stub;

	static Scanner scanner;

	static final String HELP_MESSAGE = 	"================ Help ================" + LINE_SEPARATOR +
										" leader on <replica>" + LINE_SEPARATOR +
										" debug <mode> <replica>" + LINE_SEPARATOR +
										" reconfig <configuration>" + LINE_SEPARATOR +
										" exit" + LINE_SEPARATOR;


    public static void main(String[] args) {

		System.out.println(DadkvsConsole.class.getSimpleName());

		// receive and print arguments
		System.out.printf("Received %d arguments" + LINE_SEPARATOR, args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

		// check arguments
		if (args.length < 2) {
			System.err.println("[Error] Argument(s) missing!");
			System.err.printf("Usage: java %s <host> <port>" + LINE_SEPARATOR, DadkvsConsoleClient.class.getName());
			return;
		}

		initiate(args);

		String command;
		boolean isleader;
		int replica, mode, configuration, client_id = 0, sequence_number = 0;

		boolean keep_going = true;

		while (keep_going) {
			System.out.print("console> ");
			command = scanner.nextLine();
			String[] commandParts = command.split(" ");
			String mainCommand = commandParts[0].toLowerCase();
			String parameter1 = commandParts.length > 1 ? commandParts[1] : null;
			String parameter2 = commandParts.length > 2 ? commandParts[2] : null;
			String parameter3 = commandParts.length > 3 ? commandParts[3] : null;

			switch (mainCommand) {

			case "help":
				System.out.println(HELP_MESSAGE);
				break;

			case "leader":
				if (parameter1 == null || parameter2 == null) {
					System.out.println("\t[Error] Usage: leader on <replica>");
					break;
				}
				if(parameter1.equals("off")){
					System.out.println("\t[Error] Can only turn leader on - \n" +
										"\t'leader off' responsibility is off to the Master");
					break;
				}

				if (!parameter1.equals("on")) {
					System.out.println("\t[Error] Usage: leader on <replica>");
					break;
				}

				isleader = true;

				try {
					replica = Integer.parseInt(parameter2);
					System.out.println("\t[Info] Setting leader " + isleader + " replica " + replica);

					DadkvsMaster.DefineLeaderRequest.Builder setleader_request = DadkvsMaster.DefineLeaderRequest.newBuilder();
					setleader_request.setIsleader(isleader).setServerid(replica);

					ArrayList<DadkvsMaster.DefineLeaderReply> setleader_responses = new ArrayList<>();
					GenericResponseCollector<DadkvsMaster.DefineLeaderReply> setleader_collector = new GenericResponseCollector<>(setleader_responses, 1);
					CollectorStreamObserver<DadkvsMaster.DefineLeaderReply> setleader_observer =  new CollectorStreamObserver<>(setleader_collector);

					master_async_stub.setleader(setleader_request.build(), setleader_observer);
					setleader_collector.waitForTarget(1);
					if (!setleader_responses.isEmpty()) {
						Iterator<DadkvsMaster.DefineLeaderReply> setleader_iterator = setleader_responses.iterator();
						DadkvsMaster.DefineLeaderReply setleader_reply = setleader_iterator.next();
						System.out.println("\treply = " + setleader_reply.getIsleaderack());
					} else
						System.out.println("\t[Error] No reply received");
				} catch (NumberFormatException e) {
					System.out.println("\t[Error] Usage: leader on <replica>");
				}
				break;

			case "debug":
				System.out.println("\tdebug " + parameter1 + " " + parameter2);
				if (parameter1 == null || parameter2 == null) {
					System.out.println("\t[Error] Usage: debug <mode> <replica>");
					break;
				}
				try {
					mode  =  Integer.parseInt(parameter1);
					replica =  Integer.parseInt(parameter2);
					System.out.println("\tsetting debug with mode " + mode + " on replica " + replica);


					DadkvsConsole.SetDebugRequest.Builder setdebug_request = DadkvsConsole.SetDebugRequest.newBuilder();
					ArrayList<DadkvsConsole.SetDebugReply> setdebug_responses = new ArrayList<>();
					GenericResponseCollector<DadkvsConsole.SetDebugReply> setdebug_collector = new GenericResponseCollector<>(setdebug_responses, 1);
					CollectorStreamObserver<DadkvsConsole.SetDebugReply> setdebug_observer = new CollectorStreamObserver<>(setdebug_collector);
					setdebug_request.setMode(mode);
					console_async_stubs[replica].setdebug(setdebug_request.build(), setdebug_observer);
					setdebug_collector.waitForTarget(1);
					if (!setdebug_responses.isEmpty()) {
						Iterator<DadkvsConsole.SetDebugReply> setdebug_iterator = setdebug_responses.iterator();
						DadkvsConsole.SetDebugReply setdebug_reply = setdebug_iterator.next();
						System.out.println("\treply = " + setdebug_reply.getAck());
					} else
						System.out.println("\t[Error] No reply received");
				} catch (NumberFormatException e) {
				   System.out.println("\t[Error] Usage: debug <mode> <replica>");
				}
				break;

			case "reconfig":
				System.out.println("\treconfig " + parameter1);
				int responses_needed = 1;
				if (parameter1 == null) {
					System.out.println("\t[Error] Usage: reconfig <configuration>");
					break;
				}
				try {
					configuration  =  Integer.parseInt(parameter1);
					System.out.println("\treconfiguring to configuration " + configuration);

					DadkvsMaster.ReconfigRequest.Builder reconfig_request = DadkvsMaster.ReconfigRequest.newBuilder();
					ArrayList<DadkvsMaster.ReconfigReply> reconfig_response = new ArrayList<>();
					GenericResponseCollector<DadkvsMaster.ReconfigReply> reconfig_collector = new GenericResponseCollector<>(reconfig_response, n_servers);
					reconfig_request.setConfignum(configuration);

					CollectorStreamObserver<DadkvsMaster.ReconfigReply> read_observer = new CollectorStreamObserver<>(reconfig_collector);
					master_async_stub.reconfig(reconfig_request.build(), read_observer);
					reconfig_collector.waitForTarget(1);

					if (reconfig_response.getFirst().getAck()) {
						System.out.println("\treconfig acknowledged");
					} else
						System.out.println("\treconfig not acknowledged");
				} catch (NumberFormatException e) {
					System.out.println("\t[Error] Usage: reconfig <configuration>");
				}
				break;

			case "exit":
				keep_going = false;
				break;

			case "":
				break;

			default:
				System.out.println("\t[Error] Unknown command: " + mainCommand);
				break;
			}
		}
		terminate();
    }

	private static void initiate(String[] args) {
		System.out.println("[Info] Initiating console client");// set servers
		host = args[0];
		port = Integer.parseInt(args[1]);
		targets  = new String[n_servers];
		for (int i = 0; i < n_servers; i++) {
			int target_port = port +i;
			targets[i] = host + ":" + target_port;
			System.out.printf("[Info] targets[%d] = %s%n", i, targets[i]);
		}

		// Let us use plaintext communication because we do not have certificates
		channels = new ManagedChannel[n_servers];
		for (int i = 0; i < n_servers; i++) {
			channels[i] = ManagedChannelBuilder.forTarget(targets[i]).usePlaintext().build();
		}

		main_async_stubs = new DadkvsMainServiceGrpc.DadkvsMainServiceStub[n_servers];
		console_async_stubs = new DadkvsConsoleServiceGrpc.DadkvsConsoleServiceStub[n_servers];

		for (int i = 0; i < n_servers; i++) {
			main_async_stubs[i] = DadkvsMainServiceGrpc.newStub(channels[i]);
			console_async_stubs[i] = DadkvsConsoleServiceGrpc.newStub(channels[i]);
		}

		master_channel = ManagedChannelBuilder.forAddress(host, master_port).usePlaintext().build();
		master_async_stub = DadkvsMasterServiceGrpc.newStub(master_channel);

		scanner = new Scanner(System.in);
	}

	private static void terminate() {
		System.out.println("[Info] Terminating console client");
		for (int i = 0; i < n_servers; i++) {
			channels[i].shutdownNow();
		}
		master_channel.shutdownNow();
		scanner.close();
	}
}




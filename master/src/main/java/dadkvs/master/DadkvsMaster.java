package dadkvs.master;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import dadkvs.master.rpc.DadkvsMasterServiceImpl;

/**
 * This is NOT a class dedicated to Mestre André!
 * ![Salaam Aleikum] Mestre André [Wa alaikum assalam]!
 */
public class DadkvsMaster {

    private static int port;

    private static final String mestreAndre =
            "@@@@@@@@@@@@@@@@#*@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@#+\n" +
            "@@@@@@@%%#%@@@@@##@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@%*\n" +
            "@@@@@@@@@%%@@@%@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@\n" +
            "@@@@@@@@%@%@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@\n" +
            "@@@@@@@@%%#@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@\n" +
            "@@@@@@@@@@@%@@@@@@@@@@@@@@@@%%##%@@@@@@@@@@#%@@@@@@@@@@@@@@@@@@@@@@@@@\n" +
            "@@@@@@@@@@@@@@#@@@@@@@@@@*+*+=-+==++++++++++=++@@@@@@@@@@@@@@@@@@@@@@@\n" +
            "@@@@@@@@@@@@%@#@@@@@@@%+++=--==+***###*#++-======*@@@@@@@@@@@@@@@@@@@@\n" +
            "@@@@@@@@@@@@@@%@@@@@@+++===*####%%%##########*====+=%@@@@@@@@@@@@@@@@@\n" +
            "@@@@@@@@@@@@@@@@@@@+==+=*##%%%##%#%##########%##+=====+@@@@@@@@@@@@@@@\n" +
            "@@@@@@@@@@@@@@@@@@+=+=#%%%%%%%%%%###########%#%###+===-+@@@@@@@@@@@@@@\n" +
            "@@@@@@@@@@@@@@@@@+==+@@@@%%%%%%%##########%#%#%%%###===#%@@@@@@@@@@@@@\n" +
            "@@@@@@@@@@@@@@@@++=%@@@@%%%%%%%%%##%##%#%%%%###%%%%#*==+*@@@@@@@@@@@@@\n" +
            "@@@@@@@@@@@@@@@*+=@@@@@@%%%%%%%%%%%####%%##%%%%%#%%%#++=+@@@@@@@@@@@@@\n" +
            "@@@@@@@@@@@@@@*++@@@@@%%#**#####%%%##**+===++**#%%%@#+==**@@@@@@@@@@@@\n" +
            "@@@@@@@@@@@@@*+++@%##+++=+**#%%%%%###+*+++*+==+++*#%%*+==*#@@@@@@@@@@@\n" +
            "@@@@@@@@@@@@#+==*%###%%%%#%%%%@@@%%%###%#%%%%####**#%%+==+#@@@@@@@@@@@\n" +
            "@@@@@@@@@@@@@++=#@@@@%%###%%%@@@%%%%%#########%%%%%%#%*=+++#@@@@@@@@@@\n" +
            "@@@@@@@@@@@%@=+-%@@@@%%#***##%@@%###%****===+*%%%%%%%%%+=+++%@@@@@@@@@\n" +
            "@@@@@@@@@@@@@===@@@@#*==--+=+%@@%##%%%*++**===+#-#%%%@%#+=+*#@@@@@@@@@\n" +
            "@@@@@@@@@@@@@--=@@%+*#%=***#@@@@%###%%%##**********%%@@%+++##%@@@@@@@@\n" +
            "@@@@@@@@@@@@@=-*@@@@@%%%%%@@@@@%%###%%%%%%#%######%%%%@@%=+++#@@@@@@@@\n" +
            "@@@@@@@@@@@@@*=%@@@@@@%%%%@@@@@@%%%%%%%%%#%####%%%%%%@@@@#=++*%%%@%*%@\n" +
            "@@@@@@@@@@@@@%*@@@@@@@%@%%@@@@@@%%%%%%%%@#%#%%%%%%%%%@@@@@+=+=+*#%%#%#\n" +
            "@@@@@@@@@@@@@**@@@@@@@@%%%%%*-=#%##+-=*%%##%%%%%%%%%@@@@@@++=++++*%%%#\n" +
            "@@@@@@@@@@@@@@#@@@@@@@@@@%%%##%%%%#**+***####%%%%%%%@@@@@@%-=++*+++%##\n" +
            "@@@@@@@@@@@@@@+@@@@@@@%%%@@%%%%%%%%###%###%%%##%%%%@@@@@@@@+==+**+=+*#\n" +
            "@@@@@@@@@@@@@@#@@@@@@@@@@@@%%%%#%%%%######%%%%%%%%%%@@@@@@@++#=+#*+#*+\n" +
            "@@@@@@@@@@@@@@@@@@@@@@@%@@@@@%%%#%#####%%#%%#%%%%%%%@%@@@@%#+*+=*%%%%%\n" +
            "@@@@@@@@@@@@@@@%@@@@@@%%@@%%%%####*######%%%%%#%%%%@@@@@@@@@==#@###%##\n" +
            "@@@@@@@@@@@@@@@%@@@@@%%#++#***====+=++==+++####%%%%@%@%@@@@@%%%###%#%#\n" +
            "@@@@@@@@@@@@@@@@@@@@@@%%%@%%########******#***#%#%%%%%%%%%%@%%%@#%%###\n" +
            "@@@@@@@@@@@@@@@%@@@@@@@@@@@@%%%%%%#%%%%%#%%%###%%%%%%%%%%@@@#%%@@#@###\n" +
            "@@@@@@@@@@@@@%+@@@@@@@@@@@@@@%%%%####%%%%%#%%%%#%%%%%%%%%@%%+=++%%%%%#\n" +
            "@@@@@@@@@@@@@@@@@@@@@@@@@@@@%%%%%#####%%##%#%%%%%%%%%%@%%%@@##*+#%@%##\n" +
            "@@@@@@@@@@@@@@@@@@@@@@@@%%%@%%%%%%#%%##%%##%%%%%%%%%%%%%%%%%****%@@##%\n" +
            "@@@@@@@@@@@@@@@@@@@@@@@@@@@@%%%%%%###%#%##%%%%%%%%%%%%%%%%%@@+*+%@%%#*\n" +
            "@@@@@@@@@@@@@@@@@@@#%%%%@@@@@%%%%%%##%%#%%%%%%%%%%%%%%%%%%%%@***#@@%%#\n" +
            "@@@@@@@@@@@@@@@@@@@**%%%%%@%%%%%%%####*%%%%%%%%%%%%%%%%%%%%@@%*##@#%#%\n" +
            "@@@@@@@@@@@@@@@@@@@**+%%#%%%%%%%%%####*%#%%#%%%%%%##%%%%%%%@@@#*#@@%%%\n" +
            "@@@@@@@@@@@@@@@@@@@*+*+@%%##%%%##*#####***###%###%#%%%%%%%%%@@%*%@@%%#\n" +
            "@@@@@@@@@@@@@@@@@@@*+**#@%%%%%##*#**********#%#%#%%%%%%%%%%%%%@#%@@@%%\n" +
            "@@@@@@@@@@@@@@@@@@@**+++@@%%%%######*#**####%#%%%%%%%%%%%%%%%%@@@@%%#%\n" +
            "@@@@@@@@@@@@@@@@@@@**+***@@%@%#%###%##*####%%%%%%%%%%%%%%%%%%%%@@@@@##\n" +
            "@@@@@@@@@@@@@@@@@@@*+**++%@@@%%%%%%####%%%%%%%%%%%%%%%%%%%%%%%%%%#%%%%\n" +
            "@@@@@@@@@@@@@@@@@@@****++=@@@@@@%%%%#%%%%%%%%%%%%%%%%%%%%%%%%%%#*****#\n" +
            "@@@@@@@@@@@@@@@@@@@****+==*@@@@@@%%%%%%%%%%%%%%%%%%%%%%%%%%%%#*+*+++==\n" +
            "@@@@@@@@@@@@@@@@@@@*++*-+++@@@@@@@%%%%%%%%%%%%%%%%%%%%%%%%%##++++=+=++\n";



    public static void main(String[] args) throws Exception {
        System.out.println(mestreAndre);

        System.out.println("Vertical Paxos Master - Proudly powered by Mestre Andre'");

        System.out.println(DadkvsMaster.class.getSimpleName());

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

        try {
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.err.println("Argument wrong type!");
            port = 8090;
        }

        VerticalPaxosMaster master = new VerticalPaxosMaster();

        final BindableService service_impl = new DadkvsMasterServiceImpl(master);

        Server server = ServerBuilder.forPort(port)
                                     .addService(service_impl)
                                     .build();
        server.start();

        System.out.println("Server started");

        server.awaitTermination();
    }
}
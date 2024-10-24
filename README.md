# DAD2425
Project course for DAD 2024-25

## Group 4
- Daniela Camarinha ist1112265
- Xiting Wang ist1112191
- Joana Matias ist1112438

# Compiling
The project must be compiled using `mvn clean install` in the root directory

# Deployment

Current implementation assumes that all modules run on the same physical machine and requires 5 active servers. 

The project is composed of four main components:
- Servers
- Clients
- ConsoleClient
- Master(Vertical Paxos)

On Windows, to deploy 5 servers, 1 client, 1 console and 1 Master, there is a script that can be executed by doing:
`./run-bat` in the root directory.
This is will deploy the servers starting from port 8080, id 0, and on.


## Servers

The servers run the base implementation. They are executed running the following command in the *server* directory:

`mvn exec:java -D"exec.args"="{port} {id}"`

Where you must fill in the following arguments:
- **{id}**: Sequential id of the server. Current implementation requires servers to be ID'ed starting from *0* to *N-1* servers.
- **{port}**: Base port of all servers. **All servers should use the same port**. The Server binded port will be  **{port} + {id}**. 

## Client

A client that executes transactions. It is executed by running the following command in the *client* directory:

`mvn exec:java`

The client module opens a terminal from where students may issue commands. The following commands are available:
- `help` - Shows the full command list;
- `read {read_key_1} ` - Reads a key (useful for debug);
- `tx {read_key_1} {read_key_2} {write_key}` - Takes as input 3 keys and executes a transaction with 2 reads and 1 write respectively;
- `loop` - Runs multiple transactions, one after another, in a loop;
- `lenght {loop-lenght}` - Defines the number of transactions executed when looping;
- `time {sleep-range}` - Slows down transactions by sleeping a random amount of time in sleep-range between reads and commit;
- `exit` - Gracefully finishes the client.

## ConsoleClient

The console client servers as a front-end to issue configuration settings to servers. It is executed by running the following command in the *consoleclient* directory:

`mvn exec:java`

The console client opens a terminal from where students may issue configuration changes to servers. The following commands are available:
- `help` - Shows the full command list;
- `leader on/off replica_id` - Instructs a replica to start/stop acting as a Paxos leader;
- `debug mode replica_id` - Activates debug on a given replica;
- `reconfig configuration` - Executes a transaction on key 0 to change the configuration;
- `exit` - Gracefully finishes the console.

## Protobuffs and Utils

To support these modules, the project has two additional directories:
- *contract*, holding the required `.proto` files;
- *util*, holding the general classes to collect RPC responses

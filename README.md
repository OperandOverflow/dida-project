# Concurrent replicated transaction key-value store

## Overview
This is a project developed within the course of Design and Implementation of Distributed Applications (DIDA) at Instituto Superior Técnico, University of Lisbon. 
The project consists of a replicated consistent key-value store that uses the Vertical Paxos algorithm to ensure fault tolerance and flexibility of reconfiguration. 

The system is composed of a set of servers that replicate the state of the key-value store and a client that issues transactions to the servers. The client can read and write keys in the key-value store, and the servers ensure that all transactions are executed in a linearizable order. The system is also able to reconfigure itself by adding or removing servers, and the servers can elect a leader to coordinate the reconfiguration process.

For simplicity, the current implementation assumes that there is one single client and 5 servers and all modules run on the same physical machine. Moreover, each server, as well as the client, knows the address of all other servers.

**Co-authors**: [Daniela Camarinha](https://github.com/DanielaDoesCode) & [Joana Matias](https://github.com/jrmatias) and special thanks to [Mestre André](https://github.com/4Sparkz) for the blessing 🙏.

## Table of contents
- [Getting started](#getting-started)
- [Building](#building)
- [Deployment](#deployment)
  - [Servers](#servers)
  - [Client](#client)
  - [ConsoleClient](#consoleclient)
  - [Server coordination master](#server-coordination-master)
- [Usage](#usage)
- [Feedback](#feedback)
- [License](#license)

## Getting started
This project requires [Java 22+](https://www.oracle.com/java/technologies/javase/jdk22-archive-downloads.html) and [Maven 3.9.x](https://maven.apache.org/download.cgi) to be installed on your machine to build and run the project.

## Building
To build the project, run the following command in the root directory:
```bash
mvn clean install
```
Maven will download all dependencies and compile the project.

## Deployment
The project is composed of four main components, please deploy them in the following order:

**Note**: For Windows machines, we have a script that automatically launches all components. Run the following command in the root directory:
```bash
./run.bat
```

### Servers
The servers run the base implementation. They are executed running the following command in the *server* directory:
```bash
mvn exec:java -D exec.args="8080 {id}"
```
**Note**: On Windows, it is necessary to use ``mvn exec:java -D "exec.args"="8080 {id}"`` instead.

Where you must fill in the following argument:
- **{id}**: Sequential id of the server. Current implementation requires servers to be ID'ed starting from *0* to *4* servers.

### Client
A client that executes transactions. It is executed by running the following command in the *client* directory:
```bash
mvn exec:java
```

### ConsoleClient
The debug console to control the servers. It is executed by running the following command in the *consoleclient* directory:
```bash
mvn exec:java
```

### Server coordination master
The master module is the Master in Vertical Paxos. It is executed by running the following command in the *master* directory:
```bash
mvn exec:java
```

## Usage
The client module opens a terminal from where students may issue commands. The following commands are available:
- `help` - Shows the full command list;
- `read {read_key}` - Reads a key (useful for debug);
- `tx {read_key_1} {read_key_2} {write_key}` - Takes as input 3 keys and executes a transaction with 2 reads and 1 write respectively;
- `loop` - Runs multiple transactions, one after another, in a loop, defined by the following parameters;
- `lenght {loop-lenght}` - Defines the number of transactions executed when looping;
- `time {sleep-range}` - Slows down transactions by sleeping a random amount of time in sleep-range between reads and commit;
- `exit` - Gracefully finishes the client.

The console client opens a terminal from where students may issue configuration changes to servers. The following commands are available:
- `help` - Shows the full command list;
- `leader on {server_id}` - Instructs a server to start acting as a Paxos leader;
- `debug {mode} {server_id}` - Activates debug on a given server;
- `reconfig {configuration}` - Executes a transaction on key 0 to change the configuration;
- `reconfleader {leader_id} {configuration}` - Activates the leader with the given id and changes the configuration;
- `exit` - Gracefully finishes the console.

## Feedback
For any questions or feedback, please feel free to reach out to me at wangxiting01917@gmail.com.

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

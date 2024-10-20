## **Bugs to fix**
- [X] Paxos is not working correctly, the leader gets stuck when invoking `prepare` of other replicas. Try to remove `synchronized` keyword 
  from the `prepare` method and see if it works.
- [X] Duplicated requests when having multiple leaders.
- [X] With two leaders, when turning off the leader on one replica, the Paxos should continue usually.

## **List of tasks to be done**
- [X] When a certain replica has a pending request, and it was selected to be the leader, it should
  immediately start serializing the request and run Paxos
    - Possible solution:
      - Use lock to protect the `i_am_leader` variable, so any function needs to acquire the lock before reading/writing.
      - When a replica receives a request, and it's not the leader, it stores the request in a queue.
      - When the replica becomes the leader, i.e. when `setLeader` method is invoked, it checks the queue and starts serializing the request.
- [X] Implement debug modes
    - [X] Debug mode 1: Crash
    - [X] Debug mode 2: Freeze
    - [X] Debug mode 3: Unfreeze
    - [X] Debug mode 4: Random slow
    - [X] Debug mode 5: Cancel slow
- [ ] Implement the Vertical Paxos for step 3
    - [ ] Create a new maven package for the Master in Vertical Paxos
        - Vertical Paxos Master jobs:
            - Keep track of the ballot number
            - When receives a reconfig request from the Console:
                - Increment the ballot number
                - Send a `newballot` request to all replicas
    - [X] Add new message types and RPC functions in `DadkvsPaxos.proto`:
      ```protobuf
          message NewBallotRequest {
              int32 ballotnumber = 1;
              int32 prevconfig   = 2;
              int32 newconfig    = 3;
          }
          message NewBallotResponse {
              bool ack           = 2;
          }
          service DadkvsMasterService {
              rpc newballot(NewBallotRequest) returns (NewBallotResponse) {}
          }
      ```
    - [ ] Redirect console's configuration request to the Vertical Paxos Master

## **Bugs to fix**
- [X] Paxos is not working correctly, the leader gets stuck when invoking `prepare` of other replicas. Try to remove `synchronized` keyword 
  from the `prepare` method and see if it works.
- [X] Duplicated requests when having multiple leaders.
- [X] With two leaders, when turning off the leader on one replica, the Paxos should continue usually.

## **Tickets**
- ### Ticket 1: Remove or deactivate the `leader off` functionality from the console.
    - Description: In Vertical Paxos, there is no need to explicitly turn off a leader, since by activating a new leader with a higher ballot number
        the previous leader will not be able to propose any new value. Thus, Vertical Paxos does not support this feature. Therefore, the `leader off` functionality
        should be removed or deactivated from the interface of the console.
    - Priority: Medium
    - Assignee: Daniela
    - Status: Resolved

- ### Ticket 2: Adapt the code of `propose()` function of the Proposer class to Vertical Paxos.
    - Description: The leader should not be able to change the ballot number, all consensus should use the ballot number appointed by the Master.
        When the leader can't get enough Promise or Accepted, it should stay blocked and wait for `newBallot` to be invoked.
    - Priority: High
    - Assignee: None
    - Status: Open

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
    - [ ] Remove the `leader off` functionality from the console
    - [ ] In Proposer, if the `propose` function didn't receive
    - [ ] In Proposer, when newBallot is activated, let the propose to finish or block before proceeding


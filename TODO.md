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
- [ ] Implement debug modes
    - [ ] Debug mode 1: Crash
    - [ ] Debug mode 2: Freeze
    - [ ] Debug mode 3: Unfreeze
    - [ ] Debug mode 4: Random slow
    - [ ] Debug mode 5: Cancel slow
- [ ] Implement the configuration change for the step 3
- [ ] Implement the Stoppable Paxos or Vertical Paxos for the step 3
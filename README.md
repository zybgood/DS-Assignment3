## Assignment 3
### Zhao Yanbo a1950939
### Paxos Voting System for Council President Election

This assignment is designed to implement a Paxos-style voting protocol to facilitate the election of a council president for the Adelaide Suburbs Council. The system demonstrates how consensus can be reached among distributed nodes (council members) in an environment where communication failures, delays, and simultaneous proposals are possible. The election involves nine council members (M1 to M9), with specific behavior modeled for each member to simulate real-world communication challenges.

### Components and Workflow

#### 1. **CouncilElection Class**

This is the core implementation of the Paxos voting system. It models the behavior of nine council members who are eligible to participate in the election for the council president. The class uses socket-based communication to simulate network-based message exchange between members. The key functionalities include:

- **Proposers and Acceptors**: A proposer is a member who starts the election process by sending a proposal for a candidate to all the other members (acceptors). Any of M1, M2, or M3 can act as a proposer.

- **Server and Client Communication**: Each council member is represented by a server that listens on a specific port, and proposers act as clients that connect to these servers to send their proposals.

- **Randomized Member Behavior**: Different members exhibit different response behaviors, such as delayed responses, dropped messages, or going offline. For example:
    - M1, M2, and M3 act as candidates who always vote for themselves.
    - Members M4 to M9 vote randomly among the candidates (M1, M2, M3) and may introduce random delays or fail to respond.

- **Immediate Response Mode**: The implementation also includes an "immediate response mode" that can be toggled to ensure all members respond without delay, which is useful for testing scenarios where consensus needs to be achieved quickly.

- **Paxos Consensus Mechanism**: The project uses a `ReentrantLock` to ensure that only one proposer can run at a time, preventing simultaneous proposals from causing inconsistencies in the election process. This lock simulates the consensus feature of Paxos, ensuring that only one proposal is adopted.

#### 2. **CouncilElection_Test Class**

The `CouncilElection_Test` class serves as a testing harness to validate the functionality of the voting system under various scenarios. The test cases simulate different conditions to ensure the robustness of the system, including:

- **Immediate Response Test**: This scenario sets all members to respond immediately to verify that consensus can be achieved when there are no delays or failures in the network.

- **Random Delays and Failures Test**: This scenario allows for random response delays and dropped messages, especially among members M2 and M3, who are prone to going offline or experiencing network issues. This test ensures that the voting system is resilient to these types of failures.

- **Simultaneous Proposers Test**: This test creates multiple proposers (M1, M2, M3) attempting to start an election simultaneously. The `ReentrantLock` is used to manage this conflict and ensure that only one proposal is ultimately considered, while others are either blocked or rejected.

### Detailed Flow of Election Process

1. **Initialization**
    - The project starts by initializing server sockets for each council member (M1 to M9). Each member runs on a separate thread, listening for incoming proposals.
    - The proposer threads (M1, M2, M3) are also initialized, and they attempt to start an election by sending proposals to all council members.

2. **Proposal Phase**
    - When a proposer sends a proposal, all acceptors (M1 to M9) receive the proposal. Based on the member's behavior, they may respond immediately, delay the response, or drop the message entirely.
    - Each proposer collects responses from the members and counts the votes received. Members M1, M2, and M3 vote for themselves, while others vote randomly among the candidates.

3. **Conflict Resolution**
    - To simulate the consensus nature of Paxos, the `ReentrantLock` ensures that only one proposal can proceed at a time. If multiple proposers attempt to start an election simultaneously, only one will obtain the lock and proceed, while others will be blocked until the current election concludes.

4. **Voting Outcome**
    - After collecting the votes, the proposer determines whether any candidate has received a majority (i.e., more than half the votes, or at least 5 out of 9). If a candidate achieves a majority, they are declared the winner.
    - If no candidate receives the majority, the outcome is that no one is elected, and a new election must be conducted.

### Key Features and Challenges Addressed

1. **Fault Tolerance**
    - The system is designed to handle scenarios where members fail to respond or go offline. This is crucial for distributed systems, where network failures and node outages are common.

2. **Consensus in Distributed Systems**
    - The use of `ReentrantLock` helps simulate a Paxos-like consensus, ensuring that only one proposal is adopted at a time, thereby avoiding conflicts and achieving consistency in a distributed environment.

3. **Testing Different Scenarios**
    - The `CouncilElection_Test` class provides comprehensive coverage for different scenarios, including no delays, random delays, and multiple simultaneous proposals. This helps ensure that the system behaves correctly under varying network conditions and proposer behaviors.
      Expected Output and Explanation

### Expected Output and Explanation

The expected output for different test scenarios is as follows:

1. **Immediate Response Test**
   - **Expected Output**: All members respond immediately to the proposal, and the proposer collects all 9 votes without any delay. The output will show that each member has received the proposal and responded promptly. If M1 is the proposer, the output might look like:
     ```
     Running test: Immediate Response Mode
     Received response from M1: Vote for M1
     Received response from M2: Vote for M2
     Received response from M3: Vote for M3
     Received response from M4: Vote for M1
     Received response from M5: Vote for M2
     ...
     Votes for M1: 4
     Votes for M2: 3
     Votes for M3: 2
     No candidate received the majority. No one is elected as President.
     ```
   - **Explanation**: This test verifies that the system can handle immediate responses from all members. It helps validate the basic communication flow without any delays or failures.

2. **Random Delays and Failures Test**
   - **Expected Output**: Members may respond after a delay, fail to respond, or go offline. The output will show varying response times, dropped messages, and members going offline. For example:
     ```
     Running test: Random Delays and Failures
     Member M2 went offline.
     Received response from M1: Vote for M1
     Member M3 did not respond (message dropped).
     Received response from M4: Vote for M2
     ...
     Votes for M1: 3
     Votes for M2: 4
     Votes for M3: 1
     No candidate received the majority. No one is elected as President.
     ```
   - **Explanation**: This test ensures that the system remains functional even when members are delayed or fail. It demonstrates the resilience of the voting process under real-world conditions where network reliability is not guaranteed.

3. **Simultaneous Proposers Test**
   - **Expected Output**: Multiple proposers attempt to start the election simultaneously, but only one succeeds in obtaining the lock. The output will show that only one proposer proceeds while others are blocked. For example:
     ```
     Running test: Multiple Proposers Simultaneously
     M1 tried to propose but M3 is already in progress.
     M2 tried to propose but M3 is already in progress.
     Received response from M1: Vote for M3
     Received response from M2: Vote for M3
     ...
     Votes for M3: 6
     M3 is elected as President.
     ```
   - **Explanation**: This test verifies the consensus mechanism by ensuring that only one proposal can proceed at a time, simulating how Paxos avoids conflicts in distributed environments.
nsuring that only one proposal can proceed at a time, simulating how Paxos avoids conflicts in distributed environments.
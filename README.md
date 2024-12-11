# Assignment 3

---

## Code Components

### 1. **Main Election System (CouncilElection.java)**
#### Responsibilities:
- **Proposers**: Initiate proposals for a council president.
- **Acceptors**: Process proposals and respond based on Paxos protocol logic.
- **Thread Pool**: Manages concurrent threads for proposers and acceptors.
- **Logging**: Logs system activity for monitoring and debugging.

#### Key Methods:
- **`runAcceptor(int memberId)`**: Starts an acceptor server for a council member, listens for proposals, and responds based on Paxos logic.
- **`runProposer(String proposerId)`**: Starts a proposer that sends prepare and accept requests to all acceptors.
- **`initializeMembers()`**: Initializes the online status of all members.
- **`addShutdownHook()`**: Ensures graceful shutdown of thread pool resources.

### 2. **Behavior Simulation (MemberBehaviorSimulator.java)**
#### Responsibilities:
- Simulates various behaviors for council members, such as:
    - Delayed responses.
    - Going offline intermittently.

#### Key Method:
- **`simulateBehavior(int memberId, boolean immediateMode)`**: Applies delays or marks members offline based on random factors.

### 3. **Network Handler (PaxosNetworkHandler.java)**
#### Responsibilities:
- Facilitates communication between proposers and acceptors.
- Handles network operations like sending and receiving requests over sockets.

#### Key Methods:
- **`sendPrepareRequest(int memberId, int proposalNumber, List<String> promises)`**: Sends prepare requests to acceptors.
- **`sendAcceptRequest(int memberId, int proposalNumber, String proposalValue)`**: Sends accept requests to acceptors.

### 4. **Testing Suite (CouncilElection_Test.java)**
#### Responsibilities:
- Validates the functionality of the election system under various scenarios.
- Logs results for analysis and debugging.

#### Key Methods:
- **`initializeSystem()`**: Ensures that all acceptors are initialized before testing begins.
- **`runAllTests()`**: Executes all predefined test cases.
- **Test Cases**:
    1. **All Members Respond Immediately**: Tests the basic functionality with no delays or offline members.
    2. **Simulating Member Delays**: Tests the system's handling of delayed responses.
    3. **Member Offline During Proposal**: Verifies fault tolerance when members go offline mid-process.
    4. **Concurrent Proposals**: Simulates simultaneous proposals to test conflict resolution.
    5. **Failure to Reach Majority**: Ensures that proposals fail correctly if a majority is not achieved.

---

## Implementation Highlights
1. **Paxos Consensus Protocol**:
    - Prepare phase: Acceptors respond to proposers with promises to not accept lower-numbered proposals.
    - Accept phase: Acceptors accept proposals if they have not already promised a higher-numbered one.
2. **Fault Tolerance**:
    - Handles member delays and offline scenarios.
    - Concurrent proposals are managed through proposal numbers.
3. **Logging**:
    - Enhanced with English timestamps for easier debugging and traceability.

---

## Testing Framework

### Objectives:
1. Validate core functionalities under ideal and adverse conditions.
2. Ensure robustness and fault tolerance.

### Test Cases:

| Test Case                       | Description                                                                                         | Expected Outcome                                                                 |
|---------------------------------|-----------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------|
| All Members Respond Immediately | Ensures that all members process proposals correctly without delays or offline states.             | Proposals succeed with majority votes.                                          |
| Simulating Member Delays        | Introduces random delays to test if proposers wait appropriately and collect votes.                | Proposals succeed as long as a majority is eventually reached.                 |
| Member Offline During Proposal  | Tests the system when members go offline after the process begins.                                 | Proposals succeed if the majority is maintained.                                |
| Concurrent Proposals            | Simulates two proposers sending proposals at the same time to test conflict resolution.            | Only the proposal with the higher number succeeds.                              |
| Failure to Reach Majority       | Tests a scenario where not enough members are online to reach a majority.                         | Proposals fail as the majority cannot be reached.                               |

---

## Execution Instructions
1. **Compile the Code**:
   ```
   javac *.java
   ```
2. **Run the Election System**:
   ```
   java CouncilElection
   ```
3. **Run the Testing Suite**:
   ```
   java CouncilElection_Test
   ```
4. **Analyze Logs**:
    - Logs are displayed on the console with timestamps and detailed information about each action.

---

## Conclusion
This assignment demonstrates a robust implementation of the Paxos protocol, capable of handling various failure scenarios in a distributed environment. The comprehensive testing framework ensures that the system behaves as expected under diverse conditions.


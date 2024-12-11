import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;

public class CouncilElection {
    // Logger for logging information and warnings
    private static final Logger logger = Logger.getLogger(CouncilElection.class.getName());
    // Thread pool for managing concurrent tasks
    static final ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(10);
    // Map to track the online status of each member
    static final Map<Integer, Boolean> memberOnlineStatus = new ConcurrentHashMap<>();

    // Base port number for member servers
    private static final int BASE_PORT = 5000;
    // Total number of members in the council
    private static final int TOTAL_MEMBERS = 9;
    // Majority required for a proposal to be accepted
    private static final int MAJORITY = TOTAL_MEMBERS / 2 + 1;

    public static void main(String[] args) {
        logger.info("Starting Council Election...");
        // Initialize the online status of all members
        initializeMembers();

        // Start acceptor threads for each member
        for (int i = 1; i <= TOTAL_MEMBERS; i++) {
            int memberId = i;
            threadPool.submit(() -> runAcceptor(memberId));
        }

        // Wait for all acceptor threads to start
        synchronized (memberOnlineStatus) {
            while (memberOnlineStatus.values().stream().filter(v -> v).count() < TOTAL_MEMBERS) {
                try {
                    memberOnlineStatus.wait();
                } catch (InterruptedException e) {
                    logger.warning("Waiting interrupted: " + e.getMessage());
                }
            }
        }

        // Schedule proposer threads to propose after some delay
        threadPool.schedule(() -> runProposer("M1"), 1, TimeUnit.SECONDS);
        threadPool.schedule(() -> runProposer("M2"), 2, TimeUnit.SECONDS);
        threadPool.schedule(() -> runProposer("M3"), 3, TimeUnit.SECONDS);

        // Add shutdown hook to cleanly terminate the thread pool
        addShutdownHook();
    }

    // Initialize the online status of all members to true
    static void initializeMembers() {
        for (int i = 1; i <= TOTAL_MEMBERS; i++) {
            memberOnlineStatus.put(i, true);
        }
    }

    // Add a shutdown hook to ensure proper termination of the thread pool
    private static void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            threadPool.shutdown();
            try {
                if (!threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                    threadPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                threadPool.shutdownNow();
            }
        }));
    }

    // Run the acceptor logic for a specific member
    public static void runAcceptor(int memberId) {
        try (ServerSocket serverSocket = new ServerSocket(BASE_PORT + memberId)) {
            logger.info("Member M" + memberId + " is ready on port " + (BASE_PORT + memberId));
            synchronized (CouncilElection.memberOnlineStatus) {
                CouncilElection.memberOnlineStatus.put(memberId, true); // Mark member as online
                CouncilElection.memberOnlineStatus.notifyAll(); // Notify other threads
            }
            while (true) {
                try (Socket socket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                     PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                    // Simulate behavior and handle messages
                    MemberBehaviorSimulator.simulateBehavior(memberId, false);

                    if (!memberOnlineStatus.get(memberId)) {
                        logger.info("Member M" + memberId + " is offline and cannot process requests.");
                        out.println("offline");
                        continue;
                    }

                    String receivedMessage = in.readLine();
                    String[] request = receivedMessage.split(":");
                    String command = request[0];
                    int proposalNumber = Integer.parseInt(request[1]);
                    String proposalValue = request.length > 2 ? request[2] : "";

                    if (command.equals("PREPARE")) {
                        out.println("PROMISE:" + proposalNumber + ":Accepted");
                    } else if (command.equals("ACCEPT")) {
                        out.println("YES");
                    }
                } catch (IOException | InterruptedException e) {
                    logger.warning("Error in Member M" + memberId + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            logger.severe("Failed to start server for Member M" + memberId + " on port " + (BASE_PORT + memberId));
        }
    }

    // Run the proposer logic for a specific proposer
    public static void runProposer(String proposerId) {
        int yesVotes = 0;
        int noVotes = 0;
        int proposalNumber = new Random().nextInt(1000);
        String proposalValue = "Council President Election by " + proposerId;

        List<String> promises = new ArrayList<>();

        // Send prepare requests to all members
        for (int i = 1; i <= TOTAL_MEMBERS; i++) {
            if (!memberOnlineStatus.get(i)) {
                logger.info("Member M" + i + " is offline and will not participate.");
                continue;
            }
            try {
                PaxosNetworkHandler.sendPrepareRequest(i, proposalNumber, promises);
            } catch (IOException e) {
                logger.warning("Failed to communicate with Member M" + i + ": " + e.getMessage());
            }
        }

        // If majority promises are received, send accept requests
        if (promises.size() >= MAJORITY) {
            logger.info("Proposal by " + proposerId + " has majority promises.");
            for (int i = 1; i <= TOTAL_MEMBERS; i++) {
                if (!memberOnlineStatus.get(i)) continue;
                try {
                    boolean accepted = PaxosNetworkHandler.sendAcceptRequest(i, proposalNumber, proposalValue);
                    if (accepted) yesVotes++;
                    else noVotes++;
                } catch (IOException e) {
                    logger.warning("Failed to communicate with Member M" + i + ": " + e.getMessage());
                }
            }
        }

        // Log the results of the proposal
        logger.info("YES votes: " + yesVotes);
        logger.info("NO votes: " + noVotes);
        if (yesVotes >= MAJORITY) {
            logger.info("Proposal by " + proposerId + " is successfully elected.");
        } else {
            logger.info("Proposal by " + proposerId + " is rejected.");
        }
    }
}

// Class to simulate the behavior of council members
class MemberBehaviorSimulator {
    // Simulate behavior based on member ID and mode
    public static void simulateBehavior(int memberId, boolean immediateMode) throws InterruptedException {
        if (!immediateMode) {
            if (memberId == 2) {
                Thread.sleep(new Random().nextInt(4000) + 1000);
            } else if (memberId == 3 && new Random().nextBoolean()) {
                CouncilElection.memberOnlineStatus.put(memberId, false);
                return;
            } else if (memberId > 3 && new Random().nextInt(10) < 1) {
                CouncilElection.memberOnlineStatus.put(memberId, false);
            }
        }
    }
}

// Class to handle network communication for Paxos protocol
class PaxosNetworkHandler {
    // Logger for logging network related information and warnings
    private static final Logger logger = Logger.getLogger(PaxosNetworkHandler.class.getName());

    // Send a prepare request to a member
    public static void sendPrepareRequest(int memberId, int proposalNumber, List<String> promises) throws IOException {
        try (Socket socket = new Socket("localhost", 5000 + memberId);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            out.println("PREPARE:" + proposalNumber);
            String response = in.readLine();
            if (response != null && response.startsWith("PROMISE")) {
                promises.add(response);
            }
        }
    }

    // Send an accept request to a member
    public static boolean sendAcceptRequest(int memberId, int proposalNumber, String proposalValue) throws IOException {
        try (Socket socket = new Socket("localhost", 5000 + memberId);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            out.println("ACCEPT:" + proposalNumber + ":" + proposalValue);
            String response = in.readLine();
            return "YES".equals(response);
        }
    }
}

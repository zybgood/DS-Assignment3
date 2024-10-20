import java.io.*;
import java.net.*;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

public class CouncilElection {
    private static final ReentrantLock lock = new ReentrantLock();
    private static String currentProposal = null; // Track current proposal to handle conflicts
    static boolean immediateResponseMode = false; // Flag to control immediate response mode

     /**
     * The main method initializes the system by setting up the immediate response mode and starting the acceptor threads for each council member.
     * It also simulates multiple proposers running simultaneously after a short delay.
     *
     * @param args Command line arguments (not used in this method)
     */
    public static void main(String[] args) {
        // Set immediate response mode to true or false
        immediateResponseMode = false; // Set to true for all members to respond immediately

        // Start acceptor threads for each council member (M1-M9)
        for (int i = 1; i <= 9; i++) {
            int memberId = i;
            new Thread(() -> runAcceptor(memberId)).start();
        }

        // Run multiple proposers after a short delay to simulate simultaneous proposals
        try {
            Thread.sleep(1000); // Wait for 1 second before starting the proposers
            new Thread(() -> runProposer("M1")).start(); // Start proposer M1
            new Thread(() -> runProposer("M2")).start(); // Start proposer M2
            new Thread(() -> runProposer("M3")).start(); // Start proposer M3
        } catch (InterruptedException e) {
            e.printStackTrace(); // Print stack trace if interrupted
        }
    }


     /**
     * Starts an acceptor for a specific member.
     * Each member listens on a different port (5000 + memberId) to receive and respond to proposals.
     * The behavior of different members varies, simulating different council members' decision-making processes.
     *
     * @param memberId The ID of the member, used to determine the port number and simulate behavior.
     */
    public static void runAcceptor(int memberId) {
        try (ServerSocket serverSocket = new ServerSocket(5000 + memberId)) {
            System.out.println("Member M" + memberId + " is ready.");
            while (true) {
                try (Socket socket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                     PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                    String proposal = in.readLine();
                    System.out.println("Member M" + memberId + " received proposal: " + proposal);

                    if (!immediateResponseMode) {
                        // Simulate behavior of different council members
                        if (memberId == 2) {
                            // M2: Delayed response
                            Thread.sleep(new Random().nextInt(4000) + 1000); // 1-5 seconds delay
                        } else if (memberId == 3) {
                            // M3: Randomly drop messages
                            if (new Random().nextBoolean()) {
                                System.out.println("Member M" + memberId + " did not respond (message dropped).");
                                continue;
                            }
                        } else if (memberId >= 4 && memberId <= 9) {
                            // M4-M9: Random delay
                            Thread.sleep(new Random().nextInt(3000)); // 0-3 seconds delay
                        }

                        // Randomly simulate going offline (for all members)
                        if (new Random().nextInt(10) < 2) {
                            System.out.println("Member M" + memberId + " went offline.");
                            out.println("offline");
                            continue;
                        }
                    }

                    // Vote for a candidate
                    String response;
                    if (memberId <= 3 && proposal.contains(memberId + "")) {
                        // M1, M2, M3 vote for themselves
                        response = "Vote for M" + memberId;
                    } else {
                        // Other members vote fairly among M1, M2, M3
                        int candidate = new Random().nextInt(3) + 1;
                        response = "Vote for M" + candidate;
                    }

                    out.println(response);
                    System.out.println("Member M" + memberId + " responded: " + response);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.err.println("Could not start server for Member M" + memberId);
            e.printStackTrace();
        }
    }

    /**
     * Initiates the proposer process for electing a council president.
     * This method simulates the process of a proposer initiating an election for council president among members.
     *
     * @param proposerId The unique identifier of the proposer, used to identify the proposer.
     */
    public static void runProposer(String proposerId) {
        // Lock to prevent multiple simultaneous proposals from succeeding
        lock.lock();
        try {
            // Initialize the proposal content
            currentProposal = "Council President Election by " + proposerId;

            // Initialize the vote counts for each candidate
            int votesForM1 = 0;
            int votesForM2 = 0;
            int votesForM3 = 0;

            // Iterate through members M1 to M9, attempting to communicate and obtain votes
            for (int i = 1; i <= 9; i++) {
                try (Socket socket = new Socket("localhost", 5000 + i);
                     PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                    // Send the proposal content to member Mi
                    out.println(currentProposal);
                    // Read the response from member Mi
                    String response = in.readLine();
                    System.out.println("Received response from M" + i + ": " + response);

                    // If member Mi does not respond or is offline, skip to the next member
                    if (response == null || response.equals("offline")) {
                        System.out.println("Member M" + i + " is offline or did not respond.");
                        continue;
                    }

                    // Count votes based on the response
                    if (response.contains("M1")) {
                        votesForM1++;
                    } else if (response.contains("M2")) {
                        votesForM2++;
                    } else if (response.contains("M3")) {
                        votesForM3++;
                    }

                } catch (IOException e) {
                    // Handle exceptions when communicating with member Mi
                    System.out.println("Error contacting M" + i + ": " + e.getMessage());
                }
            }

            // Determine the result of the election
            System.out.println("Votes for M1: " + votesForM1);
            System.out.println("Votes for M2: " + votesForM2);
            System.out.println("Votes for M3: " + votesForM3);

            // Based on the number of votes, decide if a candidate is elected as president
            if (votesForM1 > 4) {
                System.out.println("M1 is elected as President.");
            } else if (votesForM2 > 4) {
                System.out.println("M2 is elected as President.");
            } else if (votesForM3 > 4) {
                System.out.println("M3 is elected as President.");
            } else {
                System.out.println("No candidate received the majority. No one is elected as President.");
            }

        } finally {
            // Reset the proposal content and unlock after the proposal is done
            currentProposal = null;
            lock.unlock();
        }
    }
}

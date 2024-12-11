import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;

public class CouncilElection_Test {
    private static final Logger logger = Logger.getLogger(CouncilElection_Test.class.getName());

    public static void main(String[] args) {
        logger.setUseParentHandlers(false);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new SimpleFormatter() {
            @Override
            public String format(LogRecord record) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String timestamp = dateFormat.format(new Date(record.getMillis()));
                return timestamp + " " + record.getLevel() + ": " + record.getMessage() + "\n";
            }
        });
        logger.addHandler(handler);

        logger.info("Starting Tests for Council Election...");

        // Ensure system initialization
        initializeSystem();

        // Run all tests
        runAllTests();
    }

    private static void runAllTests() {
        testAllMembersRespondImmediately();
        testDelayedResponses();
        testOfflineMemberDuringProposal();
        testConcurrentProposals();
        testFailureToReachMajority();
    }

    private static void testAllMembersRespondImmediately() {
        logger.info("Test 1: All members respond immediately.");
        CouncilElection.initializeMembers();
        CouncilElection.memberOnlineStatus.replaceAll((k, v) -> true);
        CouncilElection.runProposer("M1");
    }

    private static void testDelayedResponses() {
        logger.info("Test 2: Simulating member delays.");
        CouncilElection.initializeMembers();
        CouncilElection.memberOnlineStatus.replaceAll((k, v) -> true);

        // Simulate delays for specific members
        try {
            MemberBehaviorSimulator.simulateBehavior(2, false);
            MemberBehaviorSimulator.simulateBehavior(3, false);
        } catch (InterruptedException e) {
            logger.warning("Simulation interrupted during delays test: " + e.getMessage());
        }

        CouncilElection.runProposer("M2");
    }

    private static void testOfflineMemberDuringProposal() {
        logger.info("Test 3: Member goes offline mid-proposal.");
        CouncilElection.initializeMembers();
        CouncilElection.memberOnlineStatus.put(3, false);
        CouncilElection.runProposer("M3");
    }

    private static void testConcurrentProposals() {
        logger.info("Test 4: Concurrent proposals.");
        CouncilElection.initializeMembers();
        CouncilElection.threadPool.submit(() -> CouncilElection.runProposer("Concurrent-M1"));
        CouncilElection.threadPool.submit(() -> CouncilElection.runProposer("Concurrent-M2"));
    }

    private static void testFailureToReachMajority() {
        logger.info("Test 5: Failure to reach a majority.");
        CouncilElection.initializeMembers();

        // Simulate only part of members responding
        CouncilElection.memberOnlineStatus.replaceAll((k, v) -> k <= 4); // Only first four members are online

        CouncilElection.runProposer("M4");
    }

    private static void initializeSystem() {
        CouncilElection.initializeMembers();
        for (int i = 1; i <= 9; i++) {
            int memberId = i;
            CouncilElection.threadPool.submit(() -> CouncilElection.runAcceptor(memberId));
        }

        // Wait for all servers to initialize
        synchronized (CouncilElection.memberOnlineStatus) {
            while (CouncilElection.memberOnlineStatus.values().stream().filter(v -> v).count() < 9) {
                try {
                    CouncilElection.memberOnlineStatus.wait();
                } catch (InterruptedException e) {
                    logger.warning("Initialization delay interrupted: " + e.getMessage());
                }
            }
        }
    }
}

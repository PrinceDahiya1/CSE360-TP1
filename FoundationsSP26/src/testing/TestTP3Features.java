package testing;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;
import java.util.ArrayList;
import java.sql.SQLException;

import database.Database;
import entityClasses.Post;
import prototype.RuleOfThreeVerifier;

/**
 * Title: TestTP3Features Class
 * 
 * Description: A JUnit test suite designed to verify the new Staff Epic features 
 * introduced in TP3. It uses an isolated in-memory database to generate specific 
 * interaction scenarios to validate the Rule of 3, Discussion Statistics, 
 * Instructor Moderation, and Unresolved Question analytics.
 */
public class TestTP3Features {

    private Database db;
    private RuleOfThreeVerifier verifier;

    /**
     * Method: setUp
     * Description: Initializes a fresh, isolated in-memory database connection before 
     * each test to ensure a clean slate and perfectly reproducible test data.
     */
    @BeforeEach
    public void setUp() {
        try {
            db = new Database("jdbc:h2:mem:tp3testdb;DB_CLOSE_DELAY=-1"); 
            db.connectToDatabase();
            db.dropAllPostTables(); 
            db.connectToDatabase(); 
            
            verifier = new RuleOfThreeVerifier(db);
        } catch (Exception e) {
            fail("Database setup failed: " + e.getMessage());
        }
    }

    /**
     * Method: testRuleOfThreeVerifier
     * Description: Epic 1. Tests that a student only passes if they reply to 3 DISTINCT peers.
     * Verifies that self-replies and multiple replies to the same peer do not inflate the score.
     */
    @Test
    public void testRuleOfThreeVerifier() {
        try {
            // 1. Setup Base Questions from 3 different users
            db.createPost(new Post("Q1", "Body", "Alice", "QUESTION", -1));
            db.createPost(new Post("Q2", "Body", "Bob", "QUESTION", -1));
            db.createPost(new Post("Q3", "Body", "Charlie", "QUESTION", -1));
            
            ArrayList<Post> allPosts = db.getAllPosts();
            int p1 = allPosts.get(2).getId(); // Alice's Post
            int p2 = allPosts.get(1).getId(); // Bob's Post
            int p3 = allPosts.get(0).getId(); // Charlie's Post

            // 2. Test Student 1 (Dave) - Passes (Replies to Alice, Bob, Charlie)
            db.createPost(new Post("Reply", "Body", "Dave", "STATEMENT", p1));
            db.createPost(new Post("Reply", "Body", "Dave", "STATEMENT", p2));
            db.createPost(new Post("Reply", "Body", "Dave", "STATEMENT", p3));
            assertTrue(verifier.verifyStudentInteraction("Dave"), "Dave replied to 3 distinct peers, should PASS.");

            // 3. Test Student 2 (Eve) - Fails (Replies to Alice 3 times, missing distinct peers)
            db.createPost(new Post("Reply", "Body", "Eve", "STATEMENT", p1));
            db.createPost(new Post("Reply", "Body", "Eve", "STATEMENT", p1));
            db.createPost(new Post("Reply", "Body", "Eve", "STATEMENT", p1));
            assertFalse(verifier.verifyStudentInteraction("Eve"), "Eve replied to 1 peer 3 times, should FAIL.");

            // 4. Test Student 3 (Frank) - Fails (Self-replies should be ignored)
            db.createPost(new Post("Q4", "Body", "Frank", "QUESTION", -1));
            int p4 = db.getAllPosts().get(0).getId(); // Frank's new post
            db.createPost(new Post("Reply", "Body", "Frank", "STATEMENT", p4)); // self reply
            db.createPost(new Post("Reply", "Body", "Frank", "STATEMENT", p1)); // replies to Alice
            db.createPost(new Post("Reply", "Body", "Frank", "STATEMENT", p2)); // replies to Bob
            assertFalse(verifier.verifyStudentInteraction("Frank"), "Frank has 2 peer replies and 1 self-reply, should FAIL.");

        } catch (Exception e) {
            fail("Rule of 3 test failed due to exception: " + e.getMessage());
        }
    }

    /**
     * Method: testStaffMetadata
     * Description: Epic 3 and 6. Verifies that Instructor Endorsements and Staff Comments 
     * are correctly saved and retrieved from the database.
     */
    @Test
    public void testStaffMetadata() {
        try {
            db.createPost(new Post("Test Title", "Test Body", "Alice", "STATEMENT", -1));
            int postId = db.getAllPosts().get(0).getId();

            // Test Staff Comment Update
            db.updateStaffComment(postId, "Great insight.");
            Post updatedPost = db.getPostById(postId);
            assertEquals("Great insight.", updatedPost.getStaffComment(), "Staff comment was not saved correctly.");

            // Test Instructor Endorsement Toggle
            db.updateInstructorEndorsement(postId, true);
            assertTrue(db.getPostById(postId).isInstructorEndorsed(), "Instructor endorsement should be TRUE.");

            db.updateInstructorEndorsement(postId, false);
            assertFalse(db.getPostById(postId).isInstructorEndorsed(), "Instructor endorsement should be FALSE.");

        } catch (Exception e) {
            fail("Metadata test failed due to exception: " + e.getMessage());
        }
    }

    /**
     * Method: testDiscussionStatistics
     * Description: Epic 4. Verifies the database accurately counts total Questions and Statements.
     */
    @Test
    public void testDiscussionStatistics() {
        try {
            // Create 3 Questions and 1 Statement
            db.createPost(new Post("Q1", "Body", "Alice", "QUESTION", -1));
            db.createPost(new Post("Q2", "Body", "Bob", "QUESTION", -1));
            db.createPost(new Post("Q3", "Body", "Charlie", "QUESTION", -1));
            db.createPost(new Post("S1", "Body", "Dave", "STATEMENT", -1));

            int[] stats = db.getDiscussionStatistics();
            assertEquals(3, stats[0], "Question count should be 3.");
            assertEquals(1, stats[1], "Statement count should be 1.");

            // Verify Peak Activity Time isn't crashing
            String peakTime = db.getPeakActivityTime();
            assertNotNull(peakTime);

        } catch (Exception e) {
            fail("Statistics test failed due to exception: " + e.getMessage());
        }
    }

    /**
     * Method: testUnresolvedQuestions
     * Description: Epic 8. Verifies that ONLY active QUESTION type posts with 0 replies 
     * are returned by the unresolved analytics query.
     */
    @Test
    public void testUnresolvedQuestions() {
        try {
            // 1. A Question with NO replies (Should be returned)
            db.createPost(new Post("Unresolved Q", "Body", "Alice", "QUESTION", -1));
            
            // 2. A Question WITH replies (Should NOT be returned)
            db.createPost(new Post("Resolved Q", "Body", "Bob", "QUESTION", -1));
            int resolvedQId = db.getAllPosts().get(0).getId();
            db.createPost(new Post("Reply to Bob", "Body", "Charlie", "STATEMENT", resolvedQId));

            // 3. A Statement (Should NOT be returned, even if it has no replies)
            db.createPost(new Post("Just a Statement", "Body", "Dave", "STATEMENT", -1));

            ArrayList<Post> unresolved = db.getUnresolvedQuestions();
            
            assertEquals(1, unresolved.size(), "Query should return exactly 1 unresolved question.");
            assertEquals("Unresolved Q", unresolved.get(0).getTitle(), "The returned post should be Alice's unresolved question.");

        } catch (Exception e) {
            fail("Unresolved questions test failed due to exception: " + e.getMessage());
        }
    }
}
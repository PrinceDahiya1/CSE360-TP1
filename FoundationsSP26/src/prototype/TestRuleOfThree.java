package prototype;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import database.Database;
import entityClasses.Post;

/**
 * Title: TestRuleOfThree
 * Description: A JUnit 5 test suite to verify the logic of the RuleOfThreeVerifier prototype.
 * @author Prince Dahiya
 */
public class TestRuleOfThree {

    private Database db;
    private RuleOfThreeVerifier verifier;

    @BeforeEach
    public void setUp() {
        try {
            db = new Database("jdbc:h2:mem:tp3testdb;DB_CLOSE_DELAY=-1"); 
            db.connectToDatabase();
            db.dropAllPostTables();
            db.connectToDatabase();
            verifier = new RuleOfThreeVerifier(db);
        } catch (Exception e) {
            fail("Setup failed: " + e.getMessage());
        }
    }

    /**
     * Method: testExactThresholdAndSelfReply
     * Description: Tests if the verifier correctly counts distinct peers and 
     * successfully ignores self-replies.
     */
    @Test
    public void testExactThresholdAndSelfReply() {
        try {
            // Setup: Create 3 distinct parent posts from different peers
            db.createPost(new Post("Q1", "Body", "Peer1", "QUESTION", -1));
            db.createPost(new Post("Q2", "Body", "Peer2", "QUESTION", -1));
            db.createPost(new Post("Q3", "Body", "Peer3", "QUESTION", -1));
            
            // Setup: Create a post by the target student
            db.createPost(new Post("Q4", "My own question", "TargetStudent", "QUESTION", -1));

            // TargetStudent replies to Peer1, Peer2, and Peer3 (3 unique peers)
            db.createPost(new Post("Reply1", "Ans", "TargetStudent", "REPLY", 1));
            db.createPost(new Post("Reply2", "Ans", "TargetStudent", "REPLY", 2));
            db.createPost(new Post("Reply3", "Ans", "TargetStudent", "REPLY", 3));
            
            // TargetStudent replies to Peer1 AGAIN (Should not increase distinct count)
            db.createPost(new Post("Reply4", "Ans2", "TargetStudent", "REPLY", 1));
            
            // TargetStudent replies to their OWN post (Should be ignored)
            db.createPost(new Post("Reply5", "Self bump", "TargetStudent", "REPLY", 4));

            // Execution & Verification
            // Internal Comment: Despite having 5 total replies, they only interacted with 
            // exactly 3 distinct peers. The system must evaluate this as TRUE.
            boolean result = verifier.verifyStudentInteraction("TargetStudent");
            assertTrue(result, "Verifier failed to identify that the student interacted with 3 distinct peers.");
            
        } catch (Exception e) {
            fail("Test crashed: " + e.getMessage());
        }
    }
}
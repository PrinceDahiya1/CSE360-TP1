package prototype;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import database.Database;

/**
 * Title: RuleOfThreeVerifier
 * Description: A prototype class designed to evaluate if a student has met the 
 * Phase 3 participation requirement of interacting with at least three distinct peers.
 * @author Prince Dahiya
 */
public class RuleOfThreeVerifier {
    
    private Database db;

    /**
     * Constructor for the verifier.
     * @param db The active database connection used for querying posts.
     */
    public RuleOfThreeVerifier(Database db) {
        this.db = db;
    }

    /**
     * Method: verifyStudentInteraction
     * Description: Calculates how many unique students the target user has replied to.
     * It specifically filters out replies made to the user's own posts.
     * * @param targetUsername The username of the student being graded.
     * @return boolean True if they replied to >= 3 distinct peers, False otherwise.
     * @throws SQLException If the database query fails.
     */
    public boolean verifyStudentInteraction(String targetUsername) throws SQLException {
        // We use a raw SQL query here rather than pulling all ArrayList<Post> 
        // objects into Java memory. Why? Because pulling thousands of posts into memory just 
        // to count them would cause massive performance bottlenecks during grading. 
        // Database-level COUNT(DISTINCT) is exponentially faster.
        
        String sql = "SELECT COUNT(DISTINCT parent.authorUsername) AS uniquePeers " +
                     "FROM postDB AS reply " +
                     "JOIN postDB AS parent ON reply.parentPostId = parent.id " +
                     "WHERE reply.authorUsername = ? " +
                     "AND parent.authorUsername != ?";
                     
        Connection conn = db.getConnection(); 
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Internal Comment: We bind the targetUsername twice. First to find replies 
            // authored by the student, and second to ensure the parent author is NOT the student,
            // which cleanly satisfies the Req-Eval-3 rule against self-replies.
            pstmt.setString(1, targetUsername);
            pstmt.setString(2, targetUsername);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int distinctPeers = rs.getInt("uniquePeers");
                    // Internal Comment: The threshold is strictly set to 3 per the HW3 rubric.
                    return distinctPeers >= 3;
                }
            }
        }
        return false;
    }
}
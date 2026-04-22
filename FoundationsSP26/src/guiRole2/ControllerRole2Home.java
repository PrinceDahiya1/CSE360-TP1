package guiRole2;

import database.Database;
import prototype.RuleOfThreeVerifier;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/*******
 * <p> Title: ControllerRole2Home Class. </p>
 * 
 * <p> Description: The Java/FX-based Role 2 Home Page.  This class provides the controller
 * actions basic on the user's use of the JavaFX GUI widgets defined by the View class.
 * 
 * This page is a stub for establish future roles for the application.
 * 
 * The class has been written assuming that the View or the Model are the only class methods that
 * can invoke these methods.  This is why each has been declared at "protected".  Do not change any
 * of these methods to public.</p>
 * 
 * <p> Copyright: Lynn Robert Carter © 2025 </p>
 * 
 * @author Lynn Robert Carter
 * 
 * @version 1.00		2025-08-17 Initial version
 * @version 1.01		2025-09-16 Update Javadoc documentation *  
 */

public class ControllerRole2Home {
	
	/*-*******************************************************************************************

	User Interface Actions for this page
	
	This controller is not a class that gets instantiated.  Rather, it is a collection of protected
	static methods that can be called by the View (which is a singleton instantiated object) and 
	the Model is often just a stub, or will be a singleton instantiated object.
	
	 */
	
	private static Database db;

    public static void setDatabase(Database database) {
        db = database;
    }

	/**
	 * Default constructor is not used.
	 */
	public ControllerRole2Home() {
	}

	/**********
	 * <p> Method: performUpdate() </p>
	 * 
	 * <p> Description: This method directs the user to the User Update Page so the user can change
	 * the user account attributes. </p>
	 * 
	 */
	protected static void performUpdate () {
		guiUserUpdate.ViewUserUpdate.displayUserUpdate(ViewRole2Home.theStage, ViewRole2Home.theUser);
	}	

	/**********
	 * <p> Method: performLogout() </p>
	 * 
	 * <p> Description: This method logs out the current user and proceeds to the normal login
	 * page where existing users can log in or potential new users with a invitation code can
	 * start the process of setting up an account. </p>
	 * 
	 */
	protected static void performLogout() {
		guiUserLogin.ViewUserLogin.displayUserLogin(ViewRole2Home.theStage);
	}
	
	/**********
	 * <p> Method: performQuit() </p>
	 * 
	 * <p> Description: This method terminates the execution of the program.  It leaves the
	 * database in a state where the normal login page will be displayed when the application is
	 * restarted.</p>
	 * 
	 */	
	protected static void performQuit() {
		System.exit(0);
	}
	
	// ==================================================================================
    // TP3 STAFF GRADING DASHBOARD METHODS
    // ==================================================================================

	public static void handleEvaluateStudent(String targetUsername, Label resultLabel) {
        if (db == null) {
            resultLabel.setText("System Error: Database not connected.");
            return;
        }
        try {
            RuleOfThreeVerifier verifier = new RuleOfThreeVerifier(db);
            boolean passed = verifier.verifyStudentInteraction(targetUsername);
            
            if (passed) {
                resultLabel.setText(targetUsername + ": PASSED (3+ peers)");
                resultLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
            } else {
                resultLabel.setText(targetUsername + ": FAILED (< 3 peers)");
                resultLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            }
        } catch (SQLException e) {
            resultLabel.setText("Error: Could not evaluate student.");
            resultLabel.setStyle("-fx-text-fill: orange;");
        }
    }

	public static void handleSaveStaffComment(int postId, String comment, Label statusLabel, ListView<String> postListView) {
        if (db == null || comment == null || comment.trim().isEmpty()) {
            statusLabel.setText("Invalid comment or DB error.");
            return;
        }
        db.updateStaffComment(postId, comment);
        statusLabel.setText("Comment saved successfully.");
        statusLabel.setStyle("-fx-text-fill: green;");
        refreshPostList(postListView);
    }

    public static void handleToggleEndorsement(int postId, boolean isEndorsed, ListView<String> postListView) {
        if (db == null) return;
        db.updateInstructorEndorsement(postId, isEndorsed);
        refreshPostList(postListView);
    }
    
    /*******
     * <p> Method: handleDeletePost </p>
     * <p> Description: Triggers the DB deletion and refreshes the dashboard UI. </p>
     */
    public static void handleDeletePost(int postId, Label statusLabel, ListView<String> postListView) {
        if (db == null) {
            statusLabel.setText("Error: Database not connected.");
            return;
        }
        
        db.deletePost(postId);
        statusLabel.setText("Post " + postId + " forcefully deleted.");
        statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        refreshPostList(postListView);
    }

    public static void refreshPostList(ListView<String> postListView) {
        if (db == null || postListView == null) return;
        
        postListView.getItems().clear();
        String query = "SELECT * FROM postDB ORDER BY id ASC";
        
        try (PreparedStatement pstmt = db.getConnection().prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
             
            while (rs.next()) {
                int id = rs.getInt("id");
                String author = rs.getString("authorUsername");
                String body = rs.getString("body");
                boolean endorsed = rs.getBoolean("isInstructorEndorsed");
                String staffComment = rs.getString("staffComment");
                
                String displayStr = "ID: " + id + " | " + author + ": " + body;
                if (endorsed) displayStr += "\n[★ INSTRUCTOR ENDORSED]";
                if (staffComment != null && !staffComment.trim().isEmpty()) displayStr += "\n[Staff Note: " + staffComment + "]";
                
                postListView.getItems().add(displayStr);
            }
        } catch (SQLException e) {
            postListView.getItems().add("System Error loading posts.");
        }
    }
}

package guiRole2;

import database.Database;
import prototype.RuleOfThreeVerifier;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import java.sql.SQLException;
import entityClasses.Post;
import java.util.ArrayList;

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
    
    private static Label dashboardStatsLabel;
    
    /*******
     * <p> Method: refreshStatistics </p>
     * <p> Description: Fetches the Question/Statement counts and Peak Activity Time. </p>
     */
    public static void refreshStatistics(Label statsLabel) {
        dashboardStatsLabel = statsLabel;
        if (db == null || dashboardStatsLabel == null) return;
        
        int[] stats = db.getDiscussionStatistics();
        String peakTime = db.getPeakActivityTime();
        
        dashboardStatsLabel.setText("📊 Board Stats | Questions: " + stats[0] + " | Statements: " + stats[1] + " | Peak Activity: " + peakTime);
        dashboardStatsLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-font-size: 14px;");
    }

    /*******
     * <p> Method: refreshPostList (Standard) </p>
     * <p> Description: Repopulates the board with all posts. </p>
     */
    public static void refreshPostList(ListView<String> postListView) {
        // Call the overloaded method with an empty search string
        refreshPostList(postListView, "");
    }

    /*******
     * <p> Method: refreshPostList (Overloaded for Epic 5 Search) </p>
     * <p> Description: Repopulates the board, applying an optional username filter. </p>
     */
    public static void refreshPostList(ListView<String> postListView, String searchUsername) {
        if (db == null || postListView == null) return;
        
        // Auto-refresh stats if the label is registered
        if (dashboardStatsLabel != null) {
            refreshStatistics(dashboardStatsLabel);
        }
        
        postListView.getItems().clear();
        
        java.util.ArrayList<entityClasses.Post> posts = db.getContextualThreadedPosts();
        boolean isSearching = searchUsername != null && !searchUsername.trim().isEmpty();
        
        for (entityClasses.Post p : posts) {
            // EPIC 5 FILTER: If we are searching, skip posts that don't match the username
            if (isSearching && !p.getAuthorUsername().toLowerCase().contains(searchUsername.trim().toLowerCase())) {
                continue; 
            }
            
            // If we are searching, flatten the indent so the results align neatly. 
            // If not searching, keep the Epic 2 contextual thread indent!
            String indent = (!isSearching && p.getParentPostId() != -1) ? "    ↳ " : "";
            
            String displayStr = indent + "ID: " + p.getId() + " | " + p.getAuthorUsername() + ": " + p.getBody();
            
            if (p.isInstructorEndorsed()) {
                displayStr += "\n" + indent + "[★ INSTRUCTOR ENDORSED]";
            }
            if (p.getStaffComment() != null && !p.getStaffComment().trim().isEmpty()) {
                displayStr += "\n" + indent + "[Staff Note: " + p.getStaffComment() + "]";
            }
            
            postListView.getItems().add(displayStr);
        }
    }
    
    /*******
     * <p> Method: handleGenerateReport </p>
     * <p> Description: Generates a CSV report of all students and their Rule of 3 status (Epic 7)
     * using a FileChooser for the save location. </p>
     */
    public static void handleGenerateReport(Label statusLabel, javafx.stage.Stage stage) {
        if (db == null) {
            statusLabel.setText("Error: Database not connected.");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        try {
            // Setup the FileChooser
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Save Student Performance Report");
            fileChooser.setInitialFileName("StudentPerformanceReport.csv");
            fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            
            // Open the save dialog
            java.io.File file = fileChooser.showSaveDialog(stage);
            
            // Check if the user clicked "Cancel"
            if (file == null) {
                statusLabel.setText("Export cancelled.");
                statusLabel.setStyle("-fx-text-fill: orange;");
                return;
            }

            prototype.RuleOfThreeVerifier verifier = new prototype.RuleOfThreeVerifier(db);
            java.util.ArrayList<entityClasses.User> allUsers = db.getAllUsers();
            
            java.io.PrintWriter writer = new java.io.PrintWriter(file);
            writer.println("Username,First Name,Last Name,Rule of 3 Status");

            int studentCount = 0;
            for (entityClasses.User u : allUsers) {
                if (u.getNewRole1()) {
                    boolean passed = verifier.verifyStudentInteraction(u.getUserName());
                    String status = passed ? "PASSED" : "FAILED";
                    
                    writer.println(u.getUserName() + "," + u.getFirstName() + "," + u.getLastName() + "," + status);
                    studentCount++;
                }
            }
            
            writer.close();
            
            statusLabel.setText("Success! " + studentCount + " students exported.");
            statusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error generating report.");
            statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        }
    }
    
    /*******
     * <p> Method: refreshUnresolvedQuestions </p>
     * <p> Description: Populates the board with only Unresolved Questions (Epic 8). </p>
     */
    public static void refreshUnresolvedQuestions(ListView<String> postListView) {
        if (db == null || postListView == null) return;
        
        if (dashboardStatsLabel != null) {
            refreshStatistics(dashboardStatsLabel);
        }
        
        postListView.getItems().clear();
        
        java.util.ArrayList<entityClasses.Post> posts = db.getUnresolvedQuestions();
        
        for (entityClasses.Post p : posts) {
            String displayStr = "ID: " + p.getId() + " | " + p.getAuthorUsername() + ": " + p.getBody();
            
            if (p.isInstructorEndorsed()) {
                displayStr += "\n[★ INSTRUCTOR ENDORSED]";
            }
            if (p.getStaffComment() != null && !p.getStaffComment().trim().isEmpty()) {
                displayStr += "\n[Staff Note: " + p.getStaffComment() + "]";
            }
            
            postListView.getItems().add(displayStr);
        }
    }
}

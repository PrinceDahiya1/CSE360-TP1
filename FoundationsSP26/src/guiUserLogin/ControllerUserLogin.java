package guiUserLogin;

import database.Database;
import entityClasses.User;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/*******
 * <p> Title: ControllerUserLogin Class. </p>
 * 
 * <p> Description: The Java/FX-based User Login Page.  This class provides the controller
 * actions basic on the user's use of the JavaFX GUI widgets defined by the View class.
 * 
 * This controller determines if the log in is valid.  If so set up the link to the database, 
 * determines how many roles this user is authorized to play, and the calls one the of the array of
 * role home pages if there is only one role.  If there are more than one role, it setup up and
 * calls the multiple roles dispatch page for the user to determine which role the user wants to
 * play.
 * 
 * The class has been written assuming that the View or the Model are the only class methods that
 * can invoke these methods.  This is why each has been declared at "protected".  Do not change any
 * of these methods to public.</p>
 * 
 * <p> Copyright: Lynn Robert Carter © 2025 </p>
 * 
 * @author Lynn Robert Carter
 * @author Prince Dahiya
 * 
 * @version 1.00		2025-08-17 Initial version
 * @version 1.01		2025-09-16 Update Javadoc documentation *  
 * @version 1.02		2026-02-08 Fixed logic after changes in constructor for OTP feature
 * @version 1.03		2026-02-09 The User is now forced to change password after use of OTP
 */

public class ControllerUserLogin {
	
	/*-********************************************************************************************

	The User Interface Actions for this page
	
	This controller is not a class that gets instantiated.  Rather, it is a collection of protected
	static methods that can be called by the View (which is a singleton instantiated object) and 
	the Model is often just a stub, or will be a singleton instantiated object.
	
	*/

	/**
	 * Default constructor is not used.
	 */
	public ControllerUserLogin() {
	}

	// Reference for the in-memory database so this package has access
	private static Database theDatabase = applicationMain.FoundationsMain.database;

	private static Stage theStage;	
	
	/**********
	 * <p> Method: public doLogin() </p>
	 * 
	 * <p> Description: This method is called when the user has clicked on the Login button. This
	 * method checks the username and password to see if they are valid.  If so, it then logs that
	 * user in my determining which role to use.
	 * 
	 * The method reaches batch to the view page and to fetch the information needed rather than
	 * passing that information as parameters.
	 * 
	 */	
	protected static void doLogin(Stage ts) {
		theStage = ts;
		String username = ViewUserLogin.text_Username.getText();
		String password = ViewUserLogin.text_Password.getText();
    	boolean loginResult = false;
    	
    	// --- CHECK 1: Input Validation Framework (Task 4) ---
    	// Added this block to prevent crashes from massive inputs
    	String valMsg = entityClasses.InputUtils.validateInput(username, 50, "Username");
    	if (!valMsg.isEmpty()) {
    		ViewUserLogin.alertUsernamePasswordError.setContentText(valMsg);
    		ViewUserLogin.alertUsernamePasswordError.showAndWait();
    		return;
    	}
    	
    	valMsg = entityClasses.InputUtils.validateInput(password, 50, "Password");
    	if (!valMsg.isEmpty()) {
    		ViewUserLogin.alertUsernamePasswordError.setContentText(valMsg);
    		ViewUserLogin.alertUsernamePasswordError.showAndWait();
    		return;
    	}
    	// ----------------------------------------------------
    	
		// Fetch the user and verify the username
     	if (theDatabase.getUserAccountDetails(username) == false) {
     		// Don't provide too much information.
    		ViewUserLogin.alertUsernamePasswordError.setContentText(
    				"Incorrect username/password. Try again!");
    		ViewUserLogin.alertUsernamePasswordError.showAndWait();
    		return;
    	}
		
		// Check to see that the login password matches the account password
    	String actualPassword = theDatabase.getCurrentPassword();
    	
    	if (password.compareTo(actualPassword) != 0) {
    		ViewUserLogin.alertUsernamePasswordError.setContentText(
    				"Incorrect username/password. Try again!");
    		ViewUserLogin.alertUsernamePasswordError.showAndWait();
    		return;
    	}
		
		// Establish this user's details
    	entityClasses.User user = null;
    	for (entityClasses.User u : theDatabase.getAllUsers()) {
    		if (u.getUserName().equals(username)) {
    			user = u;
    			break;
    		}
    	}
    	
    	if (user == null) return;
    	
    	// --- OTP SECURITY CHECK ---
    	if (user.getHasOTP()) {
    		System.out.println("OTP Detected for user: " + username);
    		
    		Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Password Reset Required");
            alert.setHeaderText("Temporary Password Detected");
            alert.setContentText("You are using a One-Time Password. You must update your password now.");
            alert.showAndWait();
            
            // Redirect to User Update Page immediately
            guiUserUpdate.ViewUserUpdate.displayUserUpdate(theStage, user);
            return; 
    	}
    	
    	// See which home page dispatch to use
		int numberOfRoles = theDatabase.getNumberOfRoles(user);		
		
		if (numberOfRoles == 1) {
			// Single Account Home Page
			if (user.getAdminRole()) {
				if (theDatabase.loginAdmin(user)) 
					guiAdminHome.ViewAdminHome.displayAdminHome(theStage, user);
			} else if (user.getNewRole1()) {
				if (theDatabase.loginRole1(user)) 
					guiRole1.ViewRole1Home.displayRole1Home(theStage, user);
			} else if (user.getNewRole2()) {
				if (theDatabase.loginRole2(user)) 
					guiRole2.ViewRole2Home.displayRole2Home(theStage, user);
			} else {
				System.out.println("***** UserLogin goToUserHome request has an invalid role");
			}
		} else if (numberOfRoles > 1) {
			// Multiple Account Home Page
			guiMultipleRoleDispatch.ViewMultipleRoleDispatch.
				displayMultipleRoleDispatch(theStage, user);
		}
	}
	
		
	/**********
	 * <p> Method: setup() </p>
	 * 
	 * <p> Description: This method is called to reset the page and then populate it with new
	 * content for the new user.</p>
	 * 
	 */
	protected static void doSetupAccount(Stage theStage, String invitationCode) {
		guiNewAccount.ViewNewAccount.displayNewAccount(theStage, invitationCode);
	}

	
	/**********
	 * <p> Method: public performQuit() </p>
	 * 
	 * <p> Description: This method is called when the user has clicked on the Quit button.  Doing
	 * this terminates the execution of the application.  All important data must be stored in the
	 * database, so there is no cleanup required.  (This is important so we can minimize the impact
	 * of crashed.)
	 * 
	 */	
	protected static void performQuit() {
		System.out.println("Perform Quit");
		System.exit(0);
	}	

}

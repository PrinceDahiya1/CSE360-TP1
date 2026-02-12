package guiNewAccount;

import java.sql.SQLException;
import database.Database;
import entityClasses.User;
import userNameRecognizerTestbed.UserNameRecognizer;

/*******
 * <p> Title: ControllerNewAccount Class. </p>
 * 
 * <p> Description: The Java/FX-based New Account Page.  This class provides the controller actions
 * to allow the user to establish a new account after responding to an invitation and the use of a
 * one time code.
 * 
 * The controller deals with the user pressing the "User Step" button widget being click.  If also
 * supports the user click on the "Quit" button widget.
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
 * @version 1.01		2026-01-26 Added UserNameRecognizer FSM logic for Username validation
 * @version 1.02		2026-02-08 Fixed logic after changes in constructor for OTP feature
 *  
 */

public class ControllerNewAccount {
	
	/*-********************************************************************************************

	The User Interface Actions for this page
	
	This controller is not a class that gets instantiated.  Rather, it is a collection of protected
	static methods that can be called by the View (which is a singleton instantiated object) and 
	the Model is often just a stub, or will be a singleton instantiated object.
	
	*/

	/**
	 * Default constructor is not used.
	 */
	public ControllerNewAccount() {
	}
	
	
	// Reference for the in-memory database so this package has access
	private static Database theDatabase = applicationMain.FoundationsMain.database;
	
	/**********
	 * <p> Method: public doCreateUser() </p>
	 * 
	 * <p> Description: This method is called when the user has clicked on the User Setup
	 * button.  This method checks the input fields to see that they are valid.  If so, it then
	 * creates the account by adding information to the database.
	 * 
	 * The method reaches batch to the view page and to fetch the information needed rather than
	 * passing that information as parameters.
	 * 
	 */	
	protected static void doCreateUser() {
        // 1. Fetch Inputs
        String username = ViewNewAccount.text_Username.getText();
        String password = ViewNewAccount.text_Password1.getText();
        String passwordRepeat = ViewNewAccount.text_Password2.getText();
        
        // --- CHECK 1: Length Validation (Prevents Crashes) ---
        String valMsg = entityClasses.InputUtils.validateInput(username, 50, "Username");
        if (!valMsg.isEmpty()) { showAlert(valMsg); return; }
        
        valMsg = entityClasses.InputUtils.validateInput(password, 50, "Password");
        if (!valMsg.isEmpty()) { showAlert(valMsg); return; }

        // --- CHECK 2: USERNAME RECOGNIZER (Only for New Accounts!) ---
        String uNameError = userNameRecognizerTestbed.UserNameRecognizer.checkForValidUserName(username);
        if (uNameError.length() > 0) {
            showAlert("Invalid Username: " + uNameError);
            return;
        }

        // --- CHECK 3: PASSWORD FSM RECOGNIZER (Using your Model.java logic) ---
        String pwdError = passwordRecognizer.PasswordRecognizer.evaluatePassword(password);
        if (!pwdError.isEmpty()) {
            showAlert("Weak Password:\n" + pwdError);
            return;
        }
        
        // --- CHECK 4: Do Passwords Match? ---
        if (!password.equals(passwordRepeat)) {
            showAlert("Passwords do not match.");
            ViewNewAccount.text_Password1.setText("");
            ViewNewAccount.text_Password2.setText("");
            return;
        }

        // --- SUCCESS: Create the User ---
        // (This part remains the same as your original code)
        System.out.println("** Creating Account for: " + username);
        
        int roleCode = 0;
        entityClasses.User user = null;

        if (ViewNewAccount.theRole.compareTo("Admin") == 0) {
            roleCode = 1;
            user = new entityClasses.User(username, password, "", "", "", "", "", true, false, false, false);
        } else if (ViewNewAccount.theRole.compareTo("Role1") == 0) {
            roleCode = 2;
            user = new entityClasses.User(username, password, "", "", "", "", "", false, true, false, false);
        } else if (ViewNewAccount.theRole.compareTo("Role2") == 0) {
            roleCode = 3;
            user = new entityClasses.User(username, password, "", "", "", "", "", false, false, true, false);
        } else {
            System.out.println("**** Error: Unknown Role");
            System.exit(0);
        }
        
        user.setEmailAddress(ViewNewAccount.emailAddress);
        applicationMain.FoundationsMain.activeHomePage = roleCode;
        
        try {
        	applicationMain.FoundationsMain.database.register(user);
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
            return;
        }
        
        // Cleanup
        applicationMain.FoundationsMain.database.removeInvitationAfterUse(ViewNewAccount.text_Invitation.getText());
        applicationMain.FoundationsMain.database.getUserAccountDetails(username);
        guiUserUpdate.ViewUserUpdate.displayUserUpdate(ViewNewAccount.theStage, user);
    }

    // Helper to keep code clean
    private static void showAlert(String content) {
        ViewNewAccount.alertUserNameError.setTitle("Validation Error");
        ViewNewAccount.alertUserNameError.setHeaderText("Input Error");
        ViewNewAccount.alertUserNameError.setContentText(content);
        ViewNewAccount.alertUserNameError.showAndWait();
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

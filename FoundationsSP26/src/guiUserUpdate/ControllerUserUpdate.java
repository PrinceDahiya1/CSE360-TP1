package guiUserUpdate;

import entityClasses.User;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class ControllerUserUpdate {
	/*-********************************************************************************************

	The Controller for ViewUserUpdate 
	
	**********************************************************************************************/

	/**********
	 * <p> Title: ControllerUserUpdate Class</p>
	 * 
	 * <p> Description: This static class supports the actions initiated by the ViewUserUpdate
	 * class. In this case, there is just one method, no constructors, and no attributes.</p>
	 *
	 */

	/*-********************************************************************************************

	The User Interface Actions for this page
	
	**********************************************************************************************/
	
	
	private static database.Database theDatabase = applicationMain.FoundationsMain.database;
	
	/**********
	 * <p> Method: public goToUserHomePage(Stage theStage, User theUser) </p>
	 * 
	 * <p> Description: This method is called when the user has clicked on the button to
	 * proceed to the user's home page.
	 * 
	 * @param theStage specifies the JavaFX Stage for next next GUI page and it's methods
	 * 
	 * @param theUser specifies the user so we go to the right page and so the right information
	 */	
	protected static void goToUserHomePage(Stage theStage, User theUser) {
		
		// SECURITY FIX: Prevent bypassing the OTP reset
		if (theUser.getHasOTP()) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Password Reset Required");
			alert.setHeaderText("Action Blocked");
			alert.setContentText("You must successfully update your password before proceeding to the home page.");
			alert.showAndWait();
			return; // Stops them from leaving the page
		}

		// ROUTING FIX: Determine the correct page based on THIS user's roles
		int numberOfRoles = theDatabase.getNumberOfRoles(theUser);
		
		if (numberOfRoles > 1) {
			// If they have multiple roles, send them to the dispatcher
			guiMultipleRoleDispatch.ViewMultipleRoleDispatch.displayMultipleRoleDispatch(theStage, theUser);
		} else if (numberOfRoles == 1) {
			// Send them to their specific single role page
			if (theUser.getAdminRole()) {
				guiAdminHome.ViewAdminHome.displayAdminHome(theStage, theUser);
			} else if (theUser.getNewRole1()) {
				guiRole1.ViewRole1Home.displayRole1Home(theStage, theUser);
			} else if (theUser.getNewRole2()) {
				guiRole2.ViewRole2Home.displayRole2Home(theStage, theUser);
			}
		} else {
			System.out.println("*** ERROR *** User has no valid roles assigned.");
		}
 	}
	
	/**********
     * Method: attemptUpdatePassword
     * Validates and updates the user's password.
     */
    protected static boolean attemptUpdatePassword(User user, String newPassword) {
        // 1. Length Check
        String valMsg = entityClasses.InputUtils.validateInput(newPassword, 50, "Password");
        if (!valMsg.isEmpty()) {
            showAlert("Invalid Password", valMsg);
            return false;
        }

        // 2. Complexity Check (FSM)
        String pwdError = passwordRecognizer.PasswordRecognizer.evaluatePassword(newPassword);
        if (!pwdError.isEmpty()) {
            showAlert("Weak Password", pwdError);
            return false;
        }

        // 3. Update Database
        theDatabase.updatePassword(user.getUserName(), newPassword);
        user.setPassword(newPassword);
        user.setHasOTP(false); 
        
        showSuccess("Password Updated", "Your password has been changed securely.");
        return true;
    }
    
    /**********
     * Method: attemptUpdateEmail
     * Validates and updates the user's email address.
     */
    protected static boolean attemptUpdateEmail(User user, String newEmail) {
        // 1. Length Check
        String valMsg = entityClasses.InputUtils.validateInput(newEmail, 100, "Email Address");
        if (!valMsg.isEmpty()) {
            showAlert("Invalid Email", valMsg);
            return false;
        }

        // 2. Format Check (Using your Email FSM)
        String emailError = emailAddressRecognizer.EmailAddressRecognizer.checkForValidEmailAddress(newEmail);
        if (!emailError.isEmpty()) {
            showAlert("Invalid Email", "The email address is invalid: " + emailError);
            return false;
        }

        // 3. Update Database
        theDatabase.updateEmailAddress(user.getUserName(), newEmail);
        user.setEmailAddress(newEmail);
        
        return true; // Success
    }
    
    /**********
     * Method: attemptUpdateName
     * Validates and updates First, Middle, Last, or Preferred Name.
     */
    protected static boolean attemptUpdateName(User user, String newName, String nameType) {
        // 1. Length Check
        String valMsg = entityClasses.InputUtils.validateInput(newName, 50, nameType);
        if (!valMsg.isEmpty()) {
            showAlert("Invalid Input", valMsg);
            return false;
        }

        // 2. Update Database based on type
        switch (nameType) {
            case "First Name":
                theDatabase.updateFirstName(user.getUserName(), newName);
                user.setFirstName(newName);
                break;
            case "Middle Name":
                theDatabase.updateMiddleName(user.getUserName(), newName);
                user.setMiddleName(newName);
                break;
            case "Last Name":
                theDatabase.updateLastName(user.getUserName(), newName);
                user.setLastName(newName);
                break;
            case "Preferred Name":
                theDatabase.updatePreferredFirstName(user.getUserName(), newName);
                user.setPreferredFirstName(newName);
                break;
        }
        return true; // Success
    }

    private static void showAlert(String header, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Update Error");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    private static void showSuccess(String header, String content) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

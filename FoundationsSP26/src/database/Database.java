package database;

import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import entityClasses.User;
import entityClasses.Post;

/*******
 * <p> Title: Database Class.  </p>
 * 
 * <p> Description: This is an in-memory database built on H2.  Detailed documentation of H2 can
 * be found at https://www.h2database.com/html/main.html (Click on "PDF (2MP) for a PDF of 438 pages
 * on the H2 main page.)  This class leverages H2 and provides numerous special supporting methods.
 * </p>
 * 
 * <p> Copyright: Lynn Robert Carter © 2025 </p>
 * 
 * @author Lynn Robert Carter
 * @author Prince Dahiya
 * 
 * @version 2.00		2025-04-29 Updated and expanded from the version produce by on a previous
 * 							version by Pravalika Mukkiri and Ishwarya Hidkimath Basavaraj
 * @version 2.01		2025-12-17 Minor updates for Spring 2026
 * @version 2.02		2026-02-04 Added a function to return All users in the Database
 * @version 2.03		2026-02-03 Added a function to delete a user in the Database
 * @version 2.04		2026-02-08 Added and fixed logic for OTP feature
 * @version 2.05		2026-03-23 Fixed an issue in getAllPosts() and getPostsByAuthor() which would
 * 							make replies of soft deleted posts inaccessible 
 */

/*
 * The Database class is responsible for establishing and managing the connection to the database,
 * and performing operations such as user registration, login validation, handling invitation 
 * codes, and numerous other database related functions.
 */
public class Database {

	// JDBC driver name and database URL 
	static final String JDBC_DRIVER = "org.h2.Driver";   
	static final String DB_URL = "jdbc:h2:~/FoundationDatabase";  

	//  Database credentials 
	static final String USER = "sa"; 
	static final String PASS = ""; 

	//  Shared variables used within this class
	private Connection connection = null;		// Singleton to access the database 
	private Statement statement = null;			// The H2 Statement is used to construct queries
	
	// These are the easily accessible attributes of the currently logged-in user
	// This is only useful for single user applications
	private String currentUsername;
	private String currentPassword;
	private String currentFirstName;
	private String currentMiddleName;
	private String currentLastName;
	private String currentPreferredFirstName;
	private String currentEmailAddress;
	private boolean currentAdminRole;
	private boolean currentNewRole1;
	private boolean currentNewRole2;
	
	private String DB_URL_OVERRIDE = null;

	/*******
	 * <p> Method: Database </p>
	 * 
	 * <p> Description: The default constructor used to establish this singleton object.</p>
	 * 
	 */
	public Database () {
		
	}
	
	/*******
    * <p> Method: Database(String) </p>
    * <p> Description: Overloaded constructor that accepts a custom DB URL. Used by
    * TestStudentPosts.java to connect to a separate test database so tests never
    * touch live data. The standard constructor still uses the default URL. </p>
    *
    * @param dbUrl The JDBC URL for the database to connect to
    */
	public Database(String dbUrl) {
	    this.DB_URL_OVERRIDE = dbUrl;
	}
	
	
/*******
 * <p> Method: connectToDatabase </p>
 * 
 * <p> Description: Used to establish the in-memory instance of the H2 database from secondary
 *		storage.</p>
 *
 * @throws SQLException when the DriverManager is unable to establish a connection
 * 
 */
	public void connectToDatabase() throws SQLException {
		try {
			Class.forName(JDBC_DRIVER); // Load the JDBC driver
			connection = DriverManager.getConnection(
				    DB_URL_OVERRIDE != null ? DB_URL_OVERRIDE : DB_URL, USER, PASS);
			statement = connection.createStatement(); 
			// You can use this command to clear the database and restart from fresh.
			//statement.execute("DROP ALL OBJECTS");

			createTables();  // Create the necessary tables if they don't exist
		} catch (ClassNotFoundException e) {
			System.err.println("JDBC Driver not found: " + e.getMessage());
		}
	}

	
/*******
 * <p> Method: createTables </p>
 * 
 * <p> Description: Used to create new instances of the two database tables used by this class.</p>
 * 
 */
	private void createTables() throws SQLException {
		// Create the user database
		String userTable = "CREATE TABLE IF NOT EXISTS userDB ("
				+ "id INT AUTO_INCREMENT PRIMARY KEY, "
				+ "userName VARCHAR(255) UNIQUE, "
				+ "password VARCHAR(255), "
				+ "firstName VARCHAR(255), "
				+ "middleName VARCHAR(255), "
				+ "lastName VARCHAR (255), "
				+ "preferredFirstName VARCHAR(255), "
				+ "emailAddress VARCHAR(255), "
				+ "adminRole BOOL DEFAULT FALSE, "
				+ "newRole1 BOOL DEFAULT FALSE, "
				+ "newRole2 BOOL DEFAULT FALSE,"
				+ "isOTP BOOL DEFAULT FALSE)";
		statement.execute(userTable);
		
		// Create the invitation codes table
	    String invitationCodesTable = "CREATE TABLE IF NOT EXISTS InvitationCodes ("
	            + "code VARCHAR(10) PRIMARY KEY, "
	    		+ "emailAddress VARCHAR(255), "
	            + "role VARCHAR(10))";
	    statement.execute(invitationCodesTable);
	    createPostsTable();
	    createReadStatusTable();
	}


/*******
 * <p> Method: isDatabaseEmpty </p>
 * 
 * <p> Description: If the user database has no rows, true is returned, else false.</p>
 * 
 * @return true if the database is empty, else it returns false
 * 
 */
	public boolean isDatabaseEmpty() {
		String query = "SELECT COUNT(*) AS count FROM userDB";
		try {
			ResultSet resultSet = statement.executeQuery(query);
			if (resultSet.next()) {
				return resultSet.getInt("count") == 0;
			}
		}  catch (SQLException e) {
	        return false;
	    }
		return true;
	}
	
	
/*******
 * <p> Method: getNumberOfUsers </p>
 * 
 * <p> Description: Returns an integer .of the number of users currently in the user database. </p>
 * 
 * @return the number of user records in the database.
 * 
 */
	public int getNumberOfUsers() {
		String query = "SELECT COUNT(*) AS count FROM userDB";
		try {
			ResultSet resultSet = statement.executeQuery(query);
			if (resultSet.next()) {
				return resultSet.getInt("count");
			}
		} catch (SQLException e) {
	        return 0;
	    }
		return 0;
	}

/*******
 * <p> Method: register(User user) </p>
 * 
 * <p> Description: Creates a new row in the database using the user parameter. </p>
 * 
 * @throws SQLException when there is an issue creating the SQL command or executing it.
 * 
 * @param user specifies a user object to be added to the database.
 * 
 */
	public void register(User user) throws SQLException {
		String insertUser = "INSERT INTO userDB (userName, password, firstName, middleName, "
				+ "lastName, preferredFirstName, emailAddress, adminRole, newRole1, newRole2, isOTP) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(insertUser)) {
			currentUsername = user.getUserName();
			pstmt.setString(1, currentUsername);
			
			currentPassword = user.getPassword();
			pstmt.setString(2, currentPassword);
			
			currentFirstName = user.getFirstName();
			pstmt.setString(3, currentFirstName);
			
			currentMiddleName = user.getMiddleName();			
			pstmt.setString(4, currentMiddleName);
			
			currentLastName = user.getLastName();
			pstmt.setString(5, currentLastName);
			
			currentPreferredFirstName = user.getPreferredFirstName();
			pstmt.setString(6, currentPreferredFirstName);
			
			currentEmailAddress = user.getEmailAddress();
			pstmt.setString(7, currentEmailAddress);
			
			currentAdminRole = user.getAdminRole();
			pstmt.setBoolean(8, currentAdminRole);
			
			currentNewRole1 = user.getNewRole1();
			pstmt.setBoolean(9, currentNewRole1);
			
			currentNewRole2 = user.getNewRole2();
			pstmt.setBoolean(10, currentNewRole2);
			
			pstmt.setBoolean(11, user.getHasOTP());
			
			pstmt.executeUpdate();
		}
		
	}
	
/*******
 *  <p> Method: List getUserList() </p>
 *  
 *  <P> Description: Generate an List of Strings, one for each user in the database,
 *  starting with {@code <Select User>} at the start of the list. </p>
 *  
 *  @return a list of userNames found in the database.
 */
	public List<String> getUserList () {
		List<String> userList = new ArrayList<String>();
		userList.add("<Select a User>");
		String query = "SELECT userName FROM userDB";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				userList.add(rs.getString("userName"));
			}
		} catch (SQLException e) {
	        return null;
	    }
//		System.out.println(userList);
		return userList;
	}

/*******
 * <p> Method: boolean loginAdmin(User user) </p>
 * 
 * <p> Description: Check to see that a user with the specified username, password, and role
 * 		is the same as a row in the table for the username, password, and role. </p>
 * 
 * @param user specifies the specific user that should be logged in playing the Admin role.
 * 
 * @return true if the specified user has been logged in as an Admin else false.
 * 
 */
	public boolean loginAdmin(User user){
		// Validates an admin user's login credentials so the user can login in as an Admin.
		String query = "SELECT * FROM userDB WHERE userName = ? AND password = ? AND "
				+ "adminRole = TRUE";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, user.getUserName());
			pstmt.setString(2, user.getPassword());
			ResultSet rs = pstmt.executeQuery();
			return rs.next();	// If a row is returned, rs.next() will return true		
		} catch  (SQLException e) {
	        e.printStackTrace();
	    }
		return false;
	}
	
	
/*******
 * <p> Method: boolean loginRole1(User user) </p>
 * 
 * <p> Description: Check to see that a user with the specified username, password, and role
 * 		is the same as a row in the table for the username, password, and role. </p>
 * 
 * @param user specifies the specific user that should be logged in playing the Student role.
 * 
 * @return true if the specified user has been logged in as an Student else false.
 * 
 */
	public boolean loginRole1(User user) {
		// Validates a student user's login credentials.
		String query = "SELECT * FROM userDB WHERE userName = ? AND password = ? AND "
				+ "newRole1 = TRUE";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, user.getUserName());
			pstmt.setString(2, user.getPassword());
			ResultSet rs = pstmt.executeQuery();
			return rs.next();
		} catch  (SQLException e) {
		       e.printStackTrace();
		}
		return false;
	}

	/*******
	 * <p> Method: boolean loginRole2(User user) </p>
	 * 
	 * <p> Description: Check to see that a user with the specified username, password, and role
	 * 		is the same as a row in the table for the username, password, and role. </p>
	 * 
	 * @param user specifies the specific user that should be logged in playing the Reviewer role.
	 * 
	 * @return true if the specified user has been logged in as an Student else false.
	 * 
	 */
	// Validates a reviewer user's login credentials.
	public boolean loginRole2(User user) {
		String query = "SELECT * FROM userDB WHERE userName = ? AND password = ? AND "
				+ "newRole2 = TRUE";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, user.getUserName());
			pstmt.setString(2, user.getPassword());
			ResultSet rs = pstmt.executeQuery();
			return rs.next();
		} catch  (SQLException e) {
		       e.printStackTrace();
		}
		return false;
	}
	
	
	/*******
	 * <p> Method: boolean doesUserExist(User user) </p>
	 * 
	 * <p> Description: Check to see that a user with the specified username is  in the table. </p>
	 * 
	 * @param userName specifies the specific user that we want to determine if it is in the table.
	 * 
	 * @return true if the specified user is in the table else false.
	 * 
	 */
	// Checks if a user already exists in the database based on their userName.
	public boolean doesUserExist(String userName) {
	    String query = "SELECT COUNT(*) FROM userDB WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        
	        pstmt.setString(1, userName);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            // If the count is greater than 0, the user exists
	            return rs.getInt(1) > 0;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false; // If an error occurs, assume user doesn't exist
	}

	
	/*******
	 * <p> Method: int getNumberOfRoles(User user) </p>
	 * 
	 * <p> Description: Determine the number of roles a specified user plays. </p>
	 * 
	 * @param user specifies the specific user that we want to determine if it is in the table.
	 * 
	 * @return the number of roles this user plays (0 - 5).
	 * 
	 */	
	// Get the number of roles that this user plays
	public int getNumberOfRoles (User user) {
		int numberOfRoles = 0;
		if (user.getAdminRole()) numberOfRoles++;
		if (user.getNewRole1()) numberOfRoles++;
		if (user.getNewRole2()) numberOfRoles++;
		return numberOfRoles;
	}	

	
	/*******
	 * <p> Method: String generateInvitationCode(String emailAddress, String role) </p>
	 * 
	 * <p> Description: Given an email address and a roles, this method establishes and invitation
	 * code and adds a record to the InvitationCodes table.  When the invitation code is used, the
	 * stored email address is used to establish the new user and the record is removed from the
	 * table.</p>
	 * 
	 * @param emailAddress specifies the email address for this new user.
	 * 
	 * @param role specified the role that this new user will play.
	 * 
	 * @return the code of six characters so the new user can use it to securely setup an account.
	 * 
	 */
	// Generates a new invitation code and inserts it into the database.
	public String generateInvitationCode(String emailAddress, String role) {
	    String code = UUID.randomUUID().toString().substring(0, 6); // Generate a random 6-character code
	    String query = "INSERT INTO InvitationCodes (code, emailaddress, role) VALUES (?, ?, ?)";

	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        pstmt.setString(2, emailAddress);
	        pstmt.setString(3, role);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return code;
	}

	
	/*******
	 * <p> Method: int getNumberOfInvitations() </p>
	 * 
	 * <p> Description: Determine the number of outstanding invitations in the table.</p>
	 *  
	 * @return the number of invitations in the table.
	 * 
	 */
	// Number of invitations in the database
	public int getNumberOfInvitations() {
		String query = "SELECT COUNT(*) AS count FROM InvitationCodes";
		try {
			ResultSet resultSet = statement.executeQuery(query);
			if (resultSet.next()) {
				return resultSet.getInt("count");
			}
		} catch  (SQLException e) {
	        e.printStackTrace();
	    }
		return 0;
	}
	
	
	/*******
	 * <p> Method: boolean emailaddressHasBeenUsed(String emailAddress) </p>
	 * 
	 * <p> Description: Determine if an email address has been user to establish a user.</p>
	 * 
	 * @param emailAddress is a string that identifies a user in the table
	 *  
	 * @return true if the email address is in the table, else return false.
	 * 
	 */
	// Check to see if an email address is already in the database
	public boolean emailaddressHasBeenUsed(String emailAddress) {
	    String query = "SELECT COUNT(*) AS count FROM InvitationCodes WHERE emailAddress = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, emailAddress);
	        ResultSet rs = pstmt.executeQuery();
	 //     System.out.println(rs);
	        if (rs.next()) {
	            // Mark the code as used
	        	return rs.getInt("count")>0;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
		return false;
	}
	
	
	/*******
	 * <p> Method: String getRoleGivenAnInvitationCode(String code) </p>
	 * 
	 * <p> Description: Get the role associated with an invitation code.</p>
	 * 
	 * @param code is the 6 character String invitation code
	 *  
	 * @return the role for the code or an empty string.
	 * 
	 */
	// Obtain the roles associated with an invitation code.
	public String getRoleGivenAnInvitationCode(String code) {
	    String query = "SELECT * FROM InvitationCodes WHERE code = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            return rs.getString("role");
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return "";
	}

	
	/*******
	 * <p> Method: String getEmailAddressUsingCode (String code ) </p>
	 * 
	 * <p> Description: Get the email addressed associated with an invitation code.</p>
	 * 
	 * @param code is the 6 character String invitation code
	 *  
	 * @return the email address for the code or an empty string.
	 * 
	 */
	// For a given invitation code, return the associated email address of an empty string
	public String getEmailAddressUsingCode (String code ) {
	    String query = "SELECT emailAddress FROM InvitationCodes WHERE code = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            return rs.getString("emailAddress");
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
		return "";
	}
	
	
	/*******
	 * <p> Method: void removeInvitationAfterUse(String code) </p>
	 * 
	 * <p> Description: Remove an invitation record once it is used.</p>
	 * 
	 * @param code is the 6 character String invitation code
	 *  
	 */
	// Remove an invitation using an email address once the user account has been setup
	public void removeInvitationAfterUse(String code) {
	    String query = "SELECT COUNT(*) AS count FROM InvitationCodes WHERE code = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	        	int counter = rs.getInt(1);
	            // Only do the remove if the code is still in the invitation table
	        	if (counter > 0) {
        			query = "DELETE FROM InvitationCodes WHERE code = ?";
	        		try (PreparedStatement pstmt2 = connection.prepareStatement(query)) {
	        			pstmt2.setString(1, code);
	        			pstmt2.executeUpdate();
	        		}catch (SQLException e) {
	        	        e.printStackTrace();
	        	    }
	        	}
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
		return;
	}
	
	
	/*******
	 * <p> Method: String getFirstName(String username) </p>
	 * 
	 * <p> Description: Get the first name of a user given that user's username.</p>
	 * 
	 * @param username is the username of the user
	 * 
	 * @return the first name of a user given that user's username 
	 *  
	 */
	// Get the First Name
	public String getFirstName(String username) {
		String query = "SELECT firstName FROM userDB WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            return rs.getString("firstName"); // Return the first name if user exists
	        }
			
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
		return null;
	}
	

	/*******
	 * <p> Method: void updateFirstName(String username, String firstName) </p>
	 * 
	 * <p> Description: Update the first name of a user given that user's username and the new
	 *		first name.</p>
	 * 
	 * @param username is the username of the user
	 * 
	 * @param firstName is the new first name for the user
	 *  
	 */
	// update the first name
	public void updateFirstName(String username, String firstName) {
	    String query = "UPDATE userDB SET firstName = ? WHERE username = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, firstName);
	        pstmt.setString(2, username);
	        pstmt.executeUpdate();
	        currentFirstName = firstName;
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

	
	/*******
	 * <p> Method: String getMiddleName(String username) </p>
	 * 
	 * <p> Description: Get the middle name of a user given that user's username.</p>
	 * 
	 * @param username is the username of the user
	 * 
	 * @return the middle name of a user given that user's username 
	 *  
	 */
	// get the middle name
	public String getMiddleName(String username) {
		String query = "SELECT MiddleName FROM userDB WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            return rs.getString("middleName"); // Return the middle name if user exists
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
		return null;
	}

	
	/*******
	 * <p> Method: void updateMiddleName(String username, String middleName) </p>
	 * 
	 * <p> Description: Update the middle name of a user given that user's username and the new
	 * 		middle name.</p>
	 * 
	 * @param username is the username of the user
	 *  
	 * @param middleName is the new middle name for the user
	 *  
	 */
	// update the middle name
	public void updateMiddleName(String username, String middleName) {
	    String query = "UPDATE userDB SET middleName = ? WHERE username = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, middleName);
	        pstmt.setString(2, username);
	        pstmt.executeUpdate();
	        currentMiddleName = middleName;
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	
	/*******
	 * <p> Method: String getLastName(String username) </p>
	 * 
	 * <p> Description: Get the last name of a user given that user's username.</p>
	 * 
	 * @param username is the username of the user
	 * 
	 * @return the last name of a user given that user's username 
	 *  
	 */
	// get he last name
	public String getLastName(String username) {
		String query = "SELECT LastName FROM userDB WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            return rs.getString("lastName"); // Return last name role if user exists
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
		return null;
	}
	
	
	/*******
	 * <p> Method: void updateLastName(String username, String lastName) </p>
	 * 
	 * <p> Description: Update the middle name of a user given that user's username and the new
	 * 		middle name.</p>
	 * 
	 * @param username is the username of the user
	 *  
	 * @param lastName is the new last name for the user
	 *  
	 */
	// update the last name
	public void updateLastName(String username, String lastName) {
	    String query = "UPDATE userDB SET lastName = ? WHERE username = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, lastName);
	        pstmt.setString(2, username);
	        pstmt.executeUpdate();
	        currentLastName = lastName;
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	
	/*******
	 * <p> Method: String getPreferredFirstName(String username) </p>
	 * 
	 * <p> Description: Get the preferred first name of a user given that user's username.</p>
	 * 
	 * @param username is the username of the user
	 * 
	 * @return the preferred first name of a user given that user's username 
	 *  
	 */
	// get the preferred first name
	public String getPreferredFirstName(String username) {
		String query = "SELECT preferredFirstName FROM userDB WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            return rs.getString("firstName"); // Return the preferred first name if user exists
	        }
			
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
		return null;
	}
	
	
	/*******
	 * <p> Method: void updatePreferredFirstName(String username, String preferredFirstName) </p>
	 * 
	 * <p> Description: Update the preferred first name of a user given that user's username and
	 * 		the new preferred first name.</p>
	 * 
	 * @param username is the username of the user
	 *  
	 * @param preferredFirstName is the new preferred first name for the user
	 *  
	 */
	// update the preferred first name of the user
	public void updatePreferredFirstName(String username, String preferredFirstName) {
	    String query = "UPDATE userDB SET preferredFirstName = ? WHERE username = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, preferredFirstName);
	        pstmt.setString(2, username);
	        pstmt.executeUpdate();
	        currentPreferredFirstName = preferredFirstName;
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	
	/*******
	 * <p> Method: String getEmailAddress(String username) </p>
	 * 
	 * <p> Description: Get the email address of a user given that user's username.</p>
	 * 
	 * @param username is the username of the user
	 * 
	 * @return the email address of a user given that user's username 
	 *  
	 */
	// get the email address
	public String getEmailAddress(String username) {
		String query = "SELECT emailAddress FROM userDB WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            return rs.getString("emailAddress"); // Return the email address if user exists
	        }
			
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
		return null;
	}
	
	
	/*******
	 * <p> Method: void updateEmailAddress(String username, String emailAddress) </p>
	 * 
	 * <p> Description: Update the email address name of a user given that user's username and
	 * 		the new email address.</p>
	 * 
	 * @param username is the username of the user
	 *  
	 * @param emailAddress is the new preferred first name for the user
	 *  
	 */
	// update the email address
	public void updateEmailAddress(String username, String emailAddress) {
	    String query = "UPDATE userDB SET emailAddress = ? WHERE username = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, emailAddress);
	        pstmt.setString(2, username);
	        pstmt.executeUpdate();
	        currentEmailAddress = emailAddress;
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	
	/*******
	 * <p> Method: boolean getUserAccountDetails(String username) </p>
	 * 
	 * <p> Description: Get all the attributes of a user given that user's username.</p>
	 * 
	 * @param username is the username of the user
	 * 
	 * @return true of the get is successful, else false
	 *  
	 */
	// get the attributes for a specified user
	public boolean getUserAccountDetails(String username) {
		String query = "SELECT * FROM userDB WHERE username = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
	        ResultSet rs = pstmt.executeQuery();			
			rs.next();
	    	currentUsername = rs.getString(2);
	    	currentPassword = rs.getString(3);
	    	currentFirstName = rs.getString(4);
	    	currentMiddleName = rs.getString(5);
	    	currentLastName = rs.getString(6);
	    	currentPreferredFirstName = rs.getString(7);
	    	currentEmailAddress = rs.getString(8);
	    	currentAdminRole = rs.getBoolean(9);
	    	currentNewRole1 = rs.getBoolean(10);
	    	currentNewRole2 = rs.getBoolean(11);
			return true;
	    } catch (SQLException e) {
			return false;
	    }
	}
	
	
	/*******
	 * <p> Method: boolean updateUserRole(String username, String role, String value) </p>
	 * 
	 * <p> Description: Update a specified role for a specified user's and set and update all the
	 * 		current user attributes.</p>
	 * 
	 * @param username is the username of the user
	 *  
	 * @param role is string that specifies the role to update
	 * 
	 * @param value is the string that specified TRUE or FALSE for the role
	 * 
	 * @return true if the update was successful, else false
	 *  
	 */
	// Update a users role
	public boolean updateUserRole(String username, String role, String value) {
		if (role.compareTo("Admin") == 0) {
			String query = "UPDATE userDB SET adminRole = ? WHERE username = ?";
			try (PreparedStatement pstmt = connection.prepareStatement(query)) {
				pstmt.setString(1, value);
				pstmt.setString(2, username);
				pstmt.executeUpdate();
				if (value.compareTo("true") == 0)
					currentAdminRole = true;
				else
					currentAdminRole = false;
				return true;
			} catch (SQLException e) {
				return false;
			}
		}
		if (role.compareTo("Role1") == 0) {
			String query = "UPDATE userDB SET newRole1 = ? WHERE username = ?";
			try (PreparedStatement pstmt = connection.prepareStatement(query)) {
				pstmt.setString(1, value);
				pstmt.setString(2, username);
				pstmt.executeUpdate();
				if (value.compareTo("true") == 0)
					currentNewRole1 = true;
				else
					currentNewRole1 = false;
				return true;
			} catch (SQLException e) {
				return false;
			}
		}
		if (role.compareTo("Role2") == 0) {
			String query = "UPDATE userDB SET newRole2 = ? WHERE username = ?";
			try (PreparedStatement pstmt = connection.prepareStatement(query)) {
				pstmt.setString(1, value);
				pstmt.setString(2, username);
				pstmt.executeUpdate();
				if (value.compareTo("true") == 0)
					currentNewRole2 = true;
				else
					currentNewRole2 = false;
				return true;
			} catch (SQLException e) {
				return false;
			}
		}
		return false;
	}
	
	
	// Attribute getters for the current user
	/*******
	 * <p> Method: String getCurrentUsername() </p>
	 * 
	 * <p> Description: Get the current user's username.</p>
	 * 
	 * @return the username value is returned
	 *  
	 */
	public String getCurrentUsername() { return currentUsername;};

	
	/*******
	 * <p> Method: String getCurrentPassword() </p>
	 * 
	 * <p> Description: Get the current user's password.</p>
	 * 
	 * @return the password value is returned
	 *  
	 */
	public String getCurrentPassword() { return currentPassword;};

	
	/*******
	 * <p> Method: String getCurrentFirstName() </p>
	 * 
	 * <p> Description: Get the current user's first name.</p>
	 * 
	 * @return the first name value is returned
	 *  
	 */
	public String getCurrentFirstName() { return currentFirstName;};

	
	/*******
	 * <p> Method: String getCurrentMiddleName() </p>
	 * 
	 * <p> Description: Get the current user's middle name.</p>
	 * 
	 * @return the middle name value is returned
	 *  
	 */
	public String getCurrentMiddleName() { return currentMiddleName;};

	
	/*******
	 * <p> Method: String getCurrentLastName() </p>
	 * 
	 * <p> Description: Get the current user's last name.</p>
	 * 
	 * @return the last name value is returned
	 *  
	 */
	public String getCurrentLastName() { return currentLastName;};

	
	/*******
	 * <p> Method: String getCurrentPreferredFirstName( </p>
	 * 
	 * <p> Description: Get the current user's preferred first name.</p>
	 * 
	 * @return the preferred first name value is returned
	 *  
	 */
	public String getCurrentPreferredFirstName() { return currentPreferredFirstName;};

	
	/*******
	 * <p> Method: String getCurrentEmailAddress() </p>
	 * 
	 * <p> Description: Get the current user's email address name.</p>
	 * 
	 * @return the email address value is returned
	 *  
	 */
	public String getCurrentEmailAddress() { return currentEmailAddress;};

	
	/*******
	 * <p> Method: boolean getCurrentAdminRole() </p>
	 * 
	 * <p> Description: Get the current user's Admin role attribute.</p>
	 * 
	 * @return true if this user plays an Admin role, else false
	 *  
	 */
	public boolean getCurrentAdminRole() { return currentAdminRole;};

	
	/*******
	 * <p> Method: boolean getCurrentNewRole1() </p>
	 * 
	 * <p> Description: Get the current user's Student role attribute.</p>
	 * 
	 * @return true if this user plays a Student role, else false
	 *  
	 */
	public boolean getCurrentNewRole1() { return currentNewRole1;};

	
	/*******
	 * <p> Method: boolean getCurrentNewRole2() </p>
	 * 
	 * <p> Description: Get the current user's Reviewer role attribute.</p>
	 * 
	 * @return true if this user plays a Reviewer role, else false
	 *  
	 */
	public boolean getCurrentNewRole2() { return currentNewRole2;};

	
	/*******
	 * <p> Debugging method</p>
	 * 
	 * <p> Description: Debugging method that dumps the database of the console.</p>
	 * 
	 * @throws SQLException if there is an issues accessing the database.
	 * 
	 */
	// Dumps the database.
	public void dump() throws SQLException {
		String query = "SELECT * FROM userDB";
		ResultSet resultSet = statement.executeQuery(query);
		ResultSetMetaData meta = resultSet.getMetaData();
		while (resultSet.next()) {
		for (int i = 0; i < meta.getColumnCount(); i++) {
		System.out.println(
		meta.getColumnLabel(i + 1) + ": " +
				resultSet.getString(i + 1));
		}
		System.out.println();
		}
		resultSet.close();
	}

	

	/*******
	 * <p> Method: {@code ArrayList<User>} getAllUsers() </p>
	 * 
	 * <p> Description: Fetch all users from the database to display in a list. </p>
	 * 
	 * @return ArrayList of User objects
	 */
	public java.util.ArrayList<User> getAllUsers() {
		java.util.ArrayList<User> list = new java.util.ArrayList<>();
		String query = "SELECT * FROM userDB";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				// Create User object from DB row
				User u = new User(
					rs.getString("userName"),
					rs.getString("password"),
					rs.getString("firstName"),
					rs.getString("middleName"),
					rs.getString("lastName"),
					rs.getString("preferredFirstName"),
					rs.getString("emailAddress"),
					rs.getBoolean("adminRole"),
					rs.getBoolean("newRole1"), // Role1 (Student)
					rs.getBoolean("newRole2"),  // Role2 (Staff)
					rs.getBoolean("isOTP")
				);
				list.add(u);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}
	
	
	/*******
	 * <p> Method: void deleteUser(String username) </p>
	 * 
	 * <p> Description: Deletes a user from the database. </p>
	 * 
	 * @param username The username of the account to delete
	 */
	public void deleteUser(String username) {
		String query = "DELETE FROM userDB WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	/**********
	 * <p> Method: resetPassword </p>
	 * 
	 * <p> Description: Resets the user's password to a temporary OTP and flags it. </p>
	 *
	 * @param username The username to change password for
	 * @param tempPassword The OneTimePassword
	 */
	public void resetPassword(String username, String tempPassword) {
		String query = "UPDATE userDB SET password = ?, isOTP = TRUE WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, tempPassword);
			pstmt.setString(2, username);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	/**********
	 * <p> Method: updatePassword </p>
	 * * <p> Description: Updates the password and clears the OTP flag. </p>
	 */
	public void updatePassword(String username, String newPassword) {
		// CRITICAL: We also set isOTP = FALSE because the user has now fixed it.
		String query = "UPDATE userDB SET password = ?, isOTP = FALSE WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, newPassword);
			pstmt.setString(2, username);
			pstmt.executeUpdate();
			currentPassword = newPassword;
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	/*******
	 * <p> Method: void closeConnection()</p>
	 * 
	 * <p> Description: Closes the database statement and connection.</p>
	 * 
	 */
	// Closes the database statement and connection.
	public void closeConnection() {
		try{ 
			if(statement!=null) statement.close(); 
		} catch(SQLException se2) { 
			se2.printStackTrace();
		} 
		try { 
			if(connection!=null) connection.close(); 
		} catch(SQLException se){ 
			se.printStackTrace(); 
		} 
	}
	
	// ==================================================================================
	// TP2 ADDITIONS
	// ==================================================================================
	//
	// The schema has changed — new columns (thread, isDeleted) and a new table
	// (readStatusDB). The ALTER TABLE lines handle migration so existing data isn't lost.
	// ==================================================================================


    /*******
     * <p> Method: createPostsTable </p>
     * <p> Description: Creates the postDB table if it doesn't exist. Also runs ALTER TABLE
     * to add new columns (thread, isDeleted) in case this is an existing database from
     * a previous run without those columns. Safe to run repeatedly — IF NOT EXISTS / IF
     * COLUMN EXISTS guards prevent errors. </p>
     *
     * @throws SQLException if table creation fails
     */
    private void createPostsTable() throws SQLException {
        // Create the table if it's brand new
        String postTable = "CREATE TABLE IF NOT EXISTS postDB ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "title VARCHAR(200) NOT NULL, "
                + "body VARCHAR(4000) NOT NULL, "
                + "authorUsername VARCHAR(255) NOT NULL, "
                + "timestamp VARCHAR(30) NOT NULL, "
                + "postType VARCHAR(20) NOT NULL DEFAULT 'QUESTION', "
                + "thread VARCHAR(100) DEFAULT 'General', "
                + "parentPostId INT DEFAULT -1, "           // -1 = top-level thread
                + "isResolved BOOL DEFAULT FALSE, "
                + "isDeleted BOOL DEFAULT FALSE, "          // soft delete — replies stay
                + "isInstructorEndorsed BOOL DEFAULT FALSE, " // TP3 Staff Epics
                + "staffComment VARCHAR(1000) DEFAULT '')";   // TP3 Staff Epics
        statement.execute(postTable);

        // Migration: add new columns to existing databases that didn't have them yet
        // H2 ignores these if the column already exists
        try { statement.execute("ALTER TABLE postDB ADD COLUMN IF NOT EXISTS thread VARCHAR(100) DEFAULT 'General'"); }
        catch (SQLException ignored) {}
        try { statement.execute("ALTER TABLE postDB ADD COLUMN IF NOT EXISTS isDeleted BOOL DEFAULT FALSE"); }
        catch (SQLException ignored) {}
    }


    /*******
     * <p> Method: createReadStatusTable </p>
     * <p> Description: Creates the readStatusDB table that tracks which posts each user
     * has read. A post is "unread" for a user if no row exists for (username, postId).
     * When a student clicks on a post, we insert a row to mark it read.
     * Called from createTables() at startup. </p>
     *
     * @throws SQLException if table creation fails
     */
    private void createReadStatusTable() throws SQLException {
        // Composite primary key prevents duplicate read entries for the same user+post
        String readTable = "CREATE TABLE IF NOT EXISTS readStatusDB ("
                + "username VARCHAR(255) NOT NULL, "
                + "postId INT NOT NULL, "
                + "PRIMARY KEY (username, postId))";
        statement.execute(readTable);
    }


    /*******
     * <p> Method: createPost(Post) </p>
     * <p> Description: Inserts a new post into postDB. The Create part of CRUD.
     * Caller should validate fields via Post.validateTitle/validateBody first.
     * Works for both top-level posts (parentPostId = -1) and replies. </p>
     *
     * @param post A fully built Post object. The id field is ignored (DB auto-assigns).
     * @throws SQLException if the insert fails
     */
    public void createPost(Post post) throws SQLException {
        String insertPost = "INSERT INTO postDB "
                + "(title, body, authorUsername, timestamp, postType, thread, parentPostId, "
                + "isResolved, isDeleted, isInstructorEndorsed, staffComment) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertPost)) {
            pstmt.setString(1, post.getTitle());
            pstmt.setString(2, post.getBody());
            pstmt.setString(3, post.getAuthorUsername());
            pstmt.setString(4, post.getTimestamp());
            pstmt.setString(5, post.getPostType());
            pstmt.setString(6, post.getThread());
            pstmt.setInt(7, post.getParentPostId());
            pstmt.setBoolean(8, post.isResolved());
            pstmt.setBoolean(9, post.isDeleted());
            pstmt.setBoolean(10, post.isInstructorEndorsed());
            pstmt.setString(11, post.getStaffComment());
            pstmt.executeUpdate();
        }
    }


    /*******
     * <p> Method: getAllPosts() </p>
     * <p> Description: Returns all non-deleted top-level posts (parentPostId = -1),
     * newest first. Soft-deleted posts are filtered out of the main list.
     * Replies are fetched separately via getRepliesForPost(). </p>
     * 
     * <p> Also shows soft deleted posts that have replies </p>
     *
     * @return ArrayList of top-level Post objects, never null
     */
    public ArrayList<Post> getAllPosts() {
        ArrayList<Post> list = new ArrayList<>();
        // FIX: Show the post if it is NOT deleted, OR if it has replies
        String query = "SELECT * FROM postDB WHERE parentPostId = -1 "
                + "AND (isDeleted = FALSE OR id IN (SELECT parentPostId FROM postDB)) "
                + "ORDER BY id DESC";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) list.add(postFromResultSet(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }


    /*******
     * <p> Method: getPostsByAuthor(String) </p>
     * <p> Description: Returns all non-deleted top-level posts by a specific author,
     * newest first. Powers the "My Posts" view per the user story: "I can see a list
     * of my posts, the number of replies, and how many I have not yet read." </p>
     * 
     * <p> Also shows soft deleted posts that have replies </p>
     *
     * @param username The author's username to filter by
     * @return ArrayList of the author's top-level posts, never null
     */
    public ArrayList<Post> getPostsByAuthor(String username) {
        ArrayList<Post> list = new ArrayList<>();
        // FIX: Show the post if it is NOT deleted, OR if it has replies
        String query = "SELECT * FROM postDB WHERE authorUsername = ? "
                + "AND parentPostId = -1 "
                + "AND (isDeleted = FALSE OR id IN (SELECT parentPostId FROM postDB)) "
                + "ORDER BY id DESC";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) list.add(postFromResultSet(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }


    /*******
     * <p> Method: getRepliesForPost(int) </p>
     * <p> Description: Returns all replies for a given post, oldest first so the
     * conversation reads in order. Includes soft-deleted replies so the thread
     * stays intact (deletion notice shown by the View). </p>
     *
     * @param parentPostId The ID of the post whose replies we want
     * @return ArrayList of reply Posts, oldest first, never null
     */
    public ArrayList<Post> getRepliesForPost(int parentPostId) {
        ArrayList<Post> replies = new ArrayList<>();
        String query = "SELECT * FROM postDB WHERE parentPostId = ? ORDER BY id ASC";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, parentPostId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) replies.add(postFromResultSet(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return replies;
    }


    /*******
     * <p> Method: getPostById(int) </p>
     * <p> Description: Fetches a single post by its primary key. Used by the Controller
     * before editing, deleting, or resolving to verify the post exists and check ownership.
     * Returns null if not found. </p>
     *
     * @param id The primary key of the post
     * @return The Post if found, or null
     */
    public Post getPostById(int id) {
        String query = "SELECT * FROM postDB WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return postFromResultSet(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    /*******
     * <p> Method: updatePost(int, String, String) </p>
     * <p> Description: Updates the title and body of an existing post. The Update part
     * of CRUD. Controller checks ownership before calling this. Only title and body
     * are editable — author, timestamp, and type are immutable after posting. </p>
     *
     * @param id       The ID of the post to update
     * @param newTitle The new title (already validated by the Controller)
     * @param newBody  The new body (already validated by the Controller)
     */
    public void updatePost(int id, String newTitle, String newBody) {
        String query = "UPDATE postDB SET title = ?, body = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, newTitle);
            pstmt.setString(2, newBody);
            pstmt.setInt(3, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /*******
     * <p> Method: softDeletePost(int) </p>
     * <p> Description: Soft-deletes a post by setting isDeleted = TRUE. Replies are NOT
     * removed — they stay in the DB per the user story: "any replies to that post are
     * not deleted, but anyone viewing the reply will see a message saying the original
     * post has been deleted." The post disappears from getAllPosts() but its replies
     * remain. </p>
     *
     * @param id The ID of the post to soft-delete
     */
    public void softDeletePost(int id) {
        // Soft delete — mark as deleted but keep the record so replies stay visible
        String query = "UPDATE postDB SET isDeleted = TRUE WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /*******
     * <p> Method: deletePost(int) </p>
     * <p> Description: Allows Instructors/Graders to forcefully delete inappropriate 
     * student posts (Staff Epic 6 Moderation Override). </p>
     */
    public void deletePost(int postId) {
        String query = "DELETE FROM postDB WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, postId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /*******
     * <p> Method: markPostResolved(int, boolean) </p>
     * <p> Description: Toggles the isResolved flag on a QUESTION post. The Controller
     * checks ownership before calling this. </p>
     *
     * @param id       The ID of the post to update
     * @param resolved True to mark resolved, false to reopen
     */
    public void markPostResolved(int id, boolean resolved) {
        String query = "UPDATE postDB SET isResolved = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setBoolean(1, resolved);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /*******
     * <p> Method: searchPosts(String) </p>
     * <p> Description: Returns non-deleted top-level posts where title or body contains
     * the keyword (case-insensitive). Uses LOWER() on both sides since H2 LIKE is
     * case-sensitive by default. </p>
     *
     * @param keyword The search term
     * @return ArrayList of matching Posts, newest first, never null
     */
    public ArrayList<Post> searchPosts(String keyword) {
        ArrayList<Post> results = new ArrayList<>();
        // LOWER() on both sides for case-insensitive match in H2
        String query = "SELECT * FROM postDB WHERE parentPostId = -1 AND isDeleted = FALSE "
                + "AND (LOWER(title) LIKE LOWER(?) OR LOWER(body) LIKE LOWER(?)) "
                + "ORDER BY id DESC";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            String pattern = "%" + keyword + "%";
            pstmt.setString(1, pattern);
            pstmt.setString(2, pattern);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) results.add(postFromResultSet(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }


    /*******
     * <p> Method: markAsRead(int, String) </p>
     * <p> Description: Records that a specific user has read a specific post. Uses a
     * MERGE (H2's upsert) so calling it multiple times is safe — no duplicate rows. </p>
     *
     * @param postId   The ID of the post that was read
     * @param username The username of the student who read it
     */
    public void markAsRead(int postId, String username) {
        // MERGE is H2's upsert — inserts the row if it doesn't exist, ignores if it does
        String query = "MERGE INTO readStatusDB (username, postId) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.setInt(2, postId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /*******
     * <p> Method: isPostRead(int, String) </p>
     * <p> Description: Returns true if the given user has read the given post.
     * Used by buildDisplayString() in the Model to show the "NEW" badge. </p>
     *
     * @param postId   The post ID to check
     * @param username The username to check for
     * @return True if the user has read this post, false if unread
     */
    public boolean isPostRead(int postId, String username) {
        String query = "SELECT COUNT(*) FROM readStatusDB WHERE postId = ? AND username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, postId);
            pstmt.setString(2, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    /*******
     * <p> Method: getReplyCount(int) </p>
     * <p> Description: Returns the total number of replies for a given post.
     * Shown in the list view so students can see at a glance how active a thread is. </p>
     *
     * @param postId The parent post ID
     * @return Total reply count, or 0 if none
     */
    public int getReplyCount(int postId) {
        String query = "SELECT COUNT(*) FROM postDB WHERE parentPostId = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, postId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }


    /*******
     * <p> Method: getUnreadReplyCount(int, String) </p>
     * <p> Description: Returns how many replies to a post the given user hasn't read yet.
     * Used to show "(2 new)" in the list view so students know which threads have new
     * activity since they last checked. </p>
     *
     * @param postId   The parent post ID
     * @param username The username to check unread status for
     * @return Number of unread replies, or 0 if all read
     */
    public int getUnreadReplyCount(int postId, String username) {
        // Count replies that have no corresponding read entry for this user
        String query = "SELECT COUNT(*) FROM postDB WHERE parentPostId = ? "
                + "AND id NOT IN (SELECT postId FROM readStatusDB WHERE username = ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, postId);
            pstmt.setString(2, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }


    /*******
     * <p> Method: getPostCountForUser(String) </p>
     * <p> Description: Returns total post count for a user. Not used in the TP2 student
     * UI but included for TP3 Staff Epics where instructors need participation metrics. </p>
     *
     * @param username The username to count posts for
     * @return Total post count, or 0
     */
    public int getPostCountForUser(String username) {
        String query = "SELECT COUNT(*) FROM postDB WHERE authorUsername = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }


    /*******
     * <p> Method: postFromResultSet(ResultSet) </p>
     * <p> Description: Private helper that constructs a Post object from the current
     * row of a ResultSet. Centralizes the column-to-field mapping so we don't repeat
     * the same 12 rs.getString() calls in every query method. </p>
     *
     * @param rs A ResultSet positioned on the row to read
     * @return A fully constructed Post object
     * @throws SQLException if any column read fails
     */
    private Post postFromResultSet(ResultSet rs) throws SQLException {
        return new Post(
            rs.getInt("id"),
            rs.getString("title"),
            rs.getString("body"),
            rs.getString("authorUsername"),
            rs.getString("timestamp"),
            rs.getString("postType"),
            rs.getString("thread"),
            rs.getInt("parentPostId"),
            rs.getBoolean("isResolved"),
            rs.getBoolean("isDeleted"),
            rs.getBoolean("isInstructorEndorsed"),
            rs.getString("staffComment")
        );
    }
    
    /*******
     * <p> Method: dropAllPostTables() </p>
     * <p> Description: Drops the postDB and readStatusDB tables so the test suite
     * can start from a completely clean state on every run. Only used by the test class —
     * never call this in production code. </p>
     *
     * @throws SQLException if the drop fails
     */
    public void dropAllPostTables() throws SQLException {
        // Drop tables so the test suite always starts from a clean slate
        statement.execute("DROP TABLE IF EXISTS postDB");
        statement.execute("DROP TABLE IF EXISTS readStatusDB");
    }
    
    // ==================================================================================
    // TP3 GRADING & INSTRUCTOR ADDITIONS
    // ==================================================================================

    /*******
     * <p> Method: getConnection() </p>
     * <p> Description: Exposes the active JDBC connection to special verification
     * classes (like RuleOfThreeVerifier) so they can execute complex grading queries
     * without bloating the main Database.java file. </p>
     *
     * @return The active database Connection object
     */
    public Connection getConnection() {
        return this.connection;
    }

    /*******
     * <p> Method: updateStaffComment(int, String) </p>
     * <p> Description: Allows Graders and Instructors to append an internal, hidden
     * comment to a specific student post (Staff Epic 6). </p>
     *
     * @param postId  The ID of the post being commented on
     * @param comment The staff-only text to save
     */
    public void updateStaffComment(int postId, String comment) {
        String query = "UPDATE postDB SET staffComment = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, comment);
            pstmt.setInt(2, postId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /*******
     * <p> Method: updateInstructorEndorsement(int, boolean) </p>
     * <p> Description: Allows Instructors to officially endorse a student's reply
     * to highlight high-quality answers (Staff Epic 3). </p>
     *
     * @param postId   The ID of the post being endorsed
     * @param endorsed True to apply the endorsement badge, false to remove it
     */
    public void updateInstructorEndorsement(int postId, boolean endorsed) {
        String query = "UPDATE postDB SET isInstructorEndorsed = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setBoolean(1, endorsed);
            pstmt.setInt(2, postId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

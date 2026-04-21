package guiRole2;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import database.Database;
import entityClasses.User;
import javafx.scene.control.TextField;
import javafx.scene.control.ListView;
import javafx.scene.control.CheckBox;


/*******
 * <p> Title: ViewRole2Home Class. </p>
 * 
 * <p> Description: The Java/FX-based Role2 Home Page.  The page is a stub for some role needed for
 * the application.  The widgets on this page are likely the minimum number and kind for other role
 * pages that may be needed.</p>
 * 
 * <p> Copyright: Lynn Robert Carter © 2025 </p>
 * 
 * @author Lynn Robert Carter
 * 
 * @version 1.00		2025-04-20 Initial version
 *  
 */

public class ViewRole2Home {
	
	/*-*******************************************************************************************

	Attributes
	
	 */
	
	// These are the application values required by the user interface
	
	private static double width = applicationMain.FoundationsMain.WINDOW_WIDTH;
	private static double height = applicationMain.FoundationsMain.WINDOW_HEIGHT;


	// These are the widget attributes for the GUI. There are 3 areas for this GUI.
	
	// GUI Area 1: It informs the user about the purpose of this page, whose account is being used,
	// and a button to allow this user to update the account settings
	protected static Label label_PageTitle = new Label();
	protected static Label label_UserDetails = new Label();
	protected static Button button_UpdateThisUser = new Button("Account Update");
		
	// This is a separator and it is used to partition the GUI for various tasks
	protected static Line line_Separator1 = new Line(20, 95, width-20, 95);

	// GUI ARea 2: This is a stub, so there are no widgets here.  For an actual role page, this are
	// would contain the widgets needed for the user to play the assigned role.
	
	
	
	// This is a separator and it is used to partition the GUI for various tasks
	protected static Line line_Separator4 = new Line(20, 525, width-20,525);
	
	// GUI Area 3: This is last of the GUI areas.  It is used for quitting the application and for
	// logging out.
	protected static Button button_Logout = new Button("Logout");
	protected static Button button_Quit = new Button("Quit");
	
	// This is the end of the GUI objects for the page.
	
	// These attributes are used to configure the page and populate it with this user's information
	private static ViewRole2Home theView;		// Used to determine if instantiation of the class
												// is needed

	// Reference for the in-memory database so this package has access
	private static Database theDatabase = applicationMain.FoundationsMain.database;

	protected static Stage theStage;			// The Stage that JavaFX has established for us	
	protected static Pane theRootPane;			// The Pane that holds all the GUI widgets
	protected static User theUser;				// The current logged in User
	
	private static Scene theRole2HomeScene;		// The shared Scene each invocation populates
	protected static final int theRole = 3;		// Admin: 1; Role1: 2; Role2: 3

	/*-*******************************************************************************************

	Constructors
	
	 */

	/**********
	 * <p> Method: displayRole2Home(Stage ps, User user) </p>
	 * 
	 * <p> Description: This method is the single entry point from outside this package to cause
	 * the Role2 Home page to be displayed.
	 * 
	 * It first sets up every shared attributes so we don't have to pass parameters.
	 * 
	 * It then checks to see if the page has been setup.  If not, it instantiates the class, 
	 * initializes all the static aspects of the GIUI widgets (e.g., location on the page, font,
	 * size, and any methods to be performed).
	 * 
	 * After the instantiation, the code then populates the elements that change based on the user
	 * and the system's current state.  It then sets the Scene onto the stage, and makes it visible
	 * to the user.
	 * 
	 * @param ps specifies the JavaFX Stage to be used for this GUI and it's methods
	 * 
	 * @param user specifies the User for this GUI and it's methods
	 * 
	 */
	public static void displayRole2Home(Stage ps, User user) {
		
		// Establish the references to the GUI and the current user
		theStage = ps;
		theUser = user;
		
		// If not yet established, populate the static aspects of the GUI
		if (theView == null) theView = new ViewRole2Home();		// Instantiate singleton if needed
		
		// Populate the dynamic aspects of the GUI with the data from the user and the current
		// state of the system.
		theDatabase.getUserAccountDetails(user.getUserName());
		applicationMain.FoundationsMain.activeHomePage = theRole;
		
		label_UserDetails.setText("User: " + theUser.getUserName());// Set the username

		// Set the title for the window, display the page, and wait for the Admin to do something
		theStage.setTitle("CSE 360 Foundations: Role2 Home Page");
		theStage.setScene(theRole2HomeScene);						// Set this page onto the stage
		theStage.show();											// Display it to the user
	}
	
	/**********
	 * <p> Method: ViewRole2Home() </p>
	 * 
	 * <p> Description: This method initializes all the elements of the graphical user interface.
	 * This method determines the location, size, font, color, and change and event handlers for
	 * each GUI object.
	 * 
	 * This is a singleton and is only performed once.  Subsequent uses fill in the changeable
	 * fields using the displayRole2Home method.</p>
	 * 
	 */
	private ViewRole2Home() {
		
		// Create the Pane for the list of widgets and the Scene for the window
		theRootPane = new Pane();
		theRole2HomeScene = new Scene(theRootPane, width, height);	// Create the scene
		
		// Set the title for the window
		
		// Populate the window with the title and other common widgets and set their static state
		
		// GUI Area 1
		label_PageTitle.setText("Role2 Home Page");
		setupLabelUI(label_PageTitle, "Arial", 28, width, Pos.CENTER, 0, 5);

		label_UserDetails.setText("User: " + theUser.getUserName());
		setupLabelUI(label_UserDetails, "Arial", 20, width, Pos.BASELINE_LEFT, 20, 55);
		
		setupButtonUI(button_UpdateThisUser, "Dialog", 18, 170, Pos.CENTER, 610, 45);
		button_UpdateThisUser.setOnAction((_) -> {ControllerRole2Home.performUpdate(); });
		
		// GUI Area 2
		
			// This is a stub, so this area is empty
		
		
		// GUI Area 3
        setupButtonUI(button_Logout, "Dialog", 18, 250, Pos.CENTER, 20, 540);
        button_Logout.setOnAction((_) -> {ControllerRole2Home.performLogout(); });
        
        setupButtonUI(button_Quit, "Dialog", 18, 250, Pos.CENTER, 300, 540);
        button_Quit.setOnAction((_) -> {ControllerRole2Home.performQuit(); });
        
        // ==================================================================================
 		// TP3 STAFF DASHBOARD LAUNCHER
 		// ==================================================================================
 		Button btnLaunchDash = new Button("Launch TP3 Staff Dashboard");
 		btnLaunchDash.setLayoutX(300); // Safely centered
 		btnLaunchDash.setLayoutY(380); // Safely below all Phase 2 template lines
 		btnLaunchDash.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
 		btnLaunchDash.setOnAction(e -> launchDashboardWindow());

		// This is the end of the GUI initialization code
		
		// Place all of the widget items into the Root Pane's list of children
        theRootPane.getChildren().addAll(
			label_PageTitle, label_UserDetails, button_UpdateThisUser, line_Separator1,
	        line_Separator4, button_Logout, button_Quit, btnLaunchDash);
	}
	
	
	/*-********************************************************************************************

	Helper methods to reduce code length

	 */
	
	/**********
	 * Private local method to initialize the standard fields for a label
	 * 
	 * @param l		The Label object to be initialized
	 * @param ff	The font to be used
	 * @param f		The size of the font to be used
	 * @param w		The width of the Button
	 * @param p		The alignment (e.g. left, centered, or right)
	 * @param x		The location from the left edge (x axis)
	 * @param y		The location from the top (y axis)
	 */
	private static void setupLabelUI(Label l, String ff, double f, double w, Pos p, double x, 
			double y){
		l.setFont(Font.font(ff, f));
		l.setMinWidth(w);
		l.setAlignment(p);
		l.setLayoutX(x);
		l.setLayoutY(y);		
	}
	
	
	/**********
	 * Private local method to initialize the standard fields for a button
	 * 
	 * @param b		The Button object to be initialized
	 * @param ff	The font to be used
	 * @param f		The size of the font to be used
	 * @param w		The width of the Button
	 * @param p		The alignment (e.g. left, centered, or right)
	 * @param x		The location from the left edge (x axis)
	 * @param y		The location from the top (y axis)
	 */
	private static void setupButtonUI(Button b, String ff, double f, double w, Pos p, double x, 
			double y){
		b.setFont(Font.font(ff, f));
		b.setMinWidth(w);
		b.setAlignment(p);
		b.setLayoutX(x);
		b.setLayoutY(y);		
	}
	
	/**********
	 * <p> Method: launchDashboardWindow </p>
	 * <p> Description: Generates a dedicated, isolated pop-up window for the TP3 Staff features 
	 * to prevent overlapping with the user details layout. </p>
	 */
	private void launchDashboardWindow() {
		Stage dashStage = new Stage();
		dashStage.setTitle("Team 9 - TP3 Staff Dashboard");
		Pane dp = new Pane();
		
		Label lblTitle = new Label("Grading & Moderation Dashboard");
		lblTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
		lblTitle.setLayoutX(20); lblTitle.setLayoutY(20);
		
		// --- 1. Rule of 3 Verification ---
		TextField tfUser = new TextField();
		tfUser.setPromptText("Target Username");
		tfUser.setLayoutX(20); tfUser.setLayoutY(60); tfUser.setPrefWidth(150);
		
		Label lblEvalResult = new Label("Ready for evaluation.");
		lblEvalResult.setLayoutX(300); lblEvalResult.setLayoutY(65);
		
		Button btnEval = new Button("Verify Rule of 3");
		btnEval.setLayoutX(180); btnEval.setLayoutY(60);
		btnEval.setOnAction(e -> ControllerRole2Home.handleEvaluateStudent(tfUser.getText().trim(), lblEvalResult));
		
		// --- 2. Posts Viewer ---
		ListView<String> listPosts = new ListView<>();
		listPosts.setLayoutX(20); listPosts.setLayoutY(110); listPosts.setPrefSize(560, 240);
		
		Button btnRefresh = new Button("Refresh Board");
		btnRefresh.setLayoutX(20); btnRefresh.setLayoutY(360);
		btnRefresh.setOnAction(e -> ControllerRole2Home.refreshPostList(listPosts));
		
		// --- 3. Moderation Tools ---
		TextField tfPId = new TextField();
		tfPId.setPromptText("Post ID");
		tfPId.setLayoutX(20); tfPId.setLayoutY(410); tfPId.setPrefWidth(70);
		
		TextField tfComment = new TextField();
		tfComment.setPromptText("Internal staff comment...");
		tfComment.setLayoutX(100); tfComment.setLayoutY(410); tfComment.setPrefWidth(240);
		
		Label lblStatus = new Label("");
		lblStatus.setLayoutX(200); lblStatus.setLayoutY(450);
		
		Button btnSave = new Button("Save Note");
		btnSave.setLayoutX(350); btnSave.setLayoutY(410);
		btnSave.setOnAction(e -> {
			try {
				int pid = Integer.parseInt(tfPId.getText().trim());
				ControllerRole2Home.handleSaveStaffComment(pid, tfComment.getText().trim(), lblStatus, listPosts);
			} catch (Exception ex) {
				lblStatus.setText("Error: Invalid numeric ID.");
				lblStatus.setStyle("-fx-text-fill: red;");
			}
		});
		
		CheckBox chkEndorse = new CheckBox("Instructor Endorsed");
		chkEndorse.setLayoutX(20); chkEndorse.setLayoutY(450);
		chkEndorse.setOnAction(e -> {
			try {
				int pid = Integer.parseInt(tfPId.getText().trim());
				ControllerRole2Home.handleToggleEndorsement(pid, chkEndorse.isSelected(), listPosts);
				lblStatus.setText("Endorsement updated.");
				lblStatus.setStyle("-fx-text-fill: green;");
			} catch (Exception ex) {
				lblStatus.setText("Error: Invalid numeric ID.");
				lblStatus.setStyle("-fx-text-fill: red;");
				chkEndorse.setSelected(!chkEndorse.isSelected()); // Revert toggle visually
			}
		});
		
		// Add all elements to the new pop-up Pane
		dp.getChildren().addAll(lblTitle, tfUser, btnEval, lblEvalResult,
				listPosts, btnRefresh, tfPId, tfComment, btnSave, chkEndorse, lblStatus);
		
		Scene scene = new Scene(dp, 600, 500);
		dashStage.setScene(scene);
		dashStage.show();
	}
}

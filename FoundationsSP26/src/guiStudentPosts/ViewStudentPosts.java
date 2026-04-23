package guiStudentPosts;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import entityClasses.Post;
import java.util.ArrayList;

/*******
 * <p> Title: ViewStudentPosts Class </p>
 *
 * <p> Description: The JavaFX View for the Student Discussion Board. Implements the full
 * student-facing UI for creating, viewing, searching, editing, soft-deleting, replying to,
 * and resolving discussion posts. Also supports a "My Posts" filter view. </p>
 *
 * <p> Follows the same singleton MVC pattern as the rest of the application. The static
 * displayStudentPosts() method is the single entry point from outside this package. </p>
 *
 * <p> Layout (fits within 800x600 window):
 * <ul>
 *   <li>Area 1 (y 0-90):   Page title, user label, account update button</li>
 *   <li>Area 2 (y 95-410):  Left: post list + search/filter buttons. Right: detail + replies</li>
 *   <li>Area 3 (y 418-505): Input panel — title, type, body fields</li>
 *   <li>Area 4 (y 510-550): Action buttons row</li>
 *   <li>Area 5 (y 560-595): Logout and Quit footer</li>
 * </ul>
 * </p>
 *
 * @author Prince Dahiya
 * @author Sumukh Gowda
 * @author Klim Savalia
 * @author Micah Branton
 * @author Huu Binh Vu
 *
 * @version 1.00    2026-03-21    Initial TP2 implementation
 * @version 1.01    2026-03-21    Added My Posts, QUESTION/STATEMENT types, unread counts,
 *                               soft-delete display, "Are you sure?" confirmation
 * @version 2.00	2026-04-22 	  Added TP3 integration (Home navigation, role-based delete actions, and Staff post types).
 */

/*******
 * <p> Title: ViewStudentPosts Class. </p>
 * 
 * <p> Description: The Java/FX-based Student Discussion Board. This class provides the UI
 * for creating, viewing, and replying to posts. </p>
 * 
 * <p> TP3 Architecture and Design: Serves as the Presentation Layer (View). For TP3, this class 
 * was modified to support cross-role integration by adding Home navigation, role-based visibility 
 * for Delete actions, and exposing Announcement/Note post types specifically for Staff/Admin users. 
 * This honors the system's access control requirements. </p>
 * 
 * <p> Tested by: Semi-automated tests in testing.TestStudentPosts and manual UI navigation tests documented in Manual Tests.pdf. </p>
 * 
 * <p> Copyright: Lynn Robert Carter © 2025 </p>
 * 
 * @author Lynn Robert Carter
 * @author Prince Dahiya
 * @author Micah Branton
 * @author Sumukh Gowda
 * @author Klim Savalia
 * 
 * @version 1.00    2025-03-01    Initial version for Phase 2
 * @version 2.00	2026-04-22    Added TP3 integration (Home navigation, role-based delete actions, and Staff post types).
 */
public class ViewStudentPosts {

    private static double width  = applicationMain.FoundationsMain.WINDOW_WIDTH;
    private static double height = applicationMain.FoundationsMain.WINDOW_HEIGHT;

    // --- Area 1: Header ---
    protected static Label  label_PageTitle   = new Label("Student Discussion Board");
    protected static Label  label_UserDetails = new Label();
    protected static Button button_UpdateUser = new Button("Account Update");
    protected static Line   line_Sep1         = new Line(20, 90, width - 20, 90);

    // --- Area 2 Left: post list + controls ---
    // Three buttons: Search, Show All, My Posts
    protected static TextField        text_Search    = new TextField();
    protected static Button           button_Search  = new Button("Search");
    protected static Button           button_ShowAll = new Button("All Posts");
    protected static Button           button_MyPosts = new Button("My Posts");
    protected static ListView<String> listView_Posts = new ListView<>();

    protected static Line line_VSep = new Line(375, 90, 375, 415);

    // --- Area 2 Right: detail view ---
    protected static Label            label_DetailTitle   = new Label("Select a post to view it");
    protected static TextArea         textArea_DetailBody = new TextArea();
    protected static Label            label_DetailMeta    = new Label();
    protected static Label            label_Resolved      = new Label();
    protected static Label            label_RepliesHeader = new Label("Replies:");
    protected static ListView<String> listView_Replies    = new ListView<>();

    protected static Line line_Sep2 = new Line(20, 415, width - 20, 415);

    // --- Area 3: Input fields ---
    protected static Label 			  label_TitleInput  = new Label("Title:");
    protected static TextField        text_TitleInput   = new TextField();
    protected static Label            label_TypeInput   = new Label("Type:");
    // Students post QUESTION or STATEMENT per the user stories
    protected static ComboBox<String> comboBox_PostType = new ComboBox<>();
    protected static Label            label_BodyInput   = new Label("Body:");
    protected static TextArea         textArea_BodyInput = new TextArea();
    protected static Label            label_ErrorMsg    = new Label();

    protected static Line line_Sep3 = new Line(20, 515, width - 20, 515);

    // --- Area 4: Action buttons ---
    protected static Button button_NewPost       = new Button("New Post");
    protected static Button button_Reply         = new Button("Reply");
    protected static Button button_EditPost      = new Button("Edit Post");
    protected static Button button_SaveEdit      = new Button("Save Edit");
    protected static Button button_Delete        = new Button("Delete");
    protected static Button button_ToggleResolve = new Button("Mark Resolved");
    protected static Button button_CancelEdit    = new Button("Cancel");

    protected static Line line_Sep4 = new Line(20, 558, width - 20, 558);

    // --- Area 5: Footer ---
    protected static Button button_Home   = new Button("Home");
    protected static Button button_Logout = new Button("Logout");
    protected static Button button_Quit   = new Button("Quit");

    // --- Singleton + shared state ---
    private static ViewStudentPosts 	theView;
    private static Scene            	theScene;
    protected static Stage              theStage;
    protected static Pane               theRootPane;
    protected static entityClasses.User theUser;
    protected static Post               selectedPost   = null;
    protected static ArrayList<Post>    displayedPosts = new ArrayList<>();
    protected static boolean            editMode          = false;
    protected static boolean 			isStaffOrAdmin = false; // Tracks if Role2/Admin is viewing
    // Guard flag: true while setItems() is running so the listener does not fire mid-swap
    protected static boolean            suppressSelection = false;


    /*******
     * <p> Method: displayStudentPosts(Stage, User) </p>
     * <p> Description: Single entry point from outside this package. Instantiates
     * the singleton if needed, refreshes the post list, and shows the scene. </p>
     *
     * @param ps   The JavaFX Stage
     * @param user The currently logged-in User
     */
    public static void displayStudentPosts(Stage ps, entityClasses.User user) {
        theStage = ps;
        theUser  = user;
        if (theView == null) theView = new ViewStudentPosts();

        // --- ROLE CHECK ---
        String prefix = "Student";
        isStaffOrAdmin = false;
        
        if (theUser != null) {
            if (theUser.getAdminRole()) { 
                prefix = "Admin"; 
                isStaffOrAdmin = true; 
            } else if (theUser.getNewRole2()) { 
                prefix = "Staff"; 
                isStaffOrAdmin = true; 
            }
        }
        
        label_UserDetails.setText(prefix + ": " + theUser.getUserName());
        
        // Add ANNOUNCEMENT and NOTE types if Staff/Admin
        if (isStaffOrAdmin) {
            comboBox_PostType.setItems(FXCollections.observableArrayList("QUESTION", "STATEMENT", "ANNOUNCEMENT", "NOTE"));
        } else {
            comboBox_PostType.setItems(FXCollections.observableArrayList("QUESTION", "STATEMENT"));
        }
        comboBox_PostType.setValue("QUESTION");
        
        ControllerStudentPosts.loadAllPosts();
        setEditMode(false);
        clearInputFields();
        label_ErrorMsg.setText("");

        theStage.setTitle("CSE 360: Student Discussion Board");
        theStage.setScene(theScene);
        theStage.show();
    }


    /*******
     * <p> Method: ViewStudentPosts() </p>
     * <p> Description: Private constructor — sets up all widgets and wires event handlers.
     * Only runs once due to the singleton pattern. </p>
     */
    private ViewStudentPosts() {
        theRootPane = new Pane();
        theScene    = new Scene(theRootPane, width, height);

        // ---- Area 1: Header ----
        setupLabelUI(label_PageTitle,   "Arial", 24, width, Pos.CENTER,        0,  5);
        setupLabelUI(label_UserDetails, "Arial", 14, 500,   Pos.BASELINE_LEFT, 20, 55);
        setupButtonUI(button_UpdateUser, "Dialog", 13, 150, Pos.CENTER,       630, 48);
        button_UpdateUser.setOnAction((_) -> ControllerStudentPosts.performUpdate());

        // ---- Area 2 Left: search + filter buttons + list ----
        text_Search.setLayoutX(20);  text_Search.setLayoutY(97);
        text_Search.setPrefWidth(155); text_Search.setPromptText("Search...");

        // Search=65px, All Posts=60px, My Posts=60px — all fit inside the 375px divider
        setupButtonUI(button_Search,  "Dialog", 11, 65, Pos.CENTER, 178, 97);
        setupButtonUI(button_ShowAll, "Dialog", 11, 60, Pos.CENTER, 248, 97);
        setupButtonUI(button_MyPosts, "Dialog", 11, 60, Pos.CENTER, 313, 97);

        button_Search.setOnAction((_)  -> ControllerStudentPosts.performSearch());
        button_ShowAll.setOnAction((_) -> ControllerStudentPosts.loadAllPosts());
        button_MyPosts.setOnAction((_) -> ControllerStudentPosts.loadMyPosts());

        listView_Posts.setLayoutX(20);
        listView_Posts.setLayoutY(127);
        listView_Posts.setPrefSize(350, 283);
        listView_Posts.getSelectionModel().selectedIndexProperty().addListener(
            (obs, oldVal, newVal) -> { if (!suppressSelection) ControllerStudentPosts.performSelectPost(newVal.intValue()); }
        );

        // ---- Area 2 Right: detail view + replies ----
        setupLabelUI(label_DetailTitle, "Arial", 14, 390, Pos.BASELINE_LEFT, 385, 97);
        label_DetailTitle.setWrapText(true);

        textArea_DetailBody.setLayoutX(385); textArea_DetailBody.setLayoutY(118);
        textArea_DetailBody.setPrefSize(390, 110);
        textArea_DetailBody.setEditable(false);
        textArea_DetailBody.setWrapText(true);

        setupLabelUI(label_DetailMeta, "Arial", 10, 390, Pos.BASELINE_LEFT, 385, 232);
        setupLabelUI(label_Resolved,   "Arial", 11, 200, Pos.BASELINE_LEFT, 385, 248);
        label_Resolved.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");

        setupLabelUI(label_RepliesHeader, "Arial", 12, 100, Pos.BASELINE_LEFT, 385, 265);
        listView_Replies.setLayoutX(385);
        listView_Replies.setLayoutY(283);
        listView_Replies.setPrefSize(390, 127);

        // ---- Area 3: Input fields ----
        setupLabelUI(label_TitleInput, "Dialog", 12, 35, Pos.BASELINE_LEFT, 20,  420);
        text_TitleInput.setLayoutX(58);   text_TitleInput.setLayoutY(417);
        text_TitleInput.setPrefWidth(270);

        setupLabelUI(label_TypeInput, "Dialog", 12, 35, Pos.BASELINE_LEFT, 335, 420);
        comboBox_PostType.setItems(FXCollections.observableArrayList("QUESTION", "STATEMENT"));
        comboBox_PostType.setValue("QUESTION");
        comboBox_PostType.setLayoutX(372); comboBox_PostType.setLayoutY(417);
        comboBox_PostType.setPrefWidth(130);

        setupLabelUI(label_BodyInput, "Dialog", 12, 35, Pos.BASELINE_LEFT, 20, 450);
        textArea_BodyInput.setLayoutX(58);   textArea_BodyInput.setLayoutY(447);
        textArea_BodyInput.setPrefSize(720, 52);
        textArea_BodyInput.setWrapText(true);

        // Error label below the body field - full width so it never clips
        setupLabelUI(label_ErrorMsg, "Arial", 11, width - 40, Pos.BASELINE_LEFT, 20, 503);
        label_ErrorMsg.setStyle("-fx-text-fill: red;");

        // ---- Area 4: Action buttons ----
        setupButtonUI(button_NewPost,       "Dialog", 12, 100, Pos.CENTER,  20, 520);
        setupButtonUI(button_Reply,         "Dialog", 12, 100, Pos.CENTER, 128, 520);
        setupButtonUI(button_EditPost,      "Dialog", 12, 100, Pos.CENTER, 236, 520);
        setupButtonUI(button_SaveEdit,      "Dialog", 12, 100, Pos.CENTER, 344, 520);
        setupButtonUI(button_Delete,        "Dialog", 12, 100, Pos.CENTER, 452, 520);
        setupButtonUI(button_ToggleResolve, "Dialog", 12, 125, Pos.CENTER, 560, 520);
        setupButtonUI(button_CancelEdit,    "Dialog", 12, 100, Pos.CENTER, 693, 520);

        button_NewPost.setOnAction((_)       -> ControllerStudentPosts.performNewPost());
        button_Reply.setOnAction((_)         -> ControllerStudentPosts.performReply());
        button_EditPost.setOnAction((_)      -> ControllerStudentPosts.performEditPost());
        button_SaveEdit.setOnAction((_)      -> ControllerStudentPosts.performSaveEdit());
        //button_Delete.setOnAction((_)        -> ControllerStudentPosts.performDelete());
        button_ToggleResolve.setOnAction((_) -> ControllerStudentPosts.performToggleResolve());
        button_CancelEdit.setOnAction((_)    -> ControllerStudentPosts.performCancelEdit());
        
        button_Delete.setOnAction((_) -> {
            if (selectedPost != null && !selectedPost.getAuthorUsername().equals(theUser.getUserName())) {
                if (isStaffOrAdmin) {
                    label_ErrorMsg.setText("You can only delete your own post. To delete another student's post, please use the Staff Dashboard.");
                    return; // Stop the deletion
                }
            }
            ControllerStudentPosts.performDelete();
        });

        // ---- Area 5: Footer ----
        setupButtonUI(button_Home,   "Dialog", 13, 140, Pos.CENTER,  20, 563);
        setupButtonUI(button_Logout, "Dialog", 13, 140, Pos.CENTER, 170, 563);
        setupButtonUI(button_Quit,   "Dialog", 13, 140, Pos.CENTER, 320, 563);
        
        button_Home.setOnAction((_)   -> ControllerStudentPosts.performHome());
        button_Logout.setOnAction((_) -> ControllerStudentPosts.performLogout());
        button_Quit.setOnAction((_)   -> ControllerStudentPosts.performQuit());

        theRootPane.getChildren().addAll(
            label_PageTitle, label_UserDetails, button_UpdateUser, line_Sep1,
            text_Search, button_Search, button_ShowAll, button_MyPosts, listView_Posts,
            line_VSep,
            label_DetailTitle, textArea_DetailBody, label_DetailMeta, label_Resolved,
            label_RepliesHeader, listView_Replies,
            line_Sep2,
            label_TitleInput, text_TitleInput, label_TypeInput, comboBox_PostType,
            label_BodyInput, textArea_BodyInput, label_ErrorMsg,
            line_Sep3,
            button_NewPost, button_Reply, button_EditPost, button_SaveEdit,
            button_Delete, button_ToggleResolve, button_CancelEdit,
            line_Sep4,
            button_Home, button_Logout, button_Quit
        );
    }


    // --- Methods called by the Controller to update the UI ---

    /*******
     * <p> Method: populatePostList(ArrayList, String) </p>
     * <p> Description: Fills the post list using display strings built by the Model.
     * Each string includes the post type, title, reply count, and unread count. </p>
     *
     * @param posts    The list of posts to display
     * @param username The logged-in user (for unread count calculation)
     */
    protected static void populatePostList(ArrayList<Post> posts, String username) {
        displayedPosts = posts;
        ModelStudentPosts model = new ModelStudentPosts();
        ObservableList<String> items = FXCollections.observableArrayList();
        for (Post p : posts)
            items.add(model.buildDisplayString(p, username));
        // Suppress listener during setItems - JavaFX fires it mid-swap when list size
        // is 0, causing IndexOutOfBoundsException. Flag blocks it, re-enables after.
        suppressSelection = true;
        listView_Posts.setItems(items);
        suppressSelection = false;
        // NOTE: clearDetailView() intentionally NOT called here.
        // populatePostList is also used by refreshCurrentList() which runs after
        // a post is selected - calling clearDetailView() here would wipe the detail panel.
        // Callers that need a full reset (loadAllPosts, search, myPosts) call clearDetailView() themselves.
    }

    /*******
     * <p> Method: showPostDetail(Post, ArrayList) </p>
     * <p> Description: Populates the right-side detail panel. If the post is soft-deleted,
     * shows a deletion notice instead of the original content. Replies still show. </p>
     *
     * @param post    The post to display
     * @param replies The replies for this post
     */
    protected static void showPostDetail(Post post, ArrayList<Post> replies) {
        selectedPost = post;

        if (post.isDeleted()) {
            // Soft-deleted: show title but replace body with deletion notice per spec
            label_DetailTitle.setText("[DELETED] " + post.getTitle());
            textArea_DetailBody.setText("This post has been deleted by the author.");
            label_DetailMeta.setText("By: " + post.getAuthorUsername()
                    + "  |  " + post.getTimestamp());
            label_Resolved.setText("");
            // Disable edit/delete/resolve for deleted posts
            button_EditPost.setDisable(true);
            button_Delete.setDisable(true);
            button_ToggleResolve.setVisible(false);
            button_CancelEdit.setLayoutX(693);
        } else {
            label_DetailTitle.setText(post.getTitle());
            textArea_DetailBody.setText(post.getBody());
            // 1. The base string that everyone is allowed to see
            String metaText = "By: " + post.getAuthorUsername()
                    + "  |  " + post.getTimestamp() + "  |  " + post.getPostType()
                    + "  |  Thread: " + post.getThread();
            
            // 2. Safely append the staff comment ONLY if they have the right role
            // NOTE: Replace 'userHasStaffPrivileges()' with your actual role-check method!
            if (!post.getStaffComment().isEmpty() && isStaffOrAdmin) {
                metaText += "\nStaff: " + post.getStaffComment();
            }
            
            // 3. Set the text
            label_DetailMeta.setText(metaText);
            label_Resolved.setText(post.isResolved() ? "✓ Resolved" : "");

            // Only show resolve button for QUESTION posts
            boolean isQuestion = post.getPostType().equals("QUESTION");
            button_ToggleResolve.setVisible(isQuestion);
            button_ToggleResolve.setText(post.isResolved() ? "Reopen" : "Mark Resolved");
            button_CancelEdit.setLayoutX(isQuestion ? 693 : 560);

            button_EditPost.setDisable(false);
            button_Delete.setDisable(false);
        }

        // Populate replies - show a note on replies if the parent was deleted
        ObservableList<String> replyItems = FXCollections.observableArrayList();
        for (Post r : replies)
            replyItems.add(r.getAuthorUsername() + " (" + r.getTimestamp() + "): " + r.getBody());
        listView_Replies.setItems(replyItems);
    }

    /*******
     * <p> Method: setEditMode(boolean) </p>
     * <p> Description: Switches between edit mode (Save/Cancel active, others disabled)
     * and normal view mode. </p>
     * @param editing True for edit mode, false for normal mode
     */
    protected static void setEditMode(boolean editing) {
        editMode = editing;
        button_SaveEdit.setDisable(!editing);
        button_CancelEdit.setDisable(!editing);
        button_NewPost.setDisable(editing);
        button_Reply.setDisable(editing);
        button_EditPost.setDisable(editing);
    }

    /*******
     * <p> Method: clearDetailView() </p>
     * <p> Description: Resets the right-side detail panel to empty state. </p>
     */
    protected static void clearDetailView() {
        selectedPost = null;
        label_DetailTitle.setText("Select a post to view it");
        textArea_DetailBody.setText("");
        label_DetailMeta.setText("");
        label_Resolved.setText("");
        listView_Replies.setItems(FXCollections.observableArrayList());
        button_ToggleResolve.setText("Mark Resolved");
        button_ToggleResolve.setVisible(true);
        button_CancelEdit.setLayoutX(693);
        button_EditPost.setDisable(false);
        button_Delete.setDisable(false);
    }

    /*******
     * <p> Method: clearInputFields() </p>
     * <p> Description: Clears the title, body, and resets type to QUESTION. </p>
     */
    protected static void clearInputFields() {
        text_TitleInput.setText("");
        textArea_BodyInput.setText("");
        comboBox_PostType.setValue("QUESTION");
    }


    // --- Standard UI setup helpers ---

    private static void setupLabelUI(Label l, String ff, double f, double w,
                                     Pos p, double x, double y) {
        l.setFont(Font.font(ff, f));
        l.setMinWidth(w);
        l.setAlignment(p);
        l.setLayoutX(x);
        l.setLayoutY(y);
    }

    private static void setupButtonUI(Button b, String ff, double f, double w,
                                      Pos p, double x, double y) {
        b.setFont(Font.font(ff, f));
        b.setMinWidth(w);
        b.setAlignment(p);
        b.setLayoutX(x);
        b.setLayoutY(y);
    }
}
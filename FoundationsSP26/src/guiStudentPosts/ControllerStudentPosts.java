package guiStudentPosts;

import entityClasses.Post;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.util.ArrayList;
import java.util.Optional;

/*******
 * <p> Title: ControllerStudentPosts Class </p>
 *
 * <p> Description: The Controller for the Student Discussion Board. Responds to button
 * clicks and list selections from the View, delegates business logic to the Model, and
 * updates the View with results. </p>
 *
 * <p> Like other controllers in this project, this class is not instantiated — it's a
 * collection of protected static methods. Do not change these to public. </p>
 *
 * @author Prince Dahiya
 * @author Sumukh Gowda
 * @author Klim Savalia
 * @author Micah Branton
 * @author Huu Binh Vu
 *
 * @version 1.00    2026-03-21    Initial TP2 implementation
 * @version 1.01    2026-03-21    Added "Are you sure?" dialog, read tracking, My Posts
 */
public class ControllerStudentPosts {

    /** Default constructor not used — all methods are static. */
    public ControllerStudentPosts() {}

    private static ModelStudentPosts theModel = new ModelStudentPosts();


    /*******
     * <p> Method: loadAllPosts() </p>
     * <p> Description: Fetches all posts from the Model and populates the list view.
     * Called on page load and after any create/edit/delete. </p>
     */
    protected static void loadAllPosts() {
        ArrayList<Post> posts = theModel.getAllPosts();
        String username = ViewStudentPosts.theUser.getUserName();
        ViewStudentPosts.clearDetailView(); // full reload — reset detail panel
        ViewStudentPosts.populatePostList(posts, username);
        ViewStudentPosts.label_ErrorMsg.setText("");
    }


    /*******
     * <p> Method: loadMyPosts() </p>
     * <p> Description: Filters the list to show only the logged-in student's own posts,
     * per the user story: "I can see a list of my posts, the number of replies, and how
     * many of them I have not yet read." </p>
     */
    protected static void loadMyPosts() {
        String username = ViewStudentPosts.theUser.getUserName();
        ArrayList<Post> posts = theModel.getMyPosts(username);
        ViewStudentPosts.clearDetailView(); // filter change — reset detail panel
        ViewStudentPosts.populatePostList(posts, username);

        if (posts.isEmpty())
            ViewStudentPosts.label_ErrorMsg.setText("You haven't made any posts yet.");
        else
            ViewStudentPosts.label_ErrorMsg.setText("Showing your posts (" + posts.size() + ")");
    }


    /*******
     * <p> Method: performSearch() </p>
     * <p> Description: Filters the post list by keyword. Shows all posts if empty. </p>
     */
    protected static void performSearch() {
        String keyword  = ViewStudentPosts.text_Search.getText();
        String username = ViewStudentPosts.theUser.getUserName();
        ArrayList<Post> results = theModel.searchPosts(keyword);
        ViewStudentPosts.clearDetailView(); // new search — reset detail panel
        ViewStudentPosts.populatePostList(results, username);

        if (results.isEmpty())
            ViewStudentPosts.label_ErrorMsg.setText("No posts found for: \"" + keyword + "\"");
        else
            ViewStudentPosts.label_ErrorMsg.setText("");
    }


    /*******
     * <p> Method: performSelectPost(int) </p>
     * <p> Description: Loads the selected post's details and marks it and its replies
     * as read for the logged-in user. New replies added after this point will show
     * as unread next time the student views the list. </p>
     *
     * @param index The selected index in the list (-1 means nothing selected)
     */
    protected static void performSelectPost(int index) {
        if (index < 0 || index >= ViewStudentPosts.displayedPosts.size()) return;

        Post selected   = ViewStudentPosts.displayedPosts.get(index);
        String username = ViewStudentPosts.theUser.getUserName();
        ArrayList<Post> replies = theModel.getRepliesForPost(selected.getId());

        ViewStudentPosts.showPostDetail(selected, replies);
        ViewStudentPosts.setEditMode(false);
        ViewStudentPosts.label_ErrorMsg.setText("");

        // Mark this post and all current replies as read for this user
        theModel.markPostAndRepliesRead(selected, username);

        // Refresh the list display so unread badges update immediately
        // Defer the list refresh until after the mouse click event is fully processed.
        // Calling setItems() mid-click crashes JavaFXs internal ListViewBehavior.
        Platform.runLater(() -> refreshCurrentList());
    }


    /*******
     * <p> Method: performNewPost() </p>
     * <p> Description: Creates a new top-level post from the input fields. Shows an
     * error if validation fails, refreshes the list on success. </p>
     */
    protected static void performNewPost() {
        String title    = ViewStudentPosts.text_TitleInput.getText();
        String body     = ViewStudentPosts.textArea_BodyInput.getText();
        String type     = (String) ViewStudentPosts.comboBox_PostType.getValue();
        String author   = ViewStudentPosts.theUser.getUserName();

        String error = theModel.createPost(title, body, author, type, Post.NO_PARENT);
        if (!error.isEmpty()) {
            ViewStudentPosts.label_ErrorMsg.setText(error);
            return;
        }

        ViewStudentPosts.clearInputFields();
        ViewStudentPosts.label_ErrorMsg.setText("");
        loadAllPosts();
    }


    /*******
     * <p> Method: performReply() </p>
     * <p> Description: Creates a reply to the currently selected post. Uses the body
     * input field for the reply text. Auto-sets title to "Re: [parent title]". </p>
     */
    protected static void performReply() {
        if (ViewStudentPosts.selectedPost == null) {
            ViewStudentPosts.label_ErrorMsg.setText("Please select a post to reply to.");
            return;
        }
        if (ViewStudentPosts.selectedPost.isDeleted()) {
            ViewStudentPosts.label_ErrorMsg.setText("Cannot reply to a deleted post.");
            return;
        }

        String body   = ViewStudentPosts.textArea_BodyInput.getText();
        String author = ViewStudentPosts.theUser.getUserName();
        int    parent = ViewStudentPosts.selectedPost.getId();
        String replyTitle = "Re: " + ViewStudentPosts.selectedPost.getTitle();

        // Replies are always STATEMENT type
        String error = theModel.createPost(replyTitle, body, author, "STATEMENT", parent);
        if (!error.isEmpty()) {
            ViewStudentPosts.label_ErrorMsg.setText(error);
            return;
        }

        ViewStudentPosts.clearInputFields();
        ViewStudentPosts.label_ErrorMsg.setText("");

        // Refresh the replies in the detail view
        ArrayList<Post> replies = theModel.getRepliesForPost(parent);
        ViewStudentPosts.showPostDetail(ViewStudentPosts.selectedPost, replies);
    }


    /*******
     * <p> Method: performEditPost() </p>
     * <p> Description: Switches to edit mode and pre-fills the input fields with the
     * selected post's current values. </p>
     */
    protected static void performEditPost() {
        if (ViewStudentPosts.selectedPost == null) {
            ViewStudentPosts.label_ErrorMsg.setText("Please select a post to edit.");
            return;
        }
        ViewStudentPosts.text_TitleInput.setText(ViewStudentPosts.selectedPost.getTitle());
        ViewStudentPosts.textArea_BodyInput.setText(ViewStudentPosts.selectedPost.getBody());
        ViewStudentPosts.comboBox_PostType.setValue(ViewStudentPosts.selectedPost.getPostType());
        ViewStudentPosts.setEditMode(true);
        ViewStudentPosts.label_ErrorMsg.setText("Edit the fields above and click Save Edit.");
    }


    /*******
     * <p> Method: performSaveEdit() </p>
     * <p> Description: Saves the edited title and body. Re-selects the post after refresh
     * so the detail view doesn't disappear. </p>
     */
    protected static void performSaveEdit() {
        if (ViewStudentPosts.selectedPost == null) return;

        String newTitle  = ViewStudentPosts.text_TitleInput.getText();
        String newBody   = ViewStudentPosts.textArea_BodyInput.getText();
        String loggedIn  = ViewStudentPosts.theUser.getUserName();

        String error = theModel.updatePost(
                ViewStudentPosts.selectedPost.getId(), newTitle, newBody, loggedIn);
        if (!error.isEmpty()) {
            ViewStudentPosts.label_ErrorMsg.setText(error);
            return;
        }

        // Remember which post was selected so we can re-select after refresh
        int savedId = ViewStudentPosts.selectedPost.getId();

        ViewStudentPosts.setEditMode(false);
        ViewStudentPosts.clearInputFields();
        ViewStudentPosts.label_ErrorMsg.setText("");
        loadAllPosts();

        // Re-select the same post so the detail view stays populated
        for (int i = 0; i < ViewStudentPosts.displayedPosts.size(); i++) {
            if (ViewStudentPosts.displayedPosts.get(i).getId() == savedId) {
                ViewStudentPosts.listView_Posts.getSelectionModel().select(i);
                performSelectPost(i);
                break;
            }
        }
    }


    /*******
     * <p> Method: performDelete() </p>
     * <p> Description: Shows an "Are you sure?" confirmation dialog before soft-deleting
     * the post. Replies are NOT deleted — they remain visible with a deletion notice per
     * the user story. Clears input fields if the user was mid-edit. </p>
     */
    protected static void performDelete() {
        if (ViewStudentPosts.selectedPost == null) {
            ViewStudentPosts.label_ErrorMsg.setText("Please select a post to delete.");
            return;
        }

        // "Are you sure?" dialog as required by the user story
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Post");
        confirm.setHeaderText("Are you sure?");
        confirm.setContentText("This will delete your post. Any replies will remain visible.");
        Optional<ButtonType> result = confirm.showAndWait();

        // If user didn't click OK, do nothing
        if (result.isEmpty() || result.get() != ButtonType.OK) return;

        String loggedIn = ViewStudentPosts.theUser.getUserName();
        String error = theModel.deletePost(
                ViewStudentPosts.selectedPost.getId(), loggedIn);
        if (!error.isEmpty()) {
            ViewStudentPosts.label_ErrorMsg.setText(error);
            return;
        }

        // Clear everything including any half-filled edit fields
        ViewStudentPosts.setEditMode(false);
        ViewStudentPosts.clearInputFields();
        ViewStudentPosts.clearDetailView();
        ViewStudentPosts.label_ErrorMsg.setText("");
        loadAllPosts();
    }


    /*******
     * <p> Method: performToggleResolve() </p>
     * <p> Description: Toggles the resolved status of the selected QUESTION post.
     * Only the author can do this. Re-selects the post after refresh. </p>
     */
    protected static void performToggleResolve() {
        if (ViewStudentPosts.selectedPost == null) {
            ViewStudentPosts.label_ErrorMsg.setText("Please select a post first.");
            return;
        }

        String loggedIn = ViewStudentPosts.theUser.getUserName();
        String error = theModel.toggleResolved(
                ViewStudentPosts.selectedPost.getId(), loggedIn);
        if (!error.isEmpty()) {
            ViewStudentPosts.label_ErrorMsg.setText(error);
            return;
        }

        int savedId = ViewStudentPosts.selectedPost.getId();
        ViewStudentPosts.label_ErrorMsg.setText("");
        loadAllPosts();

        // Re-select so the resolved badge updates in the detail view
        for (int i = 0; i < ViewStudentPosts.displayedPosts.size(); i++) {
            if (ViewStudentPosts.displayedPosts.get(i).getId() == savedId) {
                ViewStudentPosts.listView_Posts.getSelectionModel().select(i);
                performSelectPost(i);
                break;
            }
        }
    }


    /*******
     * <p> Method: performCancelEdit() </p>
     * <p> Description: Exits edit mode without saving. Clears input fields. </p>
     */
    protected static void performCancelEdit() {
        ViewStudentPosts.setEditMode(false);
        ViewStudentPosts.clearInputFields();
        ViewStudentPosts.label_ErrorMsg.setText("");
    }


    /*******
     * <p> Method: refreshCurrentList() </p>
     * <p> Description: Refreshes the list display without changing which posts are shown.
     * Called after marking posts as read so unread badges update immediately. </p>
     */
    private static void refreshCurrentList() {
        String username = ViewStudentPosts.theUser.getUserName();
        ViewStudentPosts.populatePostList(ViewStudentPosts.displayedPosts, username);
    }


    /*******
     * <p> Method: performUpdate() </p>
     * <p> Description: Navigates to the account update page. </p>
     */
    protected static void performUpdate() {
        guiUserUpdate.ViewUserUpdate.displayUserUpdate(
                ViewStudentPosts.theStage, ViewStudentPosts.theUser);
    }

    /*******
     * <p> Method: performLogout() </p>
     * <p> Description: Logs out and returns to the login page. </p>
     */
    protected static void performLogout() {
        guiUserLogin.ViewUserLogin.displayUserLogin(ViewStudentPosts.theStage);
    }

    /*******
     * <p> Method: performQuit() </p>
     * <p> Description: Terminates the application. </p>
     */
    protected static void performQuit() {
        System.exit(0);
    }
}
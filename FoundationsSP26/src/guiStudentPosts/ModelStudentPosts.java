package guiStudentPosts;

import java.sql.SQLException;
import java.util.ArrayList;
import entityClasses.Post;

/*******
 * <p> Title: ModelStudentPosts Class </p>
 *
 * <p> Description: The Model for the Student Discussion Board page. Handles all business
 * logic and database calls for student post CRUD operations, read tracking, and search.
 * The View and Controller never talk to the database directly — they go through here. </p>
 *
 * <p> <b>Testing:</b> The database interactions and state changes managed by this class 
 * are validated by the JUnit test suite in {@link testing.TestStudentPosts}. </p>
 *
 * @author Prince Dahiya
 * @author Sumukh Gowda
 * @author Klim Savalia
 * @author Micah Branton
 * @author Huu Binh Vu
 *
 * @version 1.00    2026-03-21    Initial TP2 implementation
 * @version 1.01    2026-03-21    Added read tracking, My Posts, soft delete, thread support
 */
public class ModelStudentPosts {

    private static database.Database theDatabase = applicationMain.FoundationsMain.database;


    /*******
     * <p> Method: createPost(String, String, String, String, int) </p>
     * <p> Description: Validates inputs and creates a new top-level post or reply.
     * Returns error message on failure, empty string on success. </p>
     *
     * @param title          The post title
     * @param body           The post body
     * @param authorUsername The logged-in student's username
     * @param postType       "QUESTION" or "STATEMENT"
     * @param parentPostId   Post.NO_PARENT for new thread, or a valid ID for a reply
     * @return Empty string if successful, error message otherwise
     */
    public String createPost(String title, String body, String authorUsername,
                             String postType, int parentPostId) {
        String titleError = Post.validateTitle(title);
        if (!titleError.isEmpty()) return titleError;

        String bodyError = Post.validateBody(body);
        if (!bodyError.isEmpty()) return bodyError;

        String typeError = Post.validatePostType(postType);
        if (!typeError.isEmpty()) return typeError;

        Post newPost = new Post(title, body, authorUsername, postType, parentPostId);
        try {
            theDatabase.createPost(newPost);
            return "";
        } catch (SQLException e) {
            e.printStackTrace();
            return "Database error: could not create post.";
        }
    }


    /*******
     * <p> Method: getAllPosts() </p>
     * <p> Description: Returns all top-level posts, newest first. </p>
     * @return ArrayList of Post objects, never null
     */
    public ArrayList<Post> getAllPosts() {
        return theDatabase.getAllPosts();
    }


    /*******
     * <p> Method: getMyPosts(String) </p>
     * <p> Description: Returns only the posts authored by the given user, newest first.
     * Powers the "My Posts" view per the user story. </p>
     * @param username The logged-in student's username
     * @return ArrayList of the student's own posts
     */
    public ArrayList<Post> getMyPosts(String username) {
        return theDatabase.getPostsByAuthor(username);
    }


    /*******
     * <p> Method: getRepliesForPost(int) </p>
     * <p> Description: Returns all replies for a post, oldest first. </p>
     * @param postId The parent post ID
     * @return ArrayList of reply Posts
     */
    public ArrayList<Post> getRepliesForPost(int postId) {
        return theDatabase.getRepliesForPost(postId);
    }


    /*******
     * <p> Method: searchPosts(String) </p>
     * <p> Description: Returns posts matching the keyword (case-insensitive). If keyword
     * is empty, returns all posts. </p>
     * @param keyword The search term
     * @return Matching posts, newest first
     */
    public ArrayList<Post> searchPosts(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return getAllPosts();
        return theDatabase.searchPosts(keyword.trim());
    }


    /*******
     * <p> Method: updatePost(int, String, String, String) </p>
     * <p> Description: Validates and updates a post's title and body. Checks the logged-in
     * user is the author and the post isn't deleted. </p>
     *
     * @param postId          The ID of the post to update
     * @param newTitle        The new title
     * @param newBody         The new body
     * @param loggedInUsername The currently logged-in student
     * @return Empty string if successful, error message otherwise
     */
    public String updatePost(int postId, String newTitle, String newBody,
                             String loggedInUsername) {
        Post existing = theDatabase.getPostById(postId);
        if (existing == null)    return "Post not found.";
        if (existing.isDeleted()) return "Cannot edit a deleted post.";
        if (!existing.getAuthorUsername().equals(loggedInUsername))
            return "You can only edit your own posts.";

        String titleError = Post.validateTitle(newTitle);
        if (!titleError.isEmpty()) return titleError;

        String bodyError = Post.validateBody(newBody);
        if (!bodyError.isEmpty()) return bodyError;

        theDatabase.updatePost(postId, newTitle, newBody);
        return "";
    }


    /*******
     * <p> Method: deletePost(int, String) </p>
     * <p> Description: Soft-deletes a post after verifying ownership. Replies are kept
     * per the user story — they stay visible with a deletion notice on the parent. </p>
     *
     * @param postId           The ID of the post to delete
     * @param loggedInUsername The currently logged-in student
     * @return Empty string if successful, error message otherwise
     */
    public String deletePost(int postId, String loggedInUsername) {
        Post existing = theDatabase.getPostById(postId);
        if (existing == null) return "Post not found.";
        if (!existing.getAuthorUsername().equals(loggedInUsername))
            return "You can only delete your own posts.";

        // Soft delete - sets isDeleted=TRUE, replies are NOT removed
        theDatabase.softDeletePost(postId);
        return "";
    }


    /*******
     * <p> Method: toggleResolved(int, String) </p>
     * <p> Description: Flips the resolved flag on a QUESTION post. Only the author can
     * do this, and the post must not be deleted. </p>
     *
     * @param postId           The post ID
     * @param loggedInUsername The currently logged-in student
     * @return Empty string if successful, error message otherwise
     */
    public String toggleResolved(int postId, String loggedInUsername) {
        Post existing = theDatabase.getPostById(postId);
        if (existing == null)     return "Post not found.";
        if (existing.isDeleted()) return "Cannot resolve a deleted post.";
        if (!existing.getAuthorUsername().equals(loggedInUsername))
            return "You can only mark your own posts as resolved.";

        theDatabase.markPostResolved(postId, !existing.isResolved());
        return "";
    }


    /*******
     * <p> Method: markPostAndRepliesRead(Post, String) </p>
     * <p> Description: Marks a post and all its current replies as read for the given
     * user. Called when a student selects a post. New replies added afterwards will
     * appear as unread next time the student views the list. </p>
     *
     * @param post     The post being viewed
     * @param username The logged-in student's username
     */
    public void markPostAndRepliesRead(Post post, String username) {
        theDatabase.markAsRead(post.getId(), username);
        for (Post reply : theDatabase.getRepliesForPost(post.getId()))
            theDatabase.markAsRead(reply.getId(), username);
    }


    /*******
     * <p> Method: buildDisplayString(Post, String) </p>
     * <p> Description: Builds the display string shown in the post ListView for a given
     * post. Shows resolved badge, post type, title, reply count, and unread count so
     * students can quickly see which posts need attention. </p>
     *
     * @param post     The post to build a display string for
     * @param username The logged-in user (needed for unread count)
     * @return A formatted string for the ListView item
     */
    public String buildDisplayString(Post post, String username) {
        if (post.isDeleted()) return "[DELETED] " + post.getTitle();

        String resolved  = post.isResolved()           ? "[✓] "  : "";
        String endorsed  = post.isInstructorEndorsed() ? " ★"    : "";
        String newBadge  = !theDatabase.isPostRead(post.getId(), username) ? " •NEW" : "";
        int replyCount   = theDatabase.getReplyCount(post.getId());
        int unreadCount  = theDatabase.getUnreadReplyCount(post.getId(), username);

        String countInfo = "";
        if (replyCount > 0)
            countInfo = " (" + replyCount + " repl" + (replyCount == 1 ? "y" : "ies")
                    + (unreadCount > 0 ? ", " + unreadCount + " new" : "") + ")";

        return resolved + "[" + post.getPostType() + "] "
                + post.getTitle() + endorsed + newBadge + countInfo;
    }
}
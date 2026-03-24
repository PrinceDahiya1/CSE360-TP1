package entityClasses;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/*******
 * <p> Title: Post Class </p>
 *
 * <p> Description: Represents a single discussion post in the Student Discussion System,
 * similar to a post on Ed Discussion. Handles all CRUD operations and input validation
 * for posts made by students. Located in entityClasses alongside the User class. </p>
 *
 * <p> Posts can be QUESTION or STATEMENT type. A post can also be a reply to another
 * post using parentPostId — this way we don't need a separate Reply class or table.
 * If parentPostId is -1 it's a top-level thread, otherwise it's a reply. </p>
 *
 * <p> Posts belong to a thread (defaults to "General"). Thread creation/deletion is a
 * staff function (TP3), so students just post to existing threads. </p>
 *
 * <p> Soft delete: when a student deletes a post, isDeleted is set to TRUE rather than
 * removing the record. Replies are preserved and show a deletion notice per the spec. </p>
 *
 * <p> isInstructorEndorsed and staffComment are included for TP3 Staff Epics so we
 * don't need a schema change later. </p>
 * 
 * <p> <b>Testing:</b> The CRUD and validation methods in this class are directly validated 
 * by the automated tests in {@link testing.TestStudentPosts}. </p>
 *
 * @author Prince Dahiya
 * @author Sumukh Gowda
 * @author Klim Savalia
 * @author Micah Branton
 * @author Huu Binh Vu
 *
 * @version 1.00    2026-03-21    Initial TP2 implementation
 * @version 1.01    2026-03-21    Added thread, isDeleted; fixed postType to QUESTION/STATEMENT
 */
public class Post {

    // --- Validation constants ---
    /** Max characters for a post title. */
    public static final int MAX_TITLE_LEN = 200;
    /** Max characters for a post body. */
    public static final int MAX_BODY_LEN = 4000;
    /** Max characters for a staff comment (anticipated for TP3). */
    public static final int MAX_STAFF_COMMENT_LEN = 1000;
    /** Sentinel value meaning this post has no parent (it's a top-level thread). */
    public static final int NO_PARENT = -1;
    /** Default thread name — all posts go here until staff creates more threads in TP3. */
    public static final String DEFAULT_THREAD = "General";

    // --- Attributes ---

    /** Database primary key. Set to 0 before insert; DB assigns the real ID. */
    private int id;

    /** Short title of the post. Required. */
    private String title;

    /** Full text content of the post. Required. */
    private String body;

    /**
     * Username of whoever created this post. Used to enforce ownership so only the
     * author can edit/delete. Also needed for TP3 Staff Epics to track participation.
     */
    private String authorUsername;

    /** Timestamp of when the post was created, formatted as "yyyy-MM-dd HH:mm:ss". */
    private String timestamp;

    /**
     * Type of post: "QUESTION" or "STATEMENT".
     * Students post questions and statements per the user stories.
     * Stored as String instead of enum to keep H2 compatibility simple.
     */
    private String postType;

    /**
     * The discussion thread this post belongs to. Defaults to "General".
     * Thread creation is a staff function (TP3) — students just post to existing threads.
     */
    private String thread;

    /**
     * ID of the parent post if this is a reply, or NO_PARENT (-1) if top-level.
     * This design lets us store both posts and replies in one table.
     */
    private int parentPostId;

    /** Whether this question has been answered/resolved. Only matters for QUESTION type. */
    private boolean isResolved;

    /**
     * Soft delete flag. When TRUE, the post content is replaced with a deletion notice
     * but the record stays in the DB so replies remain visible per the spec.
     */
    private boolean isDeleted;

    /**
     * Whether an instructor has endorsed this post. Not used in TP2 UI yet,
     * included so we don't need a schema change in TP3.
     */
    private boolean isInstructorEndorsed;

    /**
     * Optional feedback from a staff member. Not used in TP2 UI yet, included for TP3.
     * Always stored as empty string rather than null to avoid null checks in the View.
     */
    private String staffComment;


    /*******
     * <p> Method: Post(String, String, String, String, int) </p>
     *
     * <p> Description: Main constructor for creating a new post from the GUI.
     * Thread defaults to "General". Timestamp is set automatically. </p>
     *
     * @param title          Post title
     * @param body           Post body text
     * @param authorUsername Username of the student creating this post
     * @param postType       "QUESTION" or "STATEMENT"
     * @param parentPostId   NO_PARENT (-1) for new thread, or a post ID if this is a reply
     */
    public Post(String title, String body, String authorUsername,
                String postType, int parentPostId) {
        this(title, body, authorUsername, postType, parentPostId, DEFAULT_THREAD);
    }

    /*******
     * <p> Method: Post(String, String, String, String, int, String) </p>
     *
     * <p> Description: Constructor that also specifies a thread. Used when posting
     * to a non-default thread. </p>
     *
     * @param title          Post title
     * @param body           Post body text
     * @param authorUsername Username of the student creating this post
     * @param postType       "QUESTION" or "STATEMENT"
     * @param parentPostId   NO_PARENT or a valid post ID for replies
     * @param thread         The thread to post to (e.g. "General")
     */
    public Post(String title, String body, String authorUsername,
                String postType, int parentPostId, String thread) {
        this.id = 0; // DB sets the real ID on INSERT
        this.title = title;
        this.body = body;
        this.authorUsername = authorUsername;
        this.postType = postType;
        this.parentPostId = parentPostId;
        this.thread = (thread == null || thread.trim().isEmpty()) ? DEFAULT_THREAD : thread;
        this.timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.isResolved = false;
        this.isDeleted = false;
        this.isInstructorEndorsed = false;
        this.staffComment = "";
    }

    /*******
     * <p> Method: Post(int, String, ...) — full DB constructor </p>
     *
     * <p> Description: Full constructor used by the Database layer when loading existing
     * posts from a ResultSet. Every field is provided explicitly. </p>
     *
     * @param id                   DB primary key
     * @param title                Post title
     * @param body                 Post body
     * @param authorUsername       Author's username
     * @param timestamp            Creation timestamp string
     * @param postType             "QUESTION" or "STATEMENT"
     * @param thread               Thread name
     * @param parentPostId         Parent ID or NO_PARENT
     * @param isResolved           Whether marked resolved
     * @param isDeleted            Whether soft-deleted
     * @param isInstructorEndorsed Whether endorsed by staff (TP3)
     * @param staffComment         Staff feedback if any (TP3)
     */
    public Post(int id, String title, String body, String authorUsername, String timestamp,
                String postType, String thread, int parentPostId, boolean isResolved,
                boolean isDeleted, boolean isInstructorEndorsed, String staffComment) {
        this.id = id;
        this.title = title;
        this.body = body;
        this.authorUsername = authorUsername;
        this.timestamp = timestamp;
        this.postType = postType;
        this.thread = (thread == null || thread.trim().isEmpty()) ? DEFAULT_THREAD : thread;
        this.parentPostId = parentPostId;
        this.isResolved = isResolved;
        this.isDeleted = isDeleted;
        this.isInstructorEndorsed = isInstructorEndorsed;
        this.staffComment = (staffComment == null) ? "" : staffComment;
    }


    // --- Validation Methods ---
    // Return empty string if valid, error message if not.
    // Consistent with the InputUtils.validateInput() pattern in the codebase.

    /*******
     * <p> Method: validateTitle(String) </p>
     * <p> Description: Checks title isn't empty and isn't over the limit. </p>
     * @param title The title to validate
     * @return Empty string if valid, error message otherwise
     */
    public static String validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) return "Post title cannot be empty.";
        if (title.length() > MAX_TITLE_LEN)
            return "Post title is too long (max " + MAX_TITLE_LEN + " characters).";
        return "";
    }

    /*******
     * <p> Method: validateBody(String) </p>
     * <p> Description: Checks body isn't empty and isn't over the limit. </p>
     * @param body The body text to validate
     * @return Empty string if valid, error message otherwise
     */
    public static String validateBody(String body) {
        if (body == null || body.trim().isEmpty()) return "Post body cannot be empty.";
        if (body.length() > MAX_BODY_LEN)
            return "Post body is too long (max " + MAX_BODY_LEN + " characters).";
        return "";
    }

    /*******
     * <p> Method: validatePostType(String) </p>
     * <p> Description: Makes sure the post type is QUESTION or STATEMENT. </p>
     * @param postType The post type string to validate
     * @return Empty string if valid, error message otherwise
     */
    public static String validatePostType(String postType) {
        if (postType == null) return "Post type cannot be null.";
        if (!postType.equals("QUESTION") && !postType.equals("STATEMENT"))
            return "Post type must be QUESTION or STATEMENT.";
        return "";
    }


    // --- Getters ---

    /** @return The database ID of this post */
    public int getId() { return id; }
    /** @return The post title (or "[Deleted]" label handled by display logic) */
    public String getTitle() { return title; }
    /** @return The full post body text */
    public String getBody() { return body; }
    /** @return The username of the student who created this post */
    public String getAuthorUsername() { return authorUsername; }
    /** @return The creation timestamp string */
    public String getTimestamp() { return timestamp; }
    /** @return The post type ("QUESTION" or "STATEMENT") */
    public String getPostType() { return postType; }
    /** @return The thread this post belongs to */
    public String getThread() { return thread; }
    /** @return The parent post ID, or NO_PARENT (-1) if top-level */
    public int getParentPostId() { return parentPostId; }
    /** @return True if this question has been marked as resolved */
    public boolean isResolved() { return isResolved; }
    /** @return True if the author has soft-deleted this post */
    public boolean isDeleted() { return isDeleted; }
    /** @return True if an instructor has endorsed this post (TP3) */
    public boolean isInstructorEndorsed() { return isInstructorEndorsed; }
    /** @return Any staff comment, or empty string if none (TP3) */
    public String getStaffComment() { return staffComment; }


    // --- Setters ---

    /** @param title New title (validate with validateTitle first) */
    public void setTitle(String title) { this.title = title; }
    /** @param body New body text (validate with validateBody first) */
    public void setBody(String body) { this.body = body; }
    /** @param resolved True to mark resolved, false to reopen */
    public void setResolved(boolean resolved) { this.isResolved = resolved; }
    /** @param deleted True marks this post as soft-deleted */
    public void setDeleted(boolean deleted) { this.isDeleted = deleted; }
    /** @param thread The thread name for this post */
    public void setThread(String thread) { this.thread = thread; }
    /** @param endorsed True if an instructor endorsed this. For TP3. */
    public void setInstructorEndorsed(boolean endorsed) { this.isInstructorEndorsed = endorsed; }
    /** @param staffComment Staff feedback. Stores "" instead of null. For TP3. */
    public void setStaffComment(String staffComment) {
        this.staffComment = (staffComment == null) ? "" : staffComment;
    }


    // --- Utility ---

    /*******
     * <p> Method: isReply() </p>
     * <p> Description: Returns true if this post is a reply to another post. Used by
     * the View to decide whether to indent it under its parent. </p>
     * @return True if this is a reply, false if it's a top-level thread
     */
    public boolean isReply() { return parentPostId != NO_PARENT; }

    /*******
     * <p> Method: toString() </p>
     * <p> Description: Human-readable summary for debugging and test output. </p>
     * @return Formatted string with the key fields of this post
     */
    @Override
    public String toString() {
        return "Post[id=" + id + ", type=" + postType + ", thread=" + thread
                + ", author=" + authorUsername + ", title='" + title
                + "', parentId=" + parentPostId + ", resolved=" + isResolved
                + ", deleted=" + isDeleted + ", timestamp=" + timestamp + "]";
    }
}
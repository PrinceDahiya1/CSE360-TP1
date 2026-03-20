package database;

import java.util.ArrayList;
import java.util.List;
import entityClasses.Post;
import entityClasses.Reply;
import entityClasses.InputUtils;

/**
 * <p> ForumManager Class </p>
 * <p> Description: Manages CRUD operations for Posts and Replies. 
 * Supports returning subsets based on searches. </p>
 */
public class ForumManager {
	private List<Post> posts;
	private List<Reply> replies;
	private int nextPostId = 1;
	private int nextReplyId = 1;

	public ForumManager() {
		posts = new ArrayList<>();
		replies = new ArrayList<>();
	}

	// ==================== POST CRUD ====================

	public void createPost(String title, String body) throws Exception {
		// TP1 Integration: Use InputUtils for Validation
		String titleError = InputUtils.validateInput(title, InputUtils.MAX_POST_TITLE_LEN, "Post Title");
		if (!titleError.isEmpty()) throw new Exception(titleError);

		// Assuming posts can be long, we use a high limit like 2000
		String bodyError = InputUtils.validateInput(body, 2000, "Post Body");
		if (!bodyError.isEmpty()) throw new Exception(bodyError);

		posts.add(new Post(nextPostId++, title, body));
	}

	public List<Post> getAllPosts() {
		return new ArrayList<>(posts);
	}

	public List<Post> searchPosts(String keyword) {
		List<Post> subset = new ArrayList<>();
		for (Post p : posts) {
			if (p.getTitle().contains(keyword) || p.getBody().contains(keyword)) {
				subset.add(p);
			}
		}
		return subset;
	}

	public void updatePost(int postId, String newBody) throws Exception {
		String bodyError = InputUtils.validateInput(newBody, 2000, "Post Body");
		if (!bodyError.isEmpty()) throw new Exception(bodyError);

		for (Post p : posts) {
			if (p.getId() == postId) {
				p.setBody(newBody);
				return;
			}
		}
		throw new Exception("Error: Post not found.");
	}

	public void deletePost(int postId) {
		posts.removeIf(p -> p.getId() == postId);
		replies.removeIf(r -> r.getPostId() == postId);
	}

	// ==================== REPLY CRUD ====================

	public void createReply(int postId, String body) throws Exception {
		// TP1 Integration: Use InputUtils for Reply limits
		String replyError = InputUtils.validateInput(body, InputUtils.MAX_REPLY_BODY_LEN, "Reply");
		if (!replyError.isEmpty()) throw new Exception(replyError);

		replies.add(new Reply(nextReplyId++, postId, body));
	}

	public List<Reply> getRepliesForPost(int postId) {
		List<Reply> subset = new ArrayList<>();
		for (Reply r : replies) {
			if (r.getPostId() == postId) {
				subset.add(r);
			}
		}
		return subset;
	}
	
	public void deleteReply(int replyId) {
		replies.removeIf(r -> r.getId() == replyId);
	}
}
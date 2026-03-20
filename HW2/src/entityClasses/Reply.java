package entityClasses;

/**
 * <p> Reply Class </p>
 * <p> Description: Represents a reply attached to a specific Post. </p>
 */
public class Reply {
	private int id;
	private int postId;
	private String body;

	public Reply(int id, int postId, String body) {
		this.id = id;
		this.postId = postId;
		this.body = body;
	}

	public int getId() { return id; }
	public int getPostId() { return postId; }
	public String getBody() { return body; }
	public void setBody(String body) { this.body = body; }

	@Override
	public String toString() {
		return "  -> Reply [" + id + "] to Post [" + postId + "]: " + body;
	}
}
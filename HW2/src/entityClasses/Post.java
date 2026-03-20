package entityClasses;

/**
 * <p> Post Class </p>
 * <p> Description: Represents a discussion forum post with an ID, title, and body. </p>
 */
public class Post {
	private int id;
	private String title;
	private String body;

	public Post(int id, String title, String body) {
		this.id = id;
		this.title = title;
		this.body = body;
	}

	public int getId() { return id; }
	public String getTitle() { return title; }
	public void setTitle(String title) { this.title = title; }
	public String getBody() { return body; }
	public void setBody(String body) { this.body = body; }

	@Override
	public String toString() {
		return "Post [" + id + "]: " + title + " - " + body;
	}
}
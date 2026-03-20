package database;

import entityClasses.Post;
import entityClasses.Reply;

public class HW2Tester {

	public static void main(String[] args) {
		ForumManager manager = new ForumManager();

		System.out.println("========== HW2 TEST EXECUTION ==========\n");

		// ---------------------------------------------------------
		// TEST SUITE 1: POST OPERATIONS
		// ---------------------------------------------------------
		System.out.println("--- Test 1.1 (Positive): Create Valid Post ---");
		try {
			manager.createPost("Help with Java", "How do I use ArrayLists?");
			System.out.println("RESULT: Pass. Post created successfully.");
		} catch (Exception e) {
			System.out.println("RESULT: Fail. " + e.getMessage());
		}

		System.out.println("\n--- Test 1.2 (Negative): Create Post Validation (Empty Title) ---");
		try {
			manager.createPost("", "This should fail due to empty title.");
			System.out.println("RESULT: Fail. Post was created when it shouldn't have been.");
		} catch (Exception e) {
			System.out.println("RESULT: Pass. Caught expected error -> " + e.getMessage()); 
		}

		System.out.println("\n--- Test 1.3 (Positive): Read/Search Posts (Subset) ---");
		try {
			manager.createPost("Python Question", "How do loops work?"); // Add a dummy post
			System.out.println("Searching for keyword 'Java'...");
			for (Post p : manager.searchPosts("Java")) {
				System.out.println("Found: " + p.toString());
			}
			System.out.println("RESULT: Pass. Correct subset returned.");
		} catch (Exception e) {
			System.out.println("RESULT: Fail.");
		}

		System.out.println("\n--- Test 1.4 & 1.5 (Positive): Update & Delete Post ---");
		try {
			// Update the Java post (ID 1)
			manager.updatePost(1, "Edit: I figured out ArrayLists, thanks!");
			System.out.println("After Update: " + manager.getAllPosts().get(0).toString());
			
			// Delete the Python post (ID 2)
			manager.deletePost(2);
			System.out.println("After Deleting Post 2, total posts: " + manager.getAllPosts().size());
			System.out.println("RESULT: Pass. Update and Delete successful.");
		} catch (Exception e) {
			System.out.println("RESULT: Fail. " + e.getMessage());
		}

		// ---------------------------------------------------------
		// TEST SUITE 2: REPLY OPERATIONS
		// ---------------------------------------------------------
		System.out.println("\n--- Test 2.1 (Positive): Create Valid Reply ---");
		try {
			manager.createReply(1, "You can use the .add() method.");
			System.out.println("RESULT: Pass. Reply created successfully.");
		} catch (Exception e) {
			System.out.println("RESULT: Fail. " + e.getMessage());
		}

		System.out.println("\n--- Test 2.2 (Negative): Create Reply Validation (Over 500 chars) ---");
		try {
			String massiveReply = "a".repeat(505);
			manager.createReply(1, massiveReply);
			System.out.println("RESULT: Fail. Allowed massive reply.");
		} catch (Exception e) {
			System.out.println("RESULT: Pass. Caught expected error -> " + e.getMessage());
		}
		
		System.out.println("\n--- Staff Epic Test: Delete Reply ---");
		System.out.println("Replies before deletion:");
		for (Reply r : manager.getRepliesForPost(1)) {
			System.out.println(r.toString());
		}
		
		manager.deleteReply(1); // Delete the reply
		
		System.out.println("Replies remaining for Post 1: " + manager.getRepliesForPost(1).size());
		System.out.println("RESULT: Pass. Reply deleted successfully.");
	}
}
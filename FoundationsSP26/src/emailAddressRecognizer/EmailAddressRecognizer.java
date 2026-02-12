package emailAddressRecognizer;

public class EmailAddressRecognizer {
	/**
	 * <p> Title: checkForValidEmailAddress </p>
	 * * <p> Description: Validates an email address using a Finite State Machine (FSM).
	 * Returns an error message if invalid, or an empty string "" if valid.
	 * </p>
	 */
	public static String checkForValidEmailAddress(String email) {
		// 1. Initialize FSM variables
		if (email == null || email.isEmpty()) {
			return "Email address cannot be empty.";
		}
		
		int state = 0;
		int index = 0;
		int emailLength = email.length();
		boolean running = true;
		char currentChar;

		// 2. FSM Loop
		while (running && index < emailLength) {
			currentChar = email.charAt(index);
			
			switch (state) {
			case 0: // Start: Expecting Local Part
				if (isValidChar(currentChar)) {
					state = 1; // Good start
				} else {
					return "Email must start with a valid character (A-Z, 0-9).";
				}
				break;
				
			case 1: // Inside first part
				if (currentChar == '@') {
					state = 2; // Found the separator - Check this FIRST!
				} else if (isValidChar(currentChar) || currentChar == '.' || currentChar == '-' || currentChar == '_') {
					// Stay in state 1
				} else {
					return "Invalid character in email username: '" + currentChar + "'";
				}
				break;
				
			case 2: // After '@', Expecting Domain Name
				if (isValidChar(currentChar)) {
					state = 3; // Good domain start
				} else {
					return "Email domain must start with a valid character after '@'.";
				}
				break;
				
			case 3: // Inside Domain (e.g., "gmail")
				if (currentChar == '.') {
					state = 4; // Found the dot - Check this FIRST!
				} else if (isValidChar(currentChar) || currentChar == '-') {
					// Stay in state 3
				} else {
					return "Invalid character in domain name: '" + currentChar + "'";
				}
				break;
				
			case 4: // After '.', Expecting ending (e.g., "com")
				if (isValidChar(currentChar)) {
					state = 5; // Good TLD start
				} else {
					return "Email must end with a valid Top Level Domain (e.g., .com).";
				}
				break;
				
			case 5: // Inside the end (e.g., "com" or "edu")
				if (currentChar == '.') {
					state = 4; // Handling subdomains like co.uk - Check this FIRST!
				} else if (isValidChar(currentChar)) {
					// Stay in state 5
				} else {
					return "Invalid character in Top Level Domain: '" + currentChar + "'";
				}
				break;
			}
			index++;
		}
		
		// 3. Final State Check
		if (state == 5) {
			return ""; // Success!
		}
		return "Email address is incomplete.";
	}
	
	// Only checks for alphanumeric characters now
	private static boolean isValidChar(char c) {
		return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9');
	}
}
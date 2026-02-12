package entityClasses;

public class InputUtils {
    
    // Reasonable limits to prevent buffer overflow/crashes
    public static final int MAX_USERNAME_LEN = 50;
    public static final int MAX_PASSWORD_LEN = 50;
    public static final int MAX_EMAIL_LEN = 100;

    public static String validateInput(String input, int maxLength, String fieldName) {
        if (input == null || input.trim().isEmpty()) {
            return fieldName + " cannot be empty.";
        }
        if (input.length() > maxLength) {
            return fieldName + " is too long. Limit: " + maxLength + " characters.";
        }
        // Optional: Block dangerous characters for SQL/XSS protection
        if (input.contains("'") || input.contains("\"") || input.contains(";")) {
             return fieldName + " contains invalid characters (', \", ;).";
        }
        return ""; // Valid
    }
}
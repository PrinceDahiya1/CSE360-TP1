package entityClasses;

public class InputUtils {

    public static final int MAX_USERNAME_LEN = 50;
    public static final int MAX_PASSWORD_LEN = 50;
    public static final int MAX_EMAIL_LEN = 100;

    public static final int MAX_POST_TITLE_LEN = 100;
    public static final int MAX_REPLY_BODY_LEN = 500;

    public static String validateInput(String input, int maxLength, String fieldName) {
        if (input == null || input.trim().isEmpty()) {
            return "Error: " + fieldName + " cannot be empty.";
        }
        if (input.length() > maxLength) {
            return "Error: " + fieldName + " is too long. Limit: " + maxLength + " characters.";
        }
        return ""; // Valid (Returns empty string if no errors)
    }
}
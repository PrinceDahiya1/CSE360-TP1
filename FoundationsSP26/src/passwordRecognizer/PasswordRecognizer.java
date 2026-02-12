package passwordRecognizer;

public class PasswordRecognizer {
    
    // Internal FSM State Variables (From Model.java)
    private static String passwordErrorMessage = "";
    private static String inputLine = "";
    private static char currentChar;
    private static int currentCharNdx;
    private static boolean running;
    
    // Requirement Flags
    private static boolean foundUpperCase = false;
    private static boolean foundLowerCase = false;
    private static boolean foundNumericDigit = false;
    private static boolean foundSpecialChar = false;
    private static boolean foundLongEnough = false;

    /*******
     * <p> Title: evaluatePassword </p>
     * * <p> Description: Adapted from Model.java. uses the Finite State Machine (FSM)
     * logic to evaluate the password security. </p>
     */
    public static String evaluatePassword(String input) {
        // Initialize FSM variables
        passwordErrorMessage = "";
        inputLine = input;
        currentCharNdx = 0;
        
        // Reset Flags
        foundUpperCase = false;
        foundLowerCase = false;
        foundNumericDigit = false;
        foundSpecialChar = false;
        foundLongEnough = false;

        if (input == null || input.isEmpty()) {
            return "Password cannot be empty.";
        }
        
        // Setup first character
        currentChar = input.charAt(0);
        running = true;

        // --- START FSM LOOP (Preserved from Model.java) ---
        while (running) {
            // Transition Check
            if (currentChar >= 'A' && currentChar <= 'Z') {
                foundUpperCase = true;
            } else if (currentChar >= 'a' && currentChar <= 'z') {
                foundLowerCase = true;
            } else if (currentChar >= '0' && currentChar <= '9') {
                foundNumericDigit = true;
            } else if ("~`!@#$%^&*()_-+={}[]|\\:;\"'<>,.?/".indexOf(currentChar) >= 0) {
                foundSpecialChar = true;
            } else {
                // Invalid character found
                return "Invalid character found at index " + currentCharNdx + ": " + currentChar;
            }
            
            // State Check: Length (Checked at every step in original FSM)
            if (currentCharNdx >= 7) {
                foundLongEnough = true;
            }
            
            // Advance FSM
            currentCharNdx++;
            if (currentCharNdx >= inputLine.length()) {
                running = false;
            } else {
                currentChar = input.charAt(currentCharNdx);
            }
        }
        // --- END FSM LOOP ---

        // Construct Helpful Error Message
        String errMessage = "";
        if (!foundUpperCase)
            errMessage += "- Upper case letter (A-Z)\n";
        
        if (!foundLowerCase)
            errMessage += "- Lower case letter (a-z)\n";
        
        if (!foundNumericDigit)
            errMessage += "- Numeric digit (0-9)\n";
            
        if (!foundSpecialChar)
            errMessage += "- Special character (e.g., !@#)\n";
            
        if (!foundLongEnough)
            errMessage += "- At least 8 characters\n";
        
        if (errMessage.length() == 0) {
            return ""; // Success
        }
        
        return "Missing requirements:\n" + errMessage;
    }
}
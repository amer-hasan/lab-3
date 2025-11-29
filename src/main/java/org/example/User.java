package org.example;

//imports
import java.util.regex.Pattern;

//User class
public class User {

    //username <-- Email
    private final String username;
    private final String password;

    //Creating a user with the given Email and password
    public User(String username, String password) throws Exception {
        validateEmail(username);
        validatePassword(password);
        this.username = username;
        this.password = password;
    }

    /**
     * Validation check for the Email.
     * Now public so it can be reused by the login logic.
     */
    public static void validateEmail(String email) throws Exception {
        if (email == null || email.length() > 50) {
            throw new Exception("Username is too long, try something shorter");
        }
        String emailRegex =
                "^(?=.{1,50}$)" +
                        "([A-Za-z0-9._%+\\-]+)@" +
                        "([A-Za-z0-9][A-Za-z0-9\\-]*(?:\\.[A-Za-z0-9][A-Za-z0-9\\-]*)*)" +
                        "\\.[A-Za-z]{2,}$";
        if (!Pattern.matches(emailRegex, email)) {
            throw new Exception("Please enter a valid Email as username");
        }
    }

    //validation check for the Password
    private static void validatePassword(String pwd) throws Exception {
        if (pwd == null) throw new Exception("Please enter a valid password");
        int len = pwd.length();
        if (len < 8) throw new Exception("Your password is too short, add more characters");
        if (len > 12) throw new Exception("Your password is too long, try a shorter one");
        boolean hasLetter = Pattern.compile("[A-Za-z]").matcher(pwd).find();
        boolean hasDigit  = Pattern.compile("\\d").matcher(pwd).find();
        boolean hasSymbol = Pattern.compile("[^A-Za-z0-9]").matcher(pwd).find();
        if (!(hasLetter && hasDigit && hasSymbol) || pwd.contains(" ")) {
            throw new Exception("Please enter a valid password");
        }
    }

    //getters
    public String getName() { return username; }
    public String getPassword() { return password; }
}

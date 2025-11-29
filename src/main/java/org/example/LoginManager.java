package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages login validation, user status, and blocking logic.
 * This class is thread-safe where needed.
 * Responsibilities:
 *   - Load valid users (username/password) from Users.txt
 *   - Track per-user failed attempts and block state via UserStatus
 *   *   - Provide a thread-safe method attemptLogin(...) used by AttemptThread
 */
public class LoginManager {

    // All valid users loaded from Users.txt: username -> password
    private final Map<String, String> validUsers = new HashMap<>();

    // Per-user login status (attempt count, block time, etc.)
    private final Map<String, UserStatus> userStatusMap = new HashMap<>();

    public LoginManager() {
        loadUsersFromFile();
    }

    /**
     * Load users from Users.txt (same behavior as in Lab 2).
     * Users.txt is expected to be in src/main/resources/org/example.
     */
    private void loadUsersFromFile() {
        InputStream is = LoginManager.class.getResourceAsStream("Users.txt");
        if (is == null) {
            System.out.println("Users.txt not found in resources. No users loaded.");
            return;
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split("\\s+");
                if (parts.length < 2) {
                    // line with no password – try to create user with empty password (may fail validation)
                    try {
                        User user = new User(parts[0], "");
                        validUsers.put(user.getName(), user.getPassword());
                    } catch (Exception ex) {
                        System.out.println(line + " --> " + ex.getMessage());
                    }
                    continue;
                }

                String username = parts[0];
                String pwd = parts[1];

                try {
                    User user = new User(username, pwd);
                    validUsers.put(user.getName(), user.getPassword());
                } catch (Exception ex) {
                    System.out.println(line + " --> " + ex.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading Users.txt: " + e.getMessage());
        }
    }

    /**
     * Get or create a UserStatus object for a specific username.
     */
    private synchronized UserStatus getStatusFor(String username) {
        return userStatusMap.computeIfAbsent(
                username,
                u -> new UserStatus(
                        u,
                        LoginConfig.getMaxAttempts(),
                        LoginConfig.getBlockDurationSeconds()
                )
        );
    }

    /**
     * Validate one login attempt from a background thread.
     * This method is fully thread-safe.
     *
     * Logic:
     *   - Invalid email format       → INVALID_EMAIL_FORMAT (no attempt counted)
     *   - Email not in Users.txt     → UNKNOWN_EMAIL (no attempt counted)
     *   - Only valid + existing emails use UserStatus and can be blocked.
     */
    public synchronized LoginResult attemptLogin(String username, String password) {

        // 0. Check email format (should NOT count as an attempt if invalid)
        try {
            User.validateEmail(username);
        } catch (Exception ex) {
            // Invalid email format
            return LoginResult.INVALID_EMAIL_FORMAT;
        }

        // 1. Check if this email exists in our data (Users.txt)
        String expectedPassword = validUsers.get(username);
        if (expectedPassword == null) {
            // Email format is valid, but not in our data
            return LoginResult.UNKNOWN_EMAIL;
        }

        // 2. From here on: email is valid and exists in Users.txt.
        //    Only now we maintain per-user status (attempts, block, etc.)
        UserStatus status = getStatusFor(username);

        // 3. Check if the user is already blocked
        if (status.isBlocked()) {
            return LoginResult.BLOCKED_ALREADY;
        }

        // 4. Check credentials against the validUsers map
        boolean correctCredentials = expectedPassword.equals(password);

        if (correctCredentials) {
            // SUCCESS
            status.registerSuccessfulLogin();
            return LoginResult.SUCCESS;
        }

        // 5. Wrong password for a valid + existing email → increment attempts
        boolean userJustGotBlocked = status.registerFailedAttempt();

        if (userJustGotBlocked) {
            // Start a timer thread which will unblock the user after t seconds
            BlockTimerThread timerThread = new BlockTimerThread(status);
            timerThread.start();
            return LoginResult.BLOCKED_JUST_NOW;
        }

        // 6. Wrong credentials but not yet blocked
        return LoginResult.WRONG_CREDENTIALS;
    }

    /**
     * Get remaining block time for UI display.
     */
    public synchronized int getRemainingBlockSeconds(String username) {
        UserStatus status = getStatusFor(username);
        return status.getRemainingBlockSeconds();
    }

    /**
     * NEW: Get how many attempts are left for this email before blocking.
     * (Only meaningful for valid + known emails.)
     */
    public synchronized int getAttemptsLeft(String username) {
        UserStatus status = getStatusFor(username);
        return status.getAttemptsLeft();
    }
}

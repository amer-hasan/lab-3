package org.example;

/**
 * A background thread that performs a single login attempt.
 * It:
 *   - Takes the username & password from the controller
 *   - Calls LoginManager.attemptLogin()
 *   - Returns the result through a callback (LoginCallback)
 * IMPORTANT:
 *   - This thread MUST NOT update any JavaFX UI components directly.
 *   - The controller will receive the result and update the UI on the JavaFX thread.
 */
public class AttemptThread extends Thread {

    private final LoginManager loginManager;
    private final String username;
    private final String password;

    // Callback interface to report result back to controller
    private final LoginCallback callback;

    public AttemptThread(LoginManager loginManager,
                         String username,
                         String password,
                         LoginCallback callback) {
        this.loginManager = loginManager;
        this.username = username;
        this.password = password;
        this.callback = callback;
        setDaemon(true); // optional: thread closes when app closes
    }

    @Override
    public void run() {
        // Perform login (thread-safe)
        LoginResult result = loginManager.attemptLogin(username, password);

        // Send the result back to the controller
        if (callback != null) {
            callback.onLoginFinished(result);
        }
    }

    /**
     * Callback interface for returning results to the UI layer.
     */
    public interface LoginCallback {
        void onLoginFinished(LoginResult result);
    }
}

package org.example;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;

public class LoginView implements AttemptThread.LoginCallback {

    @FXML
    private TextField input;

    @FXML
    private PasswordField password;

    @FXML
    private Button submit;

    @FXML
    private Label errorLabel;

    private final LoginManager loginManager = new LoginManager();

    // Timeline for countdown
    private Timeline countdownTimeline;

    @FXML
    public void initialize() {
        clearMessage();
    }

    @FXML
    private void onSubmit() {
        clearMessage();

        String usernameInput = input.getText() != null ? input.getText().trim() : "";
        String passwordInput = password.getText() != null ? password.getText() : "";

        if (usernameInput.isEmpty() || passwordInput.isEmpty()) {
            showMessage("Please enter username and password.");
            return;
        }

        // Disable while the background thread is running, to avoid double-click spam
        submit.setDisable(true);
        showMessage("Checking credentials...");

        AttemptThread thread = new AttemptThread(
                loginManager,
                usernameInput,
                passwordInput,
                this
        );
        thread.start();
    }

    @Override
    public void onLoginFinished(LoginResult result) {
        Platform.runLater(() -> handleLoginResult(result));
    }

    private void handleLoginResult(LoginResult result) {

        String usernameInput = input.getText() != null ? input.getText().trim() : "";

        switch (result) {

            case SUCCESS:
                stopCountdown(); // ensure no countdown remains
                submit.setDisable(false);
                clearMessage();
                openWelcomeScreen(usernameInput);
                break;

            case WRONG_CREDENTIALS:
                stopCountdown();
                submit.setDisable(false);
                int attemptsLeft = loginManager.getAttemptsLeft(usernameInput);
                showMessage("User or password do not match. Attempts left: " + attemptsLeft + ".");
                break;

            case INVALID_EMAIL_FORMAT:
                stopCountdown();
                submit.setDisable(false);
                showMessage("Please enter a valid email address.");
                break;

            case UNKNOWN_EMAIL:
                stopCountdown();
                submit.setDisable(false);
                showMessage("This email is not registered in the system.");
                break;

            case BLOCKED_ALREADY:
                // Block is per email; we still allow trying a different email.
                submit.setDisable(false);
                showCountdown(usernameInput);
                break;

            case BLOCKED_JUST_NOW:
                // User has just been blocked (for this email).
                submit.setDisable(false);
                showCountdown(usernameInput);
                break;

            default:
                submit.setDisable(false);
                throw new IllegalStateException("Unexpected value: " + result);
        }
    }

    // ----------------------------------------------------------
    //              COUNTDOWN LOGIC FOR BLOCKING
    // ----------------------------------------------------------

    private void showCountdown(String usernameInput) {
        stopCountdown(); // clear any old timeline

        // IMPORTANT:
        // We do NOT disable the submit button here.
        // The user is blocked only for this specific email.
        // They can try a different email while the countdown runs.

        int remaining = loginManager.getRemainingBlockSeconds(usernameInput);

        if (remaining <= 0) {
            showMessage("You are no longer blocked. Try again.");
            return;
        }

        // Show initial message
        showMessage("Blocked for " + remaining + " seconds...");

        // Create timeline that updates every 1 second
        countdownTimeline = new Timeline(
                new KeyFrame(Duration.seconds(1), event -> {
                    int sec = loginManager.getRemainingBlockSeconds(usernameInput);

                    if (sec <= 0) {
                        stopCountdown();
                        showMessage("You can try again now.");
                    } else {
                        showMessage("Blocked for " + sec + " seconds.");
                    }
                })
        );

        countdownTimeline.setCycleCount(Timeline.INDEFINITE);
        countdownTimeline.play();
    }

    private void stopCountdown() {
        if (countdownTimeline != null) {
            countdownTimeline.stop();
            countdownTimeline = null;
        }
    }

    // ----------------------------------------------------------
    //                  UI helper methods
    // ----------------------------------------------------------

    private void showMessage(String text) {
        if (errorLabel != null) {
            errorLabel.setText(text);
        }
    }

    private void clearMessage() {
        if (errorLabel != null) {
            errorLabel.setText("");
        }
    }

    // ----------------------------------------------------------
    //               Switch to Welcome screen
    // ----------------------------------------------------------

    private void openWelcomeScreen(String usernameInput) {
        try {
            FXMLLoader loader =
                    new FXMLLoader(getClass().getResource("welcome-view.fxml"));
            Parent root = loader.load();

            WelcomeView controller = loader.getController();
            controller.setUsername(usernameInput);

            Stage stage =
                    (Stage) input.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Error loading Welcome screen.");
        }
    }
}

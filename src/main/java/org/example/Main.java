package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // Load the login screen
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Login");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * The program must receive two command-line parameters BEFORE opening the screens:
     *   args[0] = n (maximum number of authentication attempts)
     *   args[1] = t (block duration in seconds)
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java -jar lab3.jar <maxAttempts> <blockSeconds>");
            System.err.println("Example: java -jar lab3.jar 3 10");
            System.exit(1);
        }

        int maxAttempts;
        int blockSeconds;

        try {
            maxAttempts = Integer.parseInt(args[0]);
            blockSeconds = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.err.println("Both arguments must be integers.");
            System.err.println("Usage: java -jar lab3.jar <maxAttempts> <blockSeconds>");
            System.exit(1);
            return; // unreachable, but keeps the compiler happy
        }

        if (maxAttempts <= 0 || blockSeconds <= 0) {
            System.err.println("Both maxAttempts (n) and blockSeconds (t) must be positive.");
            System.exit(1);
        }

        // Initialize global configuration BEFORE any GUI is shown
        LoginConfig.init(maxAttempts, blockSeconds);

        // Start JavaFX application (args are passed forward if needed)
        launch(args);
    }
}

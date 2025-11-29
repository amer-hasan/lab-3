package org.example;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class WelcomeView {

    @FXML
    private Label welcomeLabel;

    public void setUsername(String username) {
        welcomeLabel.setText("Welcome, " + username + "!");
    }
}

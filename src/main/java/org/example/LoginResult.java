package org.example;

/**
 * Represents the possible outcomes of a single login attempt.
 *
 * This is used by the login attempt thread and the controller
 * to decide what to show in the UI.
 */
public enum LoginResult {

    /**
     * The username and password were correct.
     * The user should be logged in and the Welcome screen opened.
     */
    SUCCESS,

    /**
     * The username or password were incorrect, but the user did NOT
     * reach the maximum attempts yet, and is not blocked.
     */
    WRONG_CREDENTIALS,

    /**
     * The email/username format is invalid (not a valid email).
     * This should NOT be counted as an attempt.
     */
    INVALID_EMAIL_FORMAT,

    /**
     * The email/username format is valid, but this email does NOT
     * appear in the Users.txt data file.
     * This should NOT be counted as an attempt.
     */
    UNKNOWN_EMAIL,

    /**
     * The user was already blocked BEFORE this attempt, e.g. the
     * block time (t seconds) has not yet passed.
     */
    BLOCKED_ALREADY,

    /**
     * This attempt caused the user to become blocked because they
     * reached the maximum number of allowed failed attempts (n).
     */
    BLOCKED_JUST_NOW
}

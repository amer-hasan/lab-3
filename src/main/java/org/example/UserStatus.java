package org.example;

/**
 * Represents the login status of a single user:
 * - how many failed attempts they have
 * - whether they are currently blocked
 * - until when they are blocked
 * This class is thread-safe: all state-changing methods are synchronized.
 */
public class UserStatus {

    private final String username;

    // Current number of consecutive failed attempts
    private int failedAttempts;

    // Timestamp in milliseconds until which the user is blocked.
    // 0 means "not blocked".
    private long blockedUntilMillis;

    // Configuration per user (taken from command-line arguments n and t)
    private final int maxAttempts;
    private final int blockDurationSeconds;

    /**
     * Create a UserStatus object for a given user.
     *
     * @param username             the username this status belongs to
     * @param maxAttempts          maximum allowed failed attempts before blocking (n)
     * @param blockDurationSeconds block duration in seconds (t)
     */
    public UserStatus(String username, int maxAttempts, int blockDurationSeconds) {
        this.username = username;
        this.maxAttempts = maxAttempts;
        this.blockDurationSeconds = blockDurationSeconds;
        this.failedAttempts = 0;
        this.blockedUntilMillis = 0;
    }

    /**
     * Checks if the user is currently blocked.
     * If the block time has passed, this method automatically unblocks the user.
     *
     * @return true if the user is still blocked, false otherwise
     */
    public synchronized boolean isBlocked() {
        if (blockedUntilMillis == 0) {
            return false;
        }

        long now = System.currentTimeMillis();
        if (now > blockedUntilMillis) {
            // Block time is over → automatically unblock and reset attempts
            blockedUntilMillis = 0;
            failedAttempts = 0;
            return false;
        }

        return true;
    }

    /**
     * @return remaining block time in whole seconds (0 if not blocked or already expired).
     */
    public synchronized int getRemainingBlockSeconds() {
        if (!isBlocked()) {
            return 0;
        }
        long now = System.currentTimeMillis();
        long diffMillis = blockedUntilMillis - now;
        if (diffMillis <= 0) {
            return 0;
        }
        return (int) Math.ceil(diffMillis / 1000.0);
    }

    /**
     * Call this when the user logs in successfully.
     * Resets failed attempts and clears any block.
     */
    public synchronized void registerSuccessfulLogin() {
        failedAttempts = 0;
        blockedUntilMillis = 0;
    }

    /**
     * Call this when a login attempt fails.
     * Increments the failedAttempts counter and, if needed, blocks the user.
     *
     * @return true if this call caused the user to become blocked (i.e. reached maxAttempts), false otherwise.
     */
    public synchronized boolean registerFailedAttempt() {
        failedAttempts++;

        if (failedAttempts >= maxAttempts) {
            long now = System.currentTimeMillis();
            blockedUntilMillis = now + blockDurationSeconds * 1000L;
            return true; // user just got blocked
        }

        return false; // user not blocked yet
    }

    /**
     * Forcefully unblock the user and reset the failedAttempts counter.
     * This is useful if you implement a separate "timer thread" that
     * sleeps for t seconds and then calls this method.
     */
    public synchronized void forceUnblock() {
        failedAttempts = 0;
        blockedUntilMillis = 0;
    }

    // --- Getters ---

    public synchronized int getFailedAttempts() {
        return failedAttempts;
    }

    public synchronized boolean hasReachedMaxAttempts() {
        return failedAttempts >= maxAttempts;
    }

    /** NEW: how many attempts are left before blocking. */
    public synchronized int getAttemptsLeft() {
        int left = maxAttempts - failedAttempts;
        return Math.max(left, 0);
    }

    public String getUsername() {
        return username;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public int getBlockDurationSeconds() {
        return blockDurationSeconds;
    }
}

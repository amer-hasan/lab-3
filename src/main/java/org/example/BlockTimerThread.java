package org.example;

/**
 * A background thread that is started when a user becomes blocked
 * (i.e. reaches the maximum number of failed attempts).
 *
 * It sleeps for the configured block duration and then unblocks the user
 * by calling userStatus.forceUnblock().
 *
 * IMPORTANT:
 *   - This thread does NOT update any JavaFX UI components directly.
 *   - It only manipulates the UserStatus model.
 */
public class BlockTimerThread extends Thread {

    private final UserStatus userStatus;

    public BlockTimerThread(UserStatus userStatus) {
        this.userStatus = userStatus;
        setDaemon(true); // so it won't prevent the app from exiting
    }

    @Override
    public void run() {
        // We rely on the duration stored inside the UserStatus
        int blockSeconds = userStatus.getBlockDurationSeconds();

        try {
            Thread.sleep(blockSeconds * 1000L);
        } catch (InterruptedException e) {
            // If interrupted, just stop and let current block state remain
            return;
        }

        // After sleeping, unblock the user
        userStatus.forceUnblock();
    }
}

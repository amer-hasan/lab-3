package org.example;

/**
 * Global configuration for the login system.
 * Stores the maximum number of allowed failed attempts (n)
 * and the block duration in seconds (t), as given in the
 * command-line arguments.
 * Usage:
 *   - In main():   LoginConfig.init(maxAttempts, blockSeconds);
 *   - Elsewhere:   LoginConfig.getMaxAttempts(), LoginConfig.getBlockDurationSeconds()
 */
public final class LoginConfig {

    // Prevent instantiation
    private LoginConfig() {}

    private static int maxAttempts;
    private static int blockDurationSeconds;

    private static boolean initialized = false;

    /**
     * Initialize the configuration.
     * Should be called once at application startup (from main()).
     *
     * @param maxAttempts          maximum number of failed attempts before blocking (n)
     * @param blockDurationSeconds block duration in seconds (t)
     */
    public static void init(int maxAttempts, int blockDurationSeconds) {
        LoginConfig.maxAttempts = maxAttempts;
        LoginConfig.blockDurationSeconds = blockDurationSeconds;
        initialized = true;
    }

    public static int getMaxAttempts() {
        ensureInitialized();
        return maxAttempts;
    }

    public static int getBlockDurationSeconds() {
        ensureInitialized();
        return blockDurationSeconds;
    }

    public static boolean isInitialized() {
        return initialized;
    }

    private static void ensureInitialized() {
        if (!initialized) {
            throw new IllegalStateException("LoginConfig has not been initialized. Call LoginConfig.init(n, t) in main() first.");
        }
    }
}

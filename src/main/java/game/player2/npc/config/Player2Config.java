package game.player2.npc.config;

/**
 * Configuration for the Player2 NPC Library.
 * <p>
 * Create with the builder and pass to {@code Player2NpcLib.initialize()}.
 * </p>
 *
 * <pre>{@code
 * Player2Config config = Player2Config.builder()
 *     .apiBaseUrl("http://127.0.0.1:4315")
 *     .gameKey("your-game-key")
 *     .build();
 *
 * Player2NpcLib.initialize(config);
 * }</pre>
 */
public class Player2Config {
    private static volatile Player2Config INSTANCE;

    private final String apiBaseUrl;
    private final String gameKey;
    private final int connectionTimeout;
    private final int maxReconnectAttempts;

    private Player2Config(Builder builder) {
        this.apiBaseUrl = builder.apiBaseUrl;
        this.gameKey = builder.gameKey;
        this.connectionTimeout = builder.connectionTimeout;
        this.maxReconnectAttempts = builder.maxReconnectAttempts;
    }

    /**
     * Returns the base URL for the Player2 API.
     * Default: http://127.0.0.1:4315
     */
    public static String getApiBaseUrl() {
        return getInstance().apiBaseUrl;
    }

    /**
     * Returns the game key for API authentication.
     * This should be set to your game's client ID from the Player2 Developer Dashboard.
     */
    public static String getGameKey() {
        return getInstance().gameKey;
    }

    /**
     * Returns the connection timeout in seconds.
     */
    public static int getConnectionTimeout() {
        return getInstance().connectionTimeout;
    }

    /**
     * Returns the maximum number of reconnection attempts for SSE streams.
     */
    public static int getMaxReconnectAttempts() {
        return getInstance().maxReconnectAttempts;
    }

    static Player2Config getInstance() {
        if (INSTANCE == null) {
            throw new IllegalStateException(
                "Player2Config not initialized. Call Player2NpcLib.initialize() first.");
        }
        return INSTANCE;
    }

    /** Internal use only. Called by Player2NpcLib.initialize(). */
    public static void setInstance(Player2Config config) {
        INSTANCE = config;
    }

    /** Internal use only. Called by Player2NpcLib.shutdown(). */
    public static void clearInstance() {
        INSTANCE = null;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String apiBaseUrl = "http://127.0.0.1:4315";
        private String gameKey = "";
        private int connectionTimeout = 30;
        private int maxReconnectAttempts = 10;

        /**
         * Sets the base URL for the Player2 NPC API.
         * The Player2 App must be running for the API to work.
         * Default: http://127.0.0.1:4315
         */
        public Builder apiBaseUrl(String apiBaseUrl) {
            this.apiBaseUrl = apiBaseUrl;
            return this;
        }

        /**
         * Sets the game client key for API authentication.
         * Get this from the Player2 Developer Dashboard.
         * Leave empty for local testing without a registered game.
         */
        public Builder gameKey(String gameKey) {
            this.gameKey = gameKey;
            return this;
        }

        /**
         * Sets the connection timeout in seconds for HTTP requests.
         *
         * @param seconds Timeout value (5-120)
         */
        public Builder connectionTimeout(int seconds) {
            if (seconds < 5 || seconds > 120) {
                throw new IllegalArgumentException(
                    "connectionTimeout must be between 5 and 120");
            }
            this.connectionTimeout = seconds;
            return this;
        }

        /**
         * Sets the maximum number of reconnection attempts for SSE stream.
         * Uses exponential backoff between attempts.
         *
         * @param attempts Max attempts (1-50)
         */
        public Builder maxReconnectAttempts(int attempts) {
            if (attempts < 1 || attempts > 50) {
                throw new IllegalArgumentException(
                    "maxReconnectAttempts must be between 1 and 50");
            }
            this.maxReconnectAttempts = attempts;
            return this;
        }

        public Player2Config build() {
            return new Player2Config(this);
        }
    }
}

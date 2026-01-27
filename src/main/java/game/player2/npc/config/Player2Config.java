package game.player2.npc.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Configuration for the Player2 NPC Library.
 * <p>
 * Configuration file is located at {@code config/player2npc-common.toml}
 * </p>
 */
public class Player2Config {
    public static final ModConfigSpec SPEC;
    private static final Config CONFIG;

    static {
        Pair<Config, ModConfigSpec> pair = new ModConfigSpec.Builder()
            .configure(Config::new);
        SPEC = pair.getRight();
        CONFIG = pair.getLeft();
    }

    /**
     * Returns the base URL for the Player2 API.
     * Default: http://127.0.0.1:4315
     */
    public static String getApiBaseUrl() {
        return CONFIG.apiBaseUrl.get();
    }

    /**
     * Returns the game key for API authentication.
     * This should be set to your game's client ID from the Player2 Developer Dashboard.
     */
    public static String getGameKey() {
        return CONFIG.gameKey.get();
    }

    /**
     * Returns the connection timeout in seconds.
     */
    public static int getConnectionTimeout() {
        return CONFIG.connectionTimeout.get();
    }

    /**
     * Returns the maximum number of reconnection attempts for SSE streams.
     */
    public static int getMaxReconnectAttempts() {
        return CONFIG.maxReconnectAttempts.get();
    }

    private static class Config {
        final ModConfigSpec.ConfigValue<String> apiBaseUrl;
        final ModConfigSpec.ConfigValue<String> gameKey;
        final ModConfigSpec.IntValue connectionTimeout;
        final ModConfigSpec.IntValue maxReconnectAttempts;

        Config(ModConfigSpec.Builder builder) {
            builder.comment("Player2 NPC Library Configuration")
                   .push("api");

            apiBaseUrl = builder
                .comment("Base URL for the Player2 NPC API.",
                        "The Player2 App must be running for the API to work.",
                        "Default: http://127.0.0.1:4315")
                .define("baseUrl", "http://127.0.0.1:4315");

            gameKey = builder
                .comment("Game client key for API authentication.",
                        "Get this from the Player2 Developer Dashboard.",
                        "Leave empty for local testing without a registered game.")
                .define("gameKey", "");

            connectionTimeout = builder
                .comment("Connection timeout in seconds for HTTP requests.")
                .defineInRange("connectionTimeout", 30, 5, 120);

            maxReconnectAttempts = builder
                .comment("Maximum number of reconnection attempts for SSE stream.",
                        "Uses exponential backoff between attempts.")
                .defineInRange("maxReconnectAttempts", 10, 1, 50);

            builder.pop();
        }
    }
}

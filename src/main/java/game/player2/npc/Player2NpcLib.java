package game.player2.npc;

import game.player2.npc.api.NpcBuilder;
import game.player2.npc.api.NpcHandle;
import game.player2.npc.client.ChatCompletionsClient;
import game.player2.npc.client.Player2HttpClient;
import game.player2.npc.config.Player2Config;
import game.player2.npc.event.Player2EventBus;
import game.player2.npc.event.Player2EventListener;
import game.player2.npc.internal.NpcRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Main entry point for the Player2 NPC Library.
 * <p>
 * This library provides an event-driven, async interface for spawning AI NPCs
 * using the Player2 API. NPCs can chat with players, execute commands, and
 * respond to game events.
 * </p>
 *
 * <h2>Quick Start</h2>
 * <pre>{@code
 * // Initialize the library
 * Player2NpcLib.initialize(
 *     Player2Config.builder()
 *         .apiBaseUrl("http://127.0.0.1:4315")
 *         .gameKey("your-key")
 *         .build()
 * );
 *
 * // Register event listener
 * Player2NpcLib.addListener(new Player2EventListener() {
 *     @Override
 *     public boolean onMessageEvent(NpcMessageEvent event) {
 *         System.out.println("NPC says: " + event.getMessage());
 *         return false;
 *     }
 *
 *     @Override
 *     public boolean onCommandEvent(NpcCommandEvent event) {
 *         if ("minecraft_command".equals(event.getCommandName())) {
 *             String cmd = event.getStringArgument("command");
 *             // Execute the command
 *             return true;
 *         }
 *         return false;
 *     }
 * });
 *
 * // Spawn an NPC
 * Player2NpcLib.builder("shopkeeper")
 *     .name("Merchant Bob")
 *     .description("A friendly shopkeeper who sells potions")
 *     .systemPrompt("You are a medieval shopkeeper...")
 *     .withMinecraftCommands()
 *     .spawn("my_game_id")
 *     .thenAccept(npc -> {
 *         npc.chat("Steve", "Hello!");
 *     });
 *
 * // Shutdown when done
 * Player2NpcLib.shutdown();
 * }</pre>
 */
public class Player2NpcLib {
    private static final Logger LOGGER = LoggerFactory.getLogger(Player2NpcLib.class);

    private static volatile Player2HttpClient client;
    private static volatile ChatCompletionsClient chatCompletionsClient;
    private static final Object clientLock = new Object();
    private static volatile boolean initialized = false;

    private Player2NpcLib() {} // prevent instantiation

    /**
     * Initializes the library with the given configuration.
     * Must be called before any other API method.
     *
     * @param config The library configuration
     */
    public static void initialize(Player2Config config) {
        Player2Config.setInstance(config);
        initialized = true;
        LOGGER.info("Player2 NPC Library initialized");
    }

    /**
     * Initializes with default configuration.
     */
    public static void initialize() {
        initialize(Player2Config.builder().build());
    }

    /**
     * Registers an event listener.
     *
     * @param listener The listener to register
     */
    public static void addListener(Player2EventListener listener) {
        Player2EventBus.getInstance().addListener(listener);
    }

    /**
     * Removes an event listener.
     *
     * @param listener The listener to remove
     */
    public static void removeListener(Player2EventListener listener) {
        Player2EventBus.getInstance().removeListener(listener);
    }

    // ==================== Public API ====================

    /**
     * Creates a new NPC builder with the given short name.
     *
     * @param shortName Short identifier for the NPC (used in API calls)
     * @return A new NpcBuilder instance
     */
    public static NpcBuilder builder(String shortName) {
        checkInitialized();
        return new NpcBuilder(shortName, getOrCreateClient());
    }

    /**
     * Retrieves an active NPC handle by its ID.
     *
     * @param npcId The UUID of the NPC
     * @return Optional containing the NpcHandle if found
     */
    public static Optional<NpcHandle> getNpc(UUID npcId) {
        return NpcRegistry.getInstance().get(npcId);
    }

    /**
     * Kills an NPC by its ID.
     *
     * @param gameId The game session ID
     * @param npcId The UUID of the NPC to kill
     * @return CompletableFuture that completes when the NPC is killed
     */
    public static CompletableFuture<Void> killNpc(String gameId, UUID npcId) {
        return getOrCreateClient().killNpc(gameId, npcId)
            .thenRun(() -> NpcRegistry.getInstance().remove(npcId));
    }

    /**
     * Starts listening for NPC responses on the specified game.
     * <p>
     * This is called automatically when the first NPC is spawned,
     * but can be called manually if needed.
     * </p>
     *
     * @param gameId The game session ID
     */
    public static void startListening(String gameId) {
        getOrCreateClient().startSseStream(gameId);
    }

    /**
     * Stops listening for NPC responses on the specified game.
     *
     * @param gameId The game session ID
     */
    public static void stopListening(String gameId) {
        getOrCreateClient().stopSseStream(gameId);
    }

    /**
     * Checks if the SSE stream is active for the given game.
     *
     * @param gameId The game session ID
     * @return true if actively listening
     */
    public static boolean isListening(String gameId) {
        return client != null && client.isStreamActive(gameId);
    }

    /**
     * Sends a health check to the Player2 API.
     * <p>
     * It's recommended to call this every 60 seconds to maintain
     * the connection and track game time.
     * </p>
     *
     * @return CompletableFuture with true if healthy
     */
    public static CompletableFuture<Boolean> checkHealth() {
        return getOrCreateClient().checkHealth();
    }

    /**
     * Returns the ChatCompletionsClient for making /v1/chat/completions calls.
     * Lazily creates the client on first access.
     *
     * @return The ChatCompletionsClient instance
     */
    public static ChatCompletionsClient getChatCompletionsClient() {
        checkInitialized();
        if (chatCompletionsClient == null) {
            synchronized (clientLock) {
                if (chatCompletionsClient == null) {
                    chatCompletionsClient = new ChatCompletionsClient();
                }
            }
        }
        return chatCompletionsClient;
    }

    /**
     * Shuts down all connections and cleans up resources.
     * <p>
     * Must be called manually when the application shuts down.
     * </p>
     */
    public static void shutdown() {
        synchronized (clientLock) {
            if (client != null) {
                LOGGER.info("Shutting down Player2 NPC Library...");
                client.shutdown();
                client = null;
            }
            if (chatCompletionsClient != null) {
                chatCompletionsClient.shutdown();
                chatCompletionsClient = null;
            }
        }
        NpcRegistry.getInstance().clear();
        Player2EventBus.getInstance().clearListeners();
        Player2Config.clearInstance();
        initialized = false;
    }

    /**
     * Gets the HTTP client instance, creating it if necessary.
     */
    static Player2HttpClient getOrCreateClient() {
        checkInitialized();
        if (client == null) {
            synchronized (clientLock) {
                if (client == null) {
                    client = new Player2HttpClient(
                        Player2Config.getApiBaseUrl(),
                        Player2Config.getGameKey()
                    );
                }
            }
        }
        return client;
    }

    private static void checkInitialized() {
        if (!initialized) {
            throw new IllegalStateException(
                "Player2NpcLib not initialized. Call Player2NpcLib.initialize() first.");
        }
    }
}

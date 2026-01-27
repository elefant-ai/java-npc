package game.player2.npc;

import game.player2.npc.api.NpcBuilder;
import game.player2.npc.api.NpcHandle;
import game.player2.npc.client.Player2HttpClient;
import game.player2.npc.config.Player2Config;
import game.player2.npc.internal.NpcRegistry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
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
 * // Listen for NPC responses
 * @SubscribeEvent
 * public void onNpcMessage(NpcMessageEvent event) {
 *     MinecraftHelpers.sendNpcMessage(player, "NPC", event.getMessage());
 * }
 *
 * // Listen for NPC commands
 * @SubscribeEvent
 * public void onNpcCommand(NpcCommandEvent event) {
 *     if ("minecraft_command".equals(event.getCommandName())) {
 *         String cmd = event.getStringArgument("command");
 *         MinecraftHelpers.executeCommand(server, cmd);
 *     }
 * }
 * }</pre>
 */
@Mod(Player2NpcLib.MOD_ID)
public class Player2NpcLib {
    public static final String MOD_ID = "player2npc";
    private static final Logger LOGGER = LoggerFactory.getLogger(Player2NpcLib.class);

    private static volatile Player2HttpClient client;
    private static final Object clientLock = new Object();

    public Player2NpcLib(IEventBus modEventBus, ModContainer modContainer) {
        // Register config
        modContainer.registerConfig(ModConfig.Type.COMMON, Player2Config.SPEC);

        // Register lifecycle events
        modEventBus.addListener(this::onCommonSetup);

        // Register server stopping event to clean up
        NeoForge.EVENT_BUS.addListener(this::onServerStopping);

        LOGGER.info("Player2 NPC Library initialized");
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            LOGGER.info("Player2 NPC Library common setup complete");
        });
    }

    private void onServerStopping(ServerStoppingEvent event) {
        shutdown();
    }

    // ==================== Public API ====================

    /**
     * Creates a new NPC builder with the given short name.
     *
     * @param shortName Short identifier for the NPC (used in API calls)
     * @return A new NpcBuilder instance
     */
    public static NpcBuilder builder(String shortName) {
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
     * Shuts down all connections and cleans up resources.
     * <p>
     * Called automatically when the server stops, but can be
     * called manually if needed.
     * </p>
     */
    public static void shutdown() {
        synchronized (clientLock) {
            if (client != null) {
                LOGGER.info("Shutting down Player2 NPC Library...");
                client.shutdown();
                client = null;
            }
        }
        NpcRegistry.getInstance().clear();
    }

    /**
     * Gets the HTTP client instance, creating it if necessary.
     */
    static Player2HttpClient getOrCreateClient() {
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
}

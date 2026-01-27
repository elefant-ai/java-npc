package game.player2.npc.api;

import game.player2.npc.client.Player2HttpClient;
import game.player2.npc.dto.ChatRequest;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Handle to an active NPC, providing methods to interact with it.
 * <p>
 * Obtained from {@link NpcBuilder#spawn(String)} after successfully spawning an NPC.
 * </p>
 *
 * <pre>{@code
 * NpcHandle npc = ...;
 *
 * // Send a chat message
 * npc.chat("Steve", "Hello, how are you?");
 *
 * // Send with game context
 * npc.chat("Steve", "What's the weather like?", "Current weather is sunny, temperature 25C");
 *
 * // Kill the NPC when done
 * npc.kill();
 * }</pre>
 */
public class NpcHandle {
    private final UUID npcId;
    private final String gameId;
    private final String shortName;
    private final String displayName;
    private final Player2HttpClient client;

    private volatile boolean alive = true;

    public NpcHandle(UUID npcId, String gameId, String shortName,
                    String displayName, Player2HttpClient client) {
        this.npcId = npcId;
        this.gameId = gameId;
        this.shortName = shortName;
        this.displayName = displayName;
        this.client = client;
    }

    /**
     * Returns the unique ID of this NPC.
     */
    public UUID getId() {
        return npcId;
    }

    /**
     * Returns the game ID this NPC belongs to.
     */
    public String getGameId() {
        return gameId;
    }

    /**
     * Returns the short name of the NPC.
     */
    public String getShortName() {
        return shortName;
    }

    /**
     * Returns the display name of the NPC.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns whether this NPC is still alive.
     */
    public boolean isAlive() {
        return alive;
    }

    /**
     * Sends a chat message to this NPC.
     * <p>
     * Response will be delivered via {@link game.player2.npc.event.NpcMessageEvent}
     * on the NeoForge event bus.
     * </p>
     *
     * @param senderName The name of the player/entity sending the message
     * @param message The message content
     * @return CompletableFuture that completes when the message is sent
     */
    public CompletableFuture<Void> chat(String senderName, String message) {
        return chat(senderName, message, null, null);
    }

    /**
     * Sends a chat message with game context.
     *
     * @param senderName The name of the player/entity sending the message
     * @param message The message content
     * @param gameStateInfo Optional game state information for context
     * @return CompletableFuture that completes when the message is sent
     */
    public CompletableFuture<Void> chat(String senderName, String message, String gameStateInfo) {
        return chat(senderName, message, gameStateInfo, null);
    }

    /**
     * Sends a chat message with additional options.
     *
     * @param senderName The name of the player/entity sending the message
     * @param message The message content
     * @param gameStateInfo Optional game state information for context
     * @param tts Optional TTS mode: "local_client" or "server"
     * @return CompletableFuture that completes when the message is sent
     */
    public CompletableFuture<Void> chat(String senderName, String message,
                                       String gameStateInfo, String tts) {
        if (!alive) {
            return CompletableFuture.failedFuture(
                new IllegalStateException("NPC has been killed"));
        }

        ChatRequest request = new ChatRequest(senderName, message, gameStateInfo, tts);
        return client.sendChat(gameId, npcId, request);
    }

    /**
     * Kills this NPC.
     *
     * @return CompletableFuture that completes when the NPC is killed
     */
    public CompletableFuture<Void> kill() {
        if (!alive) {
            return CompletableFuture.completedFuture(null);
        }

        return client.killNpc(gameId, npcId)
            .thenRun(() -> alive = false);
    }

    /**
     * Marks this NPC as dead (internal use).
     */
    public void markDead() {
        alive = false;
    }
}

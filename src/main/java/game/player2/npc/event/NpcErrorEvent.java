package game.player2.npc.event;

import net.neoforged.bus.api.Event;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Fired when an error occurs in NPC operations.
 * <p>
 * Listen for this event on {@code NeoForge.EVENT_BUS}.
 * </p>
 *
 * <pre>{@code
 * @SubscribeEvent
 * public void onNpcError(NpcErrorEvent event) {
 *     LOGGER.error("NPC error ({}): {}", event.getType(), event.getMessage());
 *     if (event.getCause() != null) {
 *         event.getCause().printStackTrace();
 *     }
 * }
 * }</pre>
 */
public class NpcErrorEvent extends Event {
    private final ErrorType type;
    private final String message;
    @Nullable
    private final UUID npcId;
    @Nullable
    private final String gameId;
    @Nullable
    private final Throwable cause;

    public NpcErrorEvent(ErrorType type, String message) {
        this(type, message, null, null, null);
    }

    public NpcErrorEvent(ErrorType type, String message,
                        @Nullable UUID npcId, @Nullable String gameId,
                        @Nullable Throwable cause) {
        this.type = type;
        this.message = message;
        this.npcId = npcId;
        this.gameId = gameId;
        this.cause = cause;
    }

    /**
     * Returns the type of error that occurred.
     */
    public ErrorType getType() {
        return type;
    }

    /**
     * Returns a human-readable error message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns the NPC ID related to this error, if applicable.
     */
    @Nullable
    public UUID getNpcId() {
        return npcId;
    }

    /**
     * Returns the game ID related to this error, if applicable.
     */
    @Nullable
    public String getGameId() {
        return gameId;
    }

    /**
     * Returns the underlying exception that caused this error, if any.
     */
    @Nullable
    public Throwable getCause() {
        return cause;
    }

    /**
     * Error type categories.
     */
    public enum ErrorType {
        /**
         * Failed to connect to the Player2 API.
         */
        CONNECTION_FAILED,

        /**
         * HTTP request returned an error status.
         */
        HTTP_ERROR,

        /**
         * Failed to parse API response.
         */
        PARSE_ERROR,

        /**
         * SSE stream encountered an error.
         */
        STREAM_ERROR,

        /**
         * Authentication failed (user not logged in to Player2 App).
         */
        AUTH_ERROR,

        /**
         * Insufficient credits/joules.
         */
        INSUFFICIENT_CREDITS,

        /**
         * NPC not found.
         */
        NPC_NOT_FOUND,

        /**
         * Unknown or unclassified error.
         */
        UNKNOWN
    }
}

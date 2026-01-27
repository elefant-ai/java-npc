package game.player2.npc.event;

import net.neoforged.bus.api.Event;

/**
 * Fired when the SSE connection status changes for NPC responses.
 * <p>
 * Listen for this event on {@code NeoForge.EVENT_BUS}.
 * </p>
 *
 * <pre>{@code
 * @SubscribeEvent
 * public void onNpcConnection(NpcConnectionEvent event) {
 *     switch (event.getStatus()) {
 *         case CONNECTED -> LOGGER.info("Connected to NPC service");
 *         case DISCONNECTED -> LOGGER.warn("Disconnected: {}", event.getMessage());
 *         case RECONNECTING -> LOGGER.info("Reconnecting...");
 *     }
 * }
 * }</pre>
 */
public class NpcConnectionEvent extends Event {
    private final String gameId;
    private final Status status;
    private final String message;

    public NpcConnectionEvent(String gameId, Status status, String message) {
        this.gameId = gameId;
        this.status = status;
        this.message = message;
    }

    /**
     * Returns the game ID this connection event relates to.
     */
    public String getGameId() {
        return gameId;
    }

    /**
     * Returns the connection status.
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Returns an optional message with more details about the status change.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Connection status values.
     */
    public enum Status {
        /**
         * SSE stream connected successfully.
         */
        CONNECTED,

        /**
         * SSE stream disconnected.
         */
        DISCONNECTED,

        /**
         * Attempting to reconnect after a disconnection.
         */
        RECONNECTING,

        /**
         * Reconnection failed after all retry attempts.
         */
        RECONNECT_FAILED
    }
}

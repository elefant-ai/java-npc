package game.player2.npc.event;

/**
 * Fired when the SSE connection status changes for NPC responses.
 * <p>
 * Register a {@link Player2EventListener} to receive these events.
 * </p>
 *
 * <pre>{@code
 * Player2NpcLib.addListener(new Player2EventListener() {
 *     @Override
 *     public void onConnectionEvent(NpcConnectionEvent event) {
 *         switch (event.getStatus()) {
 *             case CONNECTED -> LOGGER.info("Connected to NPC service");
 *             case DISCONNECTED -> LOGGER.warn("Disconnected: {}", event.getMessage());
 *             case RECONNECTING -> LOGGER.info("Reconnecting...");
 *         }
 *     }
 * });
 * }</pre>
 */
public class NpcConnectionEvent {
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

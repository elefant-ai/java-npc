package game.player2.npc.event;

/**
 * Listener interface for Player2 NPC events.
 * <p>
 * Implement the methods you care about; default implementations are no-ops.
 * Register via {@link game.player2.npc.Player2NpcLib#addListener(Player2EventListener)}.
 * </p>
 *
 * <pre>{@code
 * Player2NpcLib.addListener(new Player2EventListener() {
 *     @Override
 *     public boolean onMessageEvent(NpcMessageEvent event) {
 *         System.out.println("NPC says: " + event.getMessage());
 *         return false;
 *     }
 * });
 * }</pre>
 */
public interface Player2EventListener {

    /**
     * Called when the SSE connection status changes.
     */
    default void onConnectionEvent(NpcConnectionEvent event) {}

    /**
     * Called when an NPC sends a text message.
     *
     * @return true to consume this event (prevents further listener propagation)
     */
    default boolean onMessageEvent(NpcMessageEvent event) { return false; }

    /**
     * Called when an NPC requests a command/function execution.
     *
     * @return true to consume this event (prevents further listener propagation)
     */
    default boolean onCommandEvent(NpcCommandEvent event) { return false; }

    /**
     * Called when an error occurs in NPC operations.
     */
    default void onErrorEvent(NpcErrorEvent event) {}
}

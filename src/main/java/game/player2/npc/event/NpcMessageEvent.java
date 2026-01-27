package game.player2.npc.event;

import javax.annotation.Nullable;
import java.util.Base64;
import java.util.UUID;

/**
 * Fired when an NPC sends a text message response.
 * <p>
 * Return {@code true} from {@link Player2EventListener#onMessageEvent} to consume this event.
 * </p>
 *
 * <pre>{@code
 * Player2NpcLib.addListener(new Player2EventListener() {
 *     @Override
 *     public boolean onMessageEvent(NpcMessageEvent event) {
 *         String message = event.getMessage();
 *         UUID npcId = event.getNpcId();
 *         // Handle the NPC response
 *         return false;
 *     }
 * });
 * }</pre>
 */
public class NpcMessageEvent {
    private final UUID npcId;
    private final String gameId;
    private final String message;
    @Nullable
    private final String audioDataBase64;

    public NpcMessageEvent(UUID npcId, String gameId, String message, @Nullable String audioDataBase64) {
        this.npcId = npcId;
        this.gameId = gameId;
        this.message = message;
        this.audioDataBase64 = audioDataBase64;
    }

    /**
     * Returns the ID of the NPC that sent this message.
     */
    public UUID getNpcId() {
        return npcId;
    }

    /**
     * Returns the game ID this NPC belongs to.
     */
    public String getGameId() {
        return gameId;
    }

    /**
     * Returns the text message from the NPC.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns true if audio data is available.
     */
    public boolean hasAudio() {
        return audioDataBase64 != null && !audioDataBase64.isEmpty();
    }

    /**
     * Returns the raw base64-encoded audio data, or null if not available.
     */
    @Nullable
    public String getAudioDataBase64() {
        return audioDataBase64;
    }

    /**
     * Returns the decoded audio data as bytes, or null if not available.
     */
    @Nullable
    public byte[] getAudioBytes() {
        if (audioDataBase64 == null || audioDataBase64.isEmpty()) {
            return null;
        }
        try {
            return Base64.getDecoder().decode(audioDataBase64);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}

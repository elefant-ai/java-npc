package game.player2.npc.dto;

import com.google.gson.annotations.SerializedName;

import javax.annotation.Nullable;

/**
 * Request body for sending a chat message to an NPC.
 */
public class ChatRequest {
    @SerializedName("sender_name")
    private final String senderName;

    @SerializedName("sender_message")
    private final String senderMessage;

    @SerializedName("game_state_info")
    @Nullable
    private final String gameStateInfo;

    @Nullable
    private final String tts;

    public ChatRequest(String senderName, String senderMessage) {
        this(senderName, senderMessage, null, null);
    }

    public ChatRequest(String senderName, String senderMessage,
                      @Nullable String gameStateInfo, @Nullable String tts) {
        this.senderName = senderName;
        this.senderMessage = senderMessage;
        this.gameStateInfo = gameStateInfo;
        this.tts = tts;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getSenderMessage() {
        return senderMessage;
    }

    @Nullable
    public String getGameStateInfo() {
        return gameStateInfo;
    }

    @Nullable
    public String getTts() {
        return tts;
    }
}

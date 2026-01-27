package game.player2.npc.dto;

import com.google.gson.annotations.SerializedName;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * Response from the NPC API SSE stream.
 */
public class NpcResponse {
    @SerializedName("npc_id")
    private UUID npcId;

    @Nullable
    private String message;

    @SerializedName("command")
    @Nullable
    private List<CommandResponse> commands;

    @Nullable
    private AudioData audio;

    public UUID getNpcId() {
        return npcId;
    }

    @Nullable
    public String getMessage() {
        return message;
    }

    @Nullable
    public List<CommandResponse> getCommands() {
        return commands;
    }

    @Nullable
    public AudioData getAudio() {
        return audio;
    }

    /**
     * Audio data from TTS response.
     */
    public static class AudioData {
        private String data;

        public String getData() {
            return data;
        }
    }
}

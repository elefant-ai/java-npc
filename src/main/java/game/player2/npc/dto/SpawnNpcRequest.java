package game.player2.npc.dto;

import com.google.gson.annotations.SerializedName;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * Request body for spawning a new NPC.
 */
public class SpawnNpcRequest {
    @SerializedName("short_name")
    private final String shortName;

    private final String name;

    @SerializedName("character_description")
    private final String characterDescription;

    @SerializedName("system_prompt")
    private final String systemPrompt;

    @SerializedName("voice_id")
    private final String voiceId;

    @Nullable
    private final List<Function> commands;

    @SerializedName("keep_game_state")
    @Nullable
    private final Boolean keepGameState;

    @SerializedName("npc_id")
    @Nullable
    private final String npcId;

    public SpawnNpcRequest(String shortName, String name, String characterDescription,
                          String systemPrompt, String voiceId,
                          @Nullable List<Function> commands, @Nullable Boolean keepGameState,
                          @Nullable UUID npcId) {
        this.shortName = shortName;
        this.name = name;
        this.characterDescription = characterDescription;
        this.systemPrompt = systemPrompt;
        this.voiceId = voiceId;
        this.commands = commands;
        this.keepGameState = keepGameState;
        this.npcId = npcId != null ? npcId.toString() : null;
    }

    public String getShortName() {
        return shortName;
    }

    public String getName() {
        return name;
    }

    public String getCharacterDescription() {
        return characterDescription;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public String getVoiceId() {
        return voiceId;
    }

    @Nullable
    public List<Function> getCommands() {
        return commands;
    }

    @Nullable
    public Boolean getKeepGameState() {
        return keepGameState;
    }

    @Nullable
    public String getNpcId() {
        return npcId;
    }
}

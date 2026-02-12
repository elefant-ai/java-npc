package game.player2.npc.dto;

import com.google.gson.annotations.SerializedName;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Request body for the TTS speak endpoint ({@code POST /v1/tts/speak}).
 */
public class TtsSpeakRequest {
    private final String text;

    @SerializedName("play_in_app")
    private final boolean playInApp;

    private final double speed;

    @SerializedName("voice_ids")
    @Nullable
    private final List<String> voiceIds;

    @SerializedName("voice_gender")
    @Nullable
    private final String voiceGender;

    @SerializedName("voice_language")
    @Nullable
    private final String voiceLanguage;

    @SerializedName("audio_format")
    @Nullable
    private final String audioFormat;

    @SerializedName("advanced_voice")
    @Nullable
    private final AdvancedVoice advancedVoice;

    public TtsSpeakRequest(String text, boolean playInApp, double speed,
                           @Nullable List<String> voiceIds, @Nullable String voiceGender,
                           @Nullable String voiceLanguage, @Nullable String audioFormat,
                           @Nullable AdvancedVoice advancedVoice) {
        this.text = text;
        this.playInApp = playInApp;
        this.speed = speed;
        this.voiceIds = voiceIds;
        this.voiceGender = voiceGender;
        this.voiceLanguage = voiceLanguage;
        this.audioFormat = audioFormat;
        this.advancedVoice = advancedVoice;
    }

    public String getText() {
        return text;
    }

    public boolean isPlayInApp() {
        return playInApp;
    }

    public double getSpeed() {
        return speed;
    }

    @Nullable
    public List<String> getVoiceIds() {
        return voiceIds;
    }

    @Nullable
    public String getVoiceGender() {
        return voiceGender;
    }

    @Nullable
    public String getVoiceLanguage() {
        return voiceLanguage;
    }

    @Nullable
    public String getAudioFormat() {
        return audioFormat;
    }

    @Nullable
    public AdvancedVoice getAdvancedVoice() {
        return advancedVoice;
    }

    /**
     * Advanced voice configuration for enhanced control.
     */
    public static class AdvancedVoice {
        @Nullable
        private final String instructions;

        public AdvancedVoice(@Nullable String instructions) {
            this.instructions = instructions;
        }

        @Nullable
        public String getInstructions() {
            return instructions;
        }
    }
}

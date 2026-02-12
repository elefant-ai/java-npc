package game.player2.npc.dto;

import javax.annotation.Nullable;

/**
 * Response from the TTS speak endpoint ({@code POST /v1/tts/speak}).
 * When {@code play_in_app} is true, the {@code data} field will be empty.
 */
public class TtsSpeakResponse {
    @Nullable
    private String data;

    @Nullable
    public String getData() {
        return data;
    }
}

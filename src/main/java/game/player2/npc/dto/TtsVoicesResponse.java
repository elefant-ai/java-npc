package game.player2.npc.dto;

import java.util.List;

/**
 * Response from the TTS voices endpoint ({@code GET /v1/tts/voices}).
 */
public class TtsVoicesResponse {
    private List<TtsVoice> voices;

    public List<TtsVoice> getVoices() {
        return voices;
    }
}

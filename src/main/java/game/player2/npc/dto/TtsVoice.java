package game.player2.npc.dto;

/**
 * Represents an available TTS voice from the Player2 API.
 */
public class TtsVoice {
    private String id;
    private String name;
    private String language;
    private String gender;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLanguage() {
        return language;
    }

    public String getGender() {
        return gender;
    }
}

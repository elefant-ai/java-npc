package game.player2.npc.dto;

import java.util.Map;

/**
 * Represents an arbitrary JSON object containing property definition for function parameters.
 */
public class Property {
    private final Map<String, Object> additionalProperties;

    public Property(Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }
}

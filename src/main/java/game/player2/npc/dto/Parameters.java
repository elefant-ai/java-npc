package game.player2.npc.dto;

import java.util.List;
import java.util.Map;

/**
 * Represents function parameters with JSON schema structure.
 */
public class Parameters {
    private final String type;
    private final Map<String, Property> properties;
    private final List<String> required;

    public Parameters(Map<String, Property> properties, List<String> required) {
        this.type = "object";
        this.properties = properties;
        this.required = required;
    }

    public String getType() {
        return type;
    }

    public Map<String, Property> getProperties() {
        return properties;
    }

    public List<String> getRequired() {
        return required;
    }
}

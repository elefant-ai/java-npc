package game.player2.npc.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Represents a function/command that an NPC can invoke.
 */
public class Function {
    private final String name;
    private final String description;
    private final Parameters parameters;

    @SerializedName("never_respond_with_message")
    private final Boolean neverRespondWithMessage;

    public Function(String name, String description, Parameters parameters) {
        this(name, description, parameters, null);
    }

    public Function(String name, String description, Parameters parameters, Boolean neverRespondWithMessage) {
        this.name = name;
        this.description = description;
        this.parameters = parameters;
        this.neverRespondWithMessage = neverRespondWithMessage;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Parameters getParameters() {
        return parameters;
    }

    public Boolean getNeverRespondWithMessage() {
        return neverRespondWithMessage;
    }
}

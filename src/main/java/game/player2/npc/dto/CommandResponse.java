package game.player2.npc.dto;

import com.google.gson.Gson;

/**
 * Represents a function/command call response from an NPC.
 */
public class CommandResponse {
    private static final Gson GSON = new Gson();

    private String name;
    private String arguments;

    public String getName() {
        return name;
    }

    public String getArgumentsJson() {
        return arguments;
    }

    /**
     * Parse the arguments JSON into the specified type.
     *
     * @param type The class to deserialize the arguments into
     * @param <T> The type to return
     * @return The parsed arguments
     */
    public <T> T getArguments(Class<T> type) {
        return GSON.fromJson(arguments, type);
    }
}

package game.player2.npc.event;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

import java.util.UUID;

/**
 * Fired when an NPC requests to execute a function/command.
 * <p>
 * Handlers should process the function call and may cancel to prevent
 * other handlers from processing it.
 * Listen for this event on {@code NeoForge.EVENT_BUS}.
 * </p>
 *
 * <pre>{@code
 * @SubscribeEvent
 * public void onNpcCommand(NpcCommandEvent event) {
 *     if ("minecraft_command".equals(event.getCommandName())) {
 *         String cmd = event.getStringArgument("command");
 *         // Execute the command
 *         event.setCanceled(true);
 *     }
 * }
 * }</pre>
 */
public class NpcCommandEvent extends Event implements ICancellableEvent {
    private static final Gson GSON = new Gson();

    private final UUID npcId;
    private final String gameId;
    private final String commandName;
    private final String argumentsJson;
    private JsonObject parsedArguments;

    public NpcCommandEvent(UUID npcId, String gameId, String commandName, String argumentsJson) {
        this.npcId = npcId;
        this.gameId = gameId;
        this.commandName = commandName;
        this.argumentsJson = argumentsJson;
    }

    /**
     * Returns the ID of the NPC that requested this command.
     */
    public UUID getNpcId() {
        return npcId;
    }

    /**
     * Returns the game ID this NPC belongs to.
     */
    public String getGameId() {
        return gameId;
    }

    /**
     * Returns the name of the function/command to execute.
     */
    public String getCommandName() {
        return commandName;
    }

    /**
     * Returns the raw JSON string of function arguments.
     */
    public String getArgumentsJson() {
        return argumentsJson;
    }

    /**
     * Returns the arguments parsed as a JsonObject.
     */
    public JsonObject getArguments() {
        if (parsedArguments == null && argumentsJson != null && !argumentsJson.isEmpty()) {
            try {
                parsedArguments = JsonParser.parseString(argumentsJson).getAsJsonObject();
            } catch (Exception e) {
                parsedArguments = new JsonObject();
            }
        }
        return parsedArguments != null ? parsedArguments : new JsonObject();
    }

    /**
     * Parse the arguments into the specified type.
     *
     * @param type The class to deserialize into
     * @param <T> The type to return
     * @return The parsed arguments
     */
    public <T> T getArguments(Class<T> type) {
        return GSON.fromJson(argumentsJson, type);
    }

    /**
     * Convenience method to get a string argument by name.
     *
     * @param name The argument name
     * @return The string value, or null if not present
     */
    public String getStringArgument(String name) {
        JsonObject args = getArguments();
        if (args != null && args.has(name) && !args.get(name).isJsonNull()) {
            return args.get(name).getAsString();
        }
        return null;
    }

    /**
     * Convenience method to get an int argument by name.
     *
     * @param name The argument name
     * @param defaultValue Default value if not present
     * @return The int value, or defaultValue if not present
     */
    public int getIntArgument(String name, int defaultValue) {
        JsonObject args = getArguments();
        if (args != null && args.has(name) && !args.get(name).isJsonNull()) {
            try {
                return args.get(name).getAsInt();
            } catch (Exception e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    /**
     * Convenience method to get a double argument by name.
     *
     * @param name The argument name
     * @param defaultValue Default value if not present
     * @return The double value, or defaultValue if not present
     */
    public double getDoubleArgument(String name, double defaultValue) {
        JsonObject args = getArguments();
        if (args != null && args.has(name) && !args.get(name).isJsonNull()) {
            try {
                return args.get(name).getAsDouble();
            } catch (Exception e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    /**
     * Convenience method to get a boolean argument by name.
     *
     * @param name The argument name
     * @param defaultValue Default value if not present
     * @return The boolean value, or defaultValue if not present
     */
    public boolean getBooleanArgument(String name, boolean defaultValue) {
        JsonObject args = getArguments();
        if (args != null && args.has(name) && !args.get(name).isJsonNull()) {
            try {
                return args.get(name).getAsBoolean();
            } catch (Exception e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
}

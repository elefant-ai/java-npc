package game.player2.npc.api;

import game.player2.npc.dto.Function;
import game.player2.npc.dto.Parameters;
import game.player2.npc.dto.Property;

import java.util.*;

/**
 * Builder for creating NPC functions/commands.
 * <p>
 * Functions define actions that NPCs can invoke, such as executing Minecraft commands
 * or triggering custom game logic.
 * </p>
 *
 * <pre>{@code
 * NpcFunction sellFunction = NpcFunction.builder("sell_item")
 *     .description("Sell an item to the player")
 *     .addStringParameter("item_id", "The item to sell", true)
 *     .addIntParameter("quantity", "Number of items", false)
 *     .build();
 * }</pre>
 */
public class NpcFunction {
    private final String name;
    private final String description;
    private final Map<String, Property> properties;
    private final List<String> required;
    private final Boolean neverRespondWithMessage;

    private NpcFunction(Builder builder) {
        this.name = builder.name;
        this.description = builder.description;
        this.properties = builder.properties;
        this.required = builder.required;
        this.neverRespondWithMessage = builder.neverRespondWithMessage;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Converts this NpcFunction to the DTO used by the API.
     */
    public Function toDto() {
        Parameters params = properties.isEmpty() ? null : new Parameters(properties, required);
        return new Function(name, description, params, neverRespondWithMessage);
    }

    /**
     * Creates a new builder for an NpcFunction.
     *
     * @param name The function name (used to identify which function the NPC called)
     */
    public static Builder builder(String name) {
        return new Builder(name);
    }

    /**
     * Creates a simple Minecraft command function.
     * This is a convenience method for the common use case of letting NPCs execute commands.
     */
    public static NpcFunction minecraftCommand() {
        return builder("minecraft_command")
            .description("Execute a Minecraft command")
            .addStringParameter("command", "The Minecraft command to run, without a slash prefix", true)
            .build();
    }

    public static class Builder {
        private final String name;
        private String description = "";
        private final Map<String, Property> properties = new LinkedHashMap<>();
        private final List<String> required = new ArrayList<>();
        private Boolean neverRespondWithMessage = null;

        public Builder(String name) {
            this.name = Objects.requireNonNull(name, "name cannot be null");
        }

        /**
         * Sets the description of what this function does.
         */
        public Builder description(String description) {
            this.description = description;
            return this;
        }

        /**
         * Adds a string parameter to the function.
         *
         * @param name Parameter name
         * @param description Parameter description
         * @param isRequired Whether this parameter is required
         */
        public Builder addStringParameter(String name, String description, boolean isRequired) {
            return addParameter(name, "string", description, isRequired);
        }

        /**
         * Adds an integer parameter to the function.
         *
         * @param name Parameter name
         * @param description Parameter description
         * @param isRequired Whether this parameter is required
         */
        public Builder addIntParameter(String name, String description, boolean isRequired) {
            return addParameter(name, "integer", description, isRequired);
        }

        /**
         * Adds a number (double) parameter to the function.
         *
         * @param name Parameter name
         * @param description Parameter description
         * @param isRequired Whether this parameter is required
         */
        public Builder addNumberParameter(String name, String description, boolean isRequired) {
            return addParameter(name, "number", description, isRequired);
        }

        /**
         * Adds a boolean parameter to the function.
         *
         * @param name Parameter name
         * @param description Parameter description
         * @param isRequired Whether this parameter is required
         */
        public Builder addBooleanParameter(String name, String description, boolean isRequired) {
            return addParameter(name, "boolean", description, isRequired);
        }

        /**
         * Adds a parameter with a custom type.
         *
         * @param name Parameter name
         * @param type JSON schema type (string, integer, number, boolean, array, object)
         * @param description Parameter description
         * @param isRequired Whether this parameter is required
         */
        public Builder addParameter(String name, String type, String description, boolean isRequired) {
            Map<String, Object> propMap = new LinkedHashMap<>();
            propMap.put("type", type);
            propMap.put("description", description);
            properties.put(name, new Property(propMap));

            if (isRequired) {
                required.add(name);
            }
            return this;
        }

        /**
         * Adds a parameter with enum values.
         *
         * @param name Parameter name
         * @param description Parameter description
         * @param isRequired Whether this parameter is required
         * @param enumValues Allowed values for this parameter
         */
        public Builder addEnumParameter(String name, String description, boolean isRequired, String... enumValues) {
            Map<String, Object> propMap = new LinkedHashMap<>();
            propMap.put("type", "string");
            propMap.put("description", description);
            propMap.put("enum", Arrays.asList(enumValues));
            properties.put(name, new Property(propMap));

            if (isRequired) {
                required.add(name);
            }
            return this;
        }

        /**
         * If true, the NPC should not respond with a message when calling this function.
         * Use this for functions that are purely actions without conversational response.
         */
        public Builder neverRespondWithMessage(boolean never) {
            this.neverRespondWithMessage = never;
            return this;
        }

        public NpcFunction build() {
            return new NpcFunction(this);
        }
    }
}

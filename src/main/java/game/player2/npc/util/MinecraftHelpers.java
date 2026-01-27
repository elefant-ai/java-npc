package game.player2.npc.util;

import com.google.gson.JsonObject;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

/**
 * Helper utilities for common Minecraft operations with NPCs.
 * <p>
 * These utilities simplify common tasks like sending NPC messages to players
 * and executing Minecraft commands.
 * </p>
 *
 * <pre>{@code
 * // Send NPC message to player
 * MinecraftHelpers.sendNpcMessage(player, "Merchant Bob", "Hello, traveler!");
 *
 * // Execute command from NPC
 * MinecraftHelpers.executeCommand(server, "give Steve diamond 5");
 *
 * // Get player context for NPC
 * String context = MinecraftHelpers.getPlayerContext(player);
 * npc.chat("Steve", "Where am I?", context);
 * }</pre>
 */
public final class MinecraftHelpers {
    private static final Logger LOGGER = LoggerFactory.getLogger(MinecraftHelpers.class);

    private MinecraftHelpers() {} // Prevent instantiation

    /**
     * Sends a chat message as an NPC to a player.
     *
     * @param player The player to send the message to
     * @param npcName The name of the NPC sending the message
     * @param message The message content
     */
    public static void sendNpcMessage(ServerPlayer player, String npcName, String message) {
        if (player == null || message == null) {
            return;
        }

        String formatted = String.format("<%s> %s", npcName, message.trim());
        player.sendSystemMessage(Component.literal(formatted));
        LOGGER.debug("Sent NPC message from {} to {}: {}", npcName, player.getName().getString(), message);
    }

    /**
     * Sends a chat message as an NPC to all players on the server.
     *
     * @param server The Minecraft server
     * @param npcName The name of the NPC sending the message
     * @param message The message content
     */
    public static void broadcastNpcMessage(MinecraftServer server, String npcName, String message) {
        if (server == null || message == null) {
            return;
        }

        String formatted = String.format("<%s> %s", npcName, message.trim());
        server.getPlayerList().broadcastSystemMessage(Component.literal(formatted), false);
        LOGGER.debug("Broadcast NPC message from {}: {}", npcName, message);
    }

    /**
     * Sends a system/info message to a player.
     *
     * @param player The player to send the message to
     * @param message The message content
     */
    public static void sendSystemMessage(ServerPlayer player, String message) {
        if (player == null || message == null) {
            return;
        }

        player.sendSystemMessage(Component.literal(message));
    }

    /**
     * Sends a system/info message with a prefix to a player.
     *
     * @param player The player to send the message to
     * @param prefix The prefix (e.g., "INFO", "ERROR")
     * @param message The message content
     */
    public static void sendSystemMessage(ServerPlayer player, String prefix, String message) {
        if (player == null || message == null) {
            return;
        }

        String formatted = String.format("[%s] %s", prefix, message);
        player.sendSystemMessage(Component.literal(formatted));
    }

    /**
     * Executes a Minecraft command as the server.
     * <p>
     * Commands should NOT include the leading slash.
     * </p>
     *
     * @param server The Minecraft server
     * @param command The command to execute (without leading slash)
     * @return true if the command executed successfully
     */
    public static boolean executeCommand(MinecraftServer server, String command) {
        if (server == null || command == null || command.isBlank()) {
            return false;
        }

        try {
            // Handle multiple commands separated by newlines
            String[] commands = command.split("\\r?\\n");

            for (String cmd : commands) {
                cmd = cmd.trim();
                if (!cmd.isEmpty()) {
                    // Remove leading slash if present
                    if (cmd.startsWith("/")) {
                        cmd = cmd.substring(1);
                    }

                    LOGGER.debug("Executing command: {}", cmd);
                    CommandSourceStack source = server.createCommandSourceStack();
                    server.getCommands().performPrefixedCommand(source, cmd);
                }
            }

            return true;
        } catch (Exception e) {
            LOGGER.error("Failed to execute command: {}", command, e);
            return false;
        }
    }

    /**
     * Gets player context information for NPC conversations.
     * <p>
     * Returns a JSON string with the player's name, position, and dimension.
     * This can be passed as the gameStateInfo parameter when chatting with NPCs.
     * </p>
     *
     * @param player The player to get context for
     * @return JSON string with player context, or empty string if player is null
     */
    public static String getPlayerContext(ServerPlayer player) {
        if (player == null) {
            return "";
        }

        JsonObject json = new JsonObject();
        json.addProperty("playerName", player.getName().getString());
        json.addProperty("position", String.format("(%d, %d, %d)",
            (int) player.getX(), (int) player.getY(), (int) player.getZ()));
        json.addProperty("dimension", player.level().dimension().location().toString());

        // Add health and hunger if you want more context
        json.addProperty("health", (int) player.getHealth());
        json.addProperty("food", player.getFoodData().getFoodLevel());

        return json.toString();
    }

    /**
     * Gets a formatted player status string for NPC conversations.
     * <p>
     * Returns a human-readable string describing the player's current state.
     * </p>
     *
     * @param player The player to get status for
     * @return Formatted status string, or empty string if player is null
     */
    public static String getPlayerStatus(ServerPlayer player) {
        if (player == null) {
            return "";
        }

        String dimension = player.level().dimension().location().toString();
        return String.format("Player '%s' is at (%d, %d, %d) in %s",
            player.getName().getString(),
            (int) player.getX(),
            (int) player.getY(),
            (int) player.getZ(),
            dimension);
    }

    /**
     * Creates a message with player context embedded.
     * <p>
     * Combines a player message with their current status for NPC context.
     * </p>
     *
     * @param player The player sending the message
     * @param message The message content
     * @return JSON string with message and player status
     */
    public static String createContextualMessage(ServerPlayer player, String message) {
        JsonObject json = new JsonObject();
        json.addProperty("message", message);

        if (player != null) {
            json.addProperty("playerStatus", getPlayerStatus(player));
        }

        return json.toString();
    }
}
